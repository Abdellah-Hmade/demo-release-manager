package com.code_challenge.release_manager.repository

import com.code_challenge.release_manager.domain.SystemVersion
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SystemVersionRepository : JpaRepository<SystemVersion, Int> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SystemVersion s WHERE s.id = 1")
    fun lockCurrent(): SystemVersion
}
