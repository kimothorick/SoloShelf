package com.kimothorick.soloshelf.util
import androidx.annotation.StringRes
import org.readium.r2.shared.util.Error

/**
 * An error that can be presented to the user.
 *
 * @param resId String resource ID for the user-facing message.
 * @param args Arguments to be used with the string resource.
 * @param cause The original cause of the error.
 */
data class UserError(
    @StringRes val resId: Int,
    val args: List<Any> = emptyList(),
    override val cause: Error? = null,
) : Error {
    constructor(
        @StringRes resId: Int,
        vararg args: Any,
        cause: Error? = null,
    ) :
        this(resId, args.toList(), cause)

    override val message: String = "User error"
}
