package com.code_challenge.release_manager.repository

import com.code_challenge.release_manager.domain.AppsVersion
import com.code_challenge.release_manager.domain.AppsVersionId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AppsVersionRepository : JpaRepository<AppsVersion, AppsVersionId> {

    @Query(
        value = "SELECT * FROM apps_version WHERE system_version_number = :systemVersionNumber",
        nativeQuery = true
    )
    fun findAllAppsVersionsBySystemVersionNumber(systemVersionNumber: Int): List<AppsVersion>

}