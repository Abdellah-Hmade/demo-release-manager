package com.code_challenge.release_manager.domain

import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
data class AppsVersion(
    @EmbeddedId
    var appsVersionId: AppsVersionId,
    @CreationTimestamp
    var creationDate: LocalDateTime? = null
)