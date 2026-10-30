package com.code_challenge.release_manager.domain

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class SystemVersion(
    @Id
    val id: Int = 1,
    var currentNumber: Int = 0
)
