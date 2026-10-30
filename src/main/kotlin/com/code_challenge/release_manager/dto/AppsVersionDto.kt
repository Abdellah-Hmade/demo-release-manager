package com.code_challenge.release_manager.dto

import com.fasterxml.jackson.annotation.JsonAlias
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

@Schema(description = "A service and its deployed version")
class AppsVersionDto(
    @field:NotBlank
    @field:Schema(description = "Service name (the request also accepts it as serviceName)", example = "Service A")
    @JsonAlias("serviceName")
    val name: String,
    @field:Schema(description = "Service version number (the request also accepts it as serviceVersionNumber)", example = "1", minimum = "0")
    @JsonAlias("serviceVersionNumber")
    @field:Min(0)
    val version: Int
)
