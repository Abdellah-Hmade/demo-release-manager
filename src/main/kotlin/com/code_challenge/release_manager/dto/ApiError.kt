package com.code_challenge.release_manager.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "Error returned for any failed request")
class ApiError(
    @field:Schema(example = "400")
    val status: Int,
    @field:Schema(example = "Bad Request")
    val error: String,
    @field:Schema(example = "Validation failed")
    val message: String,
    @field:Schema(description = "Field level details, when available", example = "[\"name: must not be blank\"]")
    val details: List<String> = emptyList(),
    val timestamp: Instant = Instant.now()
)
