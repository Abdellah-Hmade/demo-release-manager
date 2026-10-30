package com.code_challenge.release_manager.controller

import com.code_challenge.release_manager.dto.AppsVersionDto
import com.code_challenge.release_manager.service.AppsVersionService
import com.code_challenge.release_manager.dto.ApiError
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Release manager", description = "Deployments and the services of each system version")
@RestController
class AppsVersionController (
    private val appsVersionService: AppsVersionService
){

    private val log = LoggerFactory.getLogger(AppsVersionController::class.java)

    @Operation(
        summary = "List the services of a system version",
    )
    @ApiResponse(responseCode = "200", description = "Services of the system version")
    @ApiResponse(
        responseCode = "400", description = "Missing or invalid systemVersion",
        content = [Content(schema = Schema(implementation = ApiError::class))]
    )
    @GetMapping("/services")
    fun getAllAppsVersions(
        @Parameter(description = "System version number", example = "2")
        @RequestParam(name = "systemVersion") systemVersionNumber: Int
    ): List<AppsVersionDto> {
        log.info("Start controller: getAllAppsVersions: systemVersionNumber={}", systemVersionNumber)
        val appsVersions = appsVersionService.getAllAppsVersions(systemVersionNumber)
        log.info("End controller: getAllAppsVersions: systemVersionNumber={}, size={}", systemVersionNumber, appsVersions.size)
        return appsVersions
    }

    @Operation(
        summary = "Register a service deployment",
    )
    @ApiResponse(responseCode = "200", description = "System version number", content = [Content(schema = Schema(example = "3"))])
    @ApiResponse(
        responseCode = "400", description = "Invalid request body",
        content = [Content(schema = Schema(implementation = ApiError::class))]
    )
    @ApiResponse(
        responseCode = "500", description = "Unexpected error",
        content = [Content(schema = Schema(implementation = ApiError::class))]
    )
    @PostMapping("/deploy")
    fun deployAppVersion(@RequestBody @Valid appsVersionDto: AppsVersionDto): Int {
        log.info("Start controller: deployAppVersion: name={}, version={}", appsVersionDto.name, appsVersionDto.version)
        val systemVersionNumber = appsVersionService.deployNewAppsVersion(appsVersionDto)
        log.info("End controller: deployAppVersion: systemVersionNumber={}", systemVersionNumber)
        return systemVersionNumber
    }

}
