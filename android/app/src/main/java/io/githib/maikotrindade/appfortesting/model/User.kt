package io.githib.maikotrindade.appfortesting.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val username: String,
    val profilePictureUrl: String? = null,
    val bio: String = ""
) : Parcelable
