package com.kimothorick.soloshelf.ui.tooling

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

/**
 * Multi preview annotation that represents various device sizes and orientations
 * to test adaptive layouts.
 */
@Preview(name = "Phone", device = Devices.PHONE, showSystemUi = true)
@Preview(name = "Phone - Landscape", device = "spec:width=891dp,height=411dp,orientation=landscape,dpi=420", showSystemUi = true)
@Preview(name = "Unfolded Foldable", device = Devices.FOLDABLE, showSystemUi = true)
@Preview(name = "Tablet", device = Devices.TABLET, showSystemUi = true)
@Preview(name = "Desktop", device = Devices.DESKTOP, showSystemUi = true)
annotation class DevicePreviews

/**
 * Multi preview annotation that represents light and dark themes.
 */
@Preview(name = "Light Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class ThemePreviews

/**
 * Multi preview annotation for UI components, showing them in both light and dark themes.
 */
@ThemePreviews
annotation class ComponentPreviews

/**
 * Combined multi preview for both devices and themes.
 */
@DevicePreviews
@ThemePreviews
annotation class CombinedPreviews
