package com.apppillar.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String,
    val name: String
)