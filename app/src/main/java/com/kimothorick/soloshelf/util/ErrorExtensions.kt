/*
 * Copyright 2023 Readium Foundation. All rights reserved.
 * Use of this source code is governed by the BSD-style license
 * available in the top-level LICENSE file of the project.
 */

package com.kimothorick.soloshelf.util

import com.kimothorick.soloshelf.R
import org.readium.r2.shared.util.Error
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.data.ReadError
import org.readium.r2.streamer.PublicationOpener

/**
 * Extension functions to convert Readium [Error]s to [UserError]s.
 */
fun Error.toUserError(): UserError = when (this) {
    is UserError -> this
    is PublicationOpener.OpenError -> when (this) {
        is PublicationOpener.OpenError.Reading -> cause.toUserError()
        is PublicationOpener.OpenError.FormatNotSupported -> UserError(R.string.unsupported_format, cause = this)
        else -> UserError(R.string.publication_error_unexpected, cause = this)
    }
    is AssetRetriever.RetrieveError -> when (this) {
        is AssetRetriever.RetrieveError.Reading -> cause.toUserError()
        is AssetRetriever.RetrieveError.FormatNotSupported -> UserError(R.string.unsupported_format, cause = this)
        else -> UserError(R.string.publication_error_unexpected, cause = this)
    }
    is AssetRetriever.RetrieveUrlError -> when (this) {
        is AssetRetriever.RetrieveUrlError.Reading -> cause.toUserError()
        is AssetRetriever.RetrieveUrlError.FormatNotSupported -> UserError(R.string.unsupported_format, cause = this)
        is AssetRetriever.RetrieveUrlError.SchemeNotSupported -> UserError(R.string.publication_error_scheme_not_supported, cause = this)
        else -> UserError(R.string.publication_error_unexpected, cause = this)
    }
    is ReadError -> UserError(R.string.publication_error_reading, cause = this)
    else -> UserError(R.string.error_adding_book, cause = this)
}
