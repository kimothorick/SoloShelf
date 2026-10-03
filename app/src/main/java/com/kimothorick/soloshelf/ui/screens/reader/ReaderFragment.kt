package com.kimothorick.soloshelf.ui.screens.reader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.domain.ReaderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.readium.adapter.pdfium.navigator.PdfiumEngineProvider
import org.readium.r2.navigator.Navigator
import org.readium.r2.navigator.NavigatorFragment
import org.readium.r2.navigator.VisualNavigator
import org.readium.r2.navigator.epub.EpubNavigatorFactory
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.navigator.input.InputListener
import org.readium.r2.navigator.input.TapEvent
import org.readium.r2.navigator.pdf.PdfNavigatorFactory
import org.readium.r2.navigator.pdf.PdfNavigatorFragment
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.shared.ExperimentalReadiumApi
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.AbsoluteUrl
import javax.inject.Inject

@OptIn(ExperimentalReadiumApi::class)
@AndroidEntryPoint
class ReaderFragment :
    Fragment(),
    EpubNavigatorFragment.Listener,
    PdfNavigatorFragment.Listener,
    InputListener {
    @Inject
    lateinit var readerRepository: ReaderRepository

    private val publication get() = readerRepository.currentPublication

    private val viewModel: ReaderViewModel by viewModels({ requireActivity() })

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        val pub = readerRepository.currentPublication

        // We MUST set a FragmentFactory before super.onCreate if we are restoring state,
        // otherwise the FragmentManager won't know how to instantiate Readium fragments.
        // If we don't have the publication yet, we use a placeholder factory to prevent crashes.
        if (pub != null) {
            setupFragmentFactory(pub)
        } else {
            childFragmentManager.fragmentFactory = PlaceholderFragmentFactory()
        }

        super.onCreate(savedInstanceState)
    }

    /**
     * A fallback FragmentFactory that avoids crashes when the Navigator fragments
     * are being restored but the Publication is not yet available.
     */
    private class PlaceholderFragmentFactory : androidx.fragment.app.FragmentFactory() {
        override fun instantiate(
            classLoader: ClassLoader,
            className: String,
        ): Fragment =
            when (className) {
                PdfNavigatorFragment::class.java.name,
                EpubNavigatorFragment::class.java.name,
                -> Fragment()

                // Return dummy fragment to avoid crash
                else -> super.instantiate(classLoader, className)
            }
    }

    private fun setupFragmentFactory(
        publication: Publication,
    ) {
        when {
            publication.conformsTo(Publication.Profile.EPUB) -> {
                val factory = EpubNavigatorFactory(publication)
                childFragmentManager.fragmentFactory = factory.createFragmentFactory(
                    initialLocator = viewModel.initialLocator.value,
                    listener = this,
                )
            }

            publication.conformsTo(Publication.Profile.PDF) -> {
                val factory = PdfNavigatorFactory(
                    publication = publication,
                    pdfEngineProvider = PdfiumEngineProvider(),
                )
                childFragmentManager.fragmentFactory = factory.createFragmentFactory(
                    initialLocator = viewModel.initialLocator.value,
                    listener = this,
                )
            }
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val pub = publication ?: return
        setupFragmentFactory(pub)

        childFragmentManager.addFragmentOnAttachListener { _, fragment ->
            if (fragment is VisualNavigator) {
                fragment.addInputListener(this)
                observeCurrentLocator(fragment)
            }
        }

        (childFragmentManager.findFragmentById(R.id.navigator_container) as? VisualNavigator)?.apply {
            addInputListener(this@ReaderFragment)
            observeCurrentLocator(this)
        }

        observeNavigationEvents()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.readerPreferences.collectLatest { preferences ->
                    val fragment = childFragmentManager.findFragmentById(R.id.navigator_container)
                    if (fragment is EpubNavigatorFragment) {
                        fragment.submitPreferences(preferences)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.readerBrightness.collectLatest { brightness ->
                    val activity = activity ?: return@collectLatest
                    val layoutParams = activity.window.attributes
                    layoutParams.screenBrightness = if (brightness != null) brightness / 100f else -1f
                    activity.window.attributes = layoutParams
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.initialLocator.collectLatest {
                    // Refresh factory with latest locator in case of recreation
                    publication?.let { setupFragmentFactory(it) }
                }
            }
        }

        val currentFragment = childFragmentManager.findFragmentById(R.id.navigator_container)

        // If the current fragment is NOT a VisualNavigator, it means our PlaceholderFragmentFactory
        // created a dummy Fragment during restoration. We need to remove it and add the real one.
        val needsReplacement = currentFragment != null && currentFragment !is VisualNavigator

        if (currentFragment == null || needsReplacement) {
            val publication = publication ?: return
            val fragmentClass = when {
                publication.conformsTo(Publication.Profile.EPUB) -> EpubNavigatorFragment::class.java
                publication.conformsTo(Publication.Profile.PDF) -> PdfNavigatorFragment::class.java
                else -> throw IllegalStateException("Unsupported publication profile")
            }

            childFragmentManager.commit {
                replace(R.id.navigator_container, fragmentClass, null)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        FrameLayout(inflater.context).apply {
            id = R.id.navigator_container
        }

    private fun observeCurrentLocator(
        navigator: Navigator,
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                navigator.currentLocator
                    .collectLatest { locator ->
                        // Only save if it's not the initial jump to the saved position
                        // and use a larger delay to debounce frequent emissions during selection/scrolling
                        if (!viewModel.isLoading.value) {
                            kotlinx.coroutines.delay(1000) // Increase to 1 second for better stability
                            viewModel.saveProgress(locator)
                        }
                    }
            }
        }
    }

    private fun observeNavigationEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collectLatest { link ->
                    (childFragmentManager.findFragmentById(R.id.navigator_container) as? VisualNavigator)
                        ?.go(link, animated = true)
                }
            }
        }
    }

    override fun onTap(
        event: TapEvent,
    ): Boolean {
        val viewWidth = view?.width ?: return false
        val tapX = event.point.x

        // Define a "dead zone" in the middle 60% of the screen for toggling bars
        // The outer 20% on each side will be left for Readium to handle (page turning)
        val deadZoneWidth = viewWidth * 0.2
        if (tapX > deadZoneWidth && tapX < viewWidth - deadZoneWidth) {
            viewModel.toggleBars()
            return true
        }

        return false
    }

    override fun onJumpToLocator(
        locator: Locator,
    ) {
        viewModel.saveProgress(locator)
    }

    override fun onExternalLinkActivated(
        url: AbsoluteUrl,
    ) {
        // TODO: Open in browser
    }
}
