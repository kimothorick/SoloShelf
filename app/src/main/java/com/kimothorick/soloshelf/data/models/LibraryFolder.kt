package com.kimothorick.soloshelf.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class LibraryFolder(
    val uri: String,
    val name: String,
    val dateAdded: String,
) : Parcelable
