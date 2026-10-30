package com.code_challenge.release_manager.domain

import jakarta.persistence.Embeddable

@Embeddable
data class AppsVersionId(
    var name: String,
    var version: Int,
    var systemVersionNumber: Int,
    )