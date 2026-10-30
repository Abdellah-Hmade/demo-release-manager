package com.code_challenge.release_manager.service

import com.code_challenge.release_manager.domain.AppsVersion
import com.code_challenge.release_manager.domain.AppsVersionId
import com.code_challenge.release_manager.domain.SystemVersion
import com.code_challenge.release_manager.dto.AppsVersionDto
import com.code_challenge.release_manager.mapper.AppsVersionMapper
import com.code_challenge.release_manager.repository.AppsVersionRepository
import com.code_challenge.release_manager.repository.SystemVersionRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mapstruct.factory.Mappers
import org.mockito.ArgumentMatchers.anyIterable
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class AppsVersionServiceTest {

    @Mock
    private lateinit var repository: AppsVersionRepository

    @Mock
    private lateinit var systemVersionRepository: SystemVersionRepository

    private lateinit var service: AppsVersionService

    private var savedEntities: List<AppsVersion> = emptyList()

    @BeforeEach
    fun setUp() {
        service = AppsVersionService(
            repository,
            systemVersionRepository,
            Mappers.getMapper(AppsVersionMapper::class.java)
        )
    }

    private fun appsVersion(name: String, version: Int, systemVersionNumber: Int) =
        AppsVersion(AppsVersionId(name, version, systemVersionNumber))

    private fun stubCurrent(currentNumber: Int, vararg apps: AppsVersion): SystemVersion {
        val systemVersion = SystemVersion(currentNumber = currentNumber)
        `when`(systemVersionRepository.lockCurrent()).thenReturn(systemVersion)
        `when`(repository.findAllAppsVersionsBySystemVersionNumber(currentNumber)).thenReturn(apps.toList())
        return systemVersion
    }

    private fun stubSaveAll() {
        `when`(repository.saveAll(anyIterable<AppsVersion>())).thenAnswer { invocation ->
            savedEntities = (invocation.arguments[0] as Iterable<*>).filterIsInstance<AppsVersion>()
            savedEntities
        }
    }

    @Test
    fun `deploy returns existing system version when app and version are already deployed`() {
        val systemVersion = stubCurrent(3, appsVersion("a", 1, 3), appsVersion("b", 2, 3))

        val result = service.deployNewAppsVersion(AppsVersionDto("b", 2))

        assertEquals(3, result)
        assertEquals(3, systemVersion.currentNumber)
        verify(repository, never()).saveAll(anyIterable<AppsVersion>())
    }

    @Test
    fun `deploy on empty state creates the first system version with only the new app`() {
        val systemVersion = stubCurrent(0)
        stubSaveAll()

        val result = service.deployNewAppsVersion(AppsVersionDto("a", 1))

        assertEquals(1, result)
        assertEquals(1, systemVersion.currentNumber)
        assertEquals(listOf(appsVersion("a", 1, 1).appsVersionId), savedEntities.map { it.appsVersionId })
    }

    @Test
    fun `deploy copies existing apps into the new system version`() {
        stubCurrent(1, appsVersion("a", 1, 1))
        stubSaveAll()

        val result = service.deployNewAppsVersion(AppsVersionDto("b", 1))

        assertEquals(2, result)
        assertEquals(
            listOf(AppsVersionId("a", 1, 2), AppsVersionId("b", 1, 2)),
            savedEntities.map { it.appsVersionId }
        )
    }

    @Test
    fun `deploy replaces the previous version of the same app`() {
        stubCurrent(1, appsVersion("a", 1, 1), appsVersion("b", 1, 1))
        stubSaveAll()

        val result = service.deployNewAppsVersion(AppsVersionDto("a", 2))

        assertEquals(2, result)
        assertEquals(
            listOf(AppsVersionId("b", 1, 2), AppsVersionId("a", 2, 2)),
            savedEntities.map { it.appsVersionId }
        )
    }

    @Test
    fun `getAllAppsVersions maps the entities of the requested system version`() {
        `when`(repository.findAllAppsVersionsBySystemVersionNumber(2))
            .thenReturn(listOf(appsVersion("a", 1, 2), appsVersion("b", 3, 2)))

        val result = service.getAllAppsVersions(2)

        assertEquals(listOf("a" to 1, "b" to 3), result.map { it.name to it.version })
    }

    @Test
    fun `getAllAppsVersions returns an empty list when the system version does not exist`() {
        `when`(repository.findAllAppsVersionsBySystemVersionNumber(9)).thenReturn(emptyList())

        assertEquals(emptyList(), service.getAllAppsVersions(9))
    }
}
