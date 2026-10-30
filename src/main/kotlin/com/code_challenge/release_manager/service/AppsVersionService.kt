package com.code_challenge.release_manager.service

import com.code_challenge.release_manager.dto.AppsVersionDto
import com.code_challenge.release_manager.mapper.AppsVersionMapper
import com.code_challenge.release_manager.repository.AppsVersionRepository
import com.code_challenge.release_manager.repository.SystemVersionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AppsVersionService(
    private val appsVersionRepository: AppsVersionRepository,
    private val systemVersionRepository: SystemVersionRepository,
    private val appsVersionMapper: AppsVersionMapper
) {

    private val log = LoggerFactory.getLogger(AppsVersionService::class.java)


    @Transactional
    fun deployNewAppsVersion(appsVersionDto: AppsVersionDto): Int {
        log.info("Start service: deployNewAppsVersion: name={}, version={}", appsVersionDto.name, appsVersionDto.version)

        val systemVersion = systemVersionRepository.lockCurrent()
        val currentApps = appsVersionRepository.findAllAppsVersionsBySystemVersionNumber(systemVersion.currentNumber)

        if (currentApps.any { it.appsVersionId.name == appsVersionDto.name && it.appsVersionId.version == appsVersionDto.version }) {
            log.info("End service: deployNewAppsVersion: nextSystemVersion={}", systemVersion.currentNumber)
            return systemVersion.currentNumber
        }

        systemVersion.currentNumber += 1
        val nextSystemVersion = systemVersion.currentNumber
        val newEntity = appsVersionMapper.toEntity(appsVersionDto)

        val newSnapshot = currentApps
            .filter { it.appsVersionId.name != newEntity.appsVersionId.name }
            .map { it.copy(appsVersionId = it.appsVersionId.copy(systemVersionNumber = nextSystemVersion)) } +
                newEntity.copy(appsVersionId = newEntity.appsVersionId.copy(systemVersionNumber = nextSystemVersion))
        appsVersionRepository.saveAll(newSnapshot)

        log.info("End service: deployNewAppsVersion: nextSystemVersion={}", nextSystemVersion)
        return nextSystemVersion
    }


    fun getAllAppsVersions(systemVersionNumber: Int): List<AppsVersionDto> {
        log.info("Start service: getAllAppsVersions: systemVersionNumber={}", systemVersionNumber)
        val appsVersions = appsVersionMapper.toDtoList(
            appsVersionRepository.findAllAppsVersionsBySystemVersionNumber(systemVersionNumber)
        )
        log.info("End service: getAllAppsVersions: systemVersionNumber={}, size={}", systemVersionNumber, appsVersions.size)
        return appsVersions
    }

}