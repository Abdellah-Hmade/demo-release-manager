package com.code_challenge.release_manager.controller

import com.code_challenge.release_manager.dto.AppsVersionDto
import com.code_challenge.release_manager.service.AppsVersionService
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(AppsVersionController::class)
class AppsVersionControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var appsVersionService: AppsVersionService

    private fun anyDto(): AppsVersionDto =
        ArgumentMatchers.any(AppsVersionDto::class.java) ?: AppsVersionDto("", 0)

    @Test
    fun `POST deploy returns the system version number`() {
        `when`(appsVersionService.deployNewAppsVersion(anyDto())).thenReturn(4)

        mockMvc.perform(
            post("/deploy").contentType(MediaType.APPLICATION_JSON).content("""{"name":"a","version":1}""")
        )
            .andExpect(status().isOk)
            .andExpect(content().string("4"))
    }

    @Test
    fun `POST deploy rejects a blank name`() {
        mockMvc.perform(
            post("/deploy").contentType(MediaType.APPLICATION_JSON).content("""{"name":" ","version":1}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.details[0]").value("name: must not be blank"))

        verify(appsVersionService, never()).deployNewAppsVersion(anyDto())
    }

    @Test
    fun `POST deploy rejects a negative version`() {
        mockMvc.perform(
            post("/deploy").contentType(MediaType.APPLICATION_JSON).content("""{"name":"a","version":-1}""")
        ).andExpect(status().isBadRequest)

        verify(appsVersionService, never()).deployNewAppsVersion(anyDto())
    }

    @Test
    fun `POST deploy rejects a malformed body`() {
        mockMvc.perform(post("/deploy").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
    }

    @Test
    fun `POST deploy without a body is a bad request`() {
        mockMvc.perform(post("/deploy").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
    }

    @Test
    fun `POST deploy returns a 500 error body when the service fails`() {
        `when`(appsVersionService.deployNewAppsVersion(anyDto())).thenThrow(IllegalStateException("boom"))

        mockMvc.perform(
            post("/deploy").contentType(MediaType.APPLICATION_JSON).content("""{"name":"a","version":1}""")
        )
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.message").value("Unexpected error"))
    }

    @Test
    fun `GET services returns the apps of the requested system version`() {
        `when`(appsVersionService.getAllAppsVersions(2))
            .thenReturn(listOf(AppsVersionDto("a", 1), AppsVersionDto("b", 3)))

        mockMvc.perform(get("/services").param("systemVersion", "2"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("a"))
            .andExpect(jsonPath("$[1].version").value(3))
    }

    @Test
    fun `GET services without the system version parameter is a bad request`() {
        mockMvc.perform(get("/services"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Required parameter 'systemVersion' is missing"))
    }

    @Test
    fun `GET services with a non numeric system version is a bad request`() {
        mockMvc.perform(get("/services").param("systemVersion", "abc"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Parameter 'systemVersion' has an invalid value"))
    }

    @Test
    fun `unsupported method keeps its 405 status`() {
        mockMvc.perform(get("/deploy"))
            .andExpect(status().isMethodNotAllowed)
            .andExpect(jsonPath("$.status").value(405))
    }

    @Test
    fun `unknown path keeps its 404 status`() {
        mockMvc.perform(get("/unknown"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
    }
}
