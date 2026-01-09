package com.example.accessingdatarest

import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class AccessingDataRestApplicationTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var personRepository: PersonRepository

    @BeforeEach
    fun deleteAllBeforeTests() {
        personRepository.deleteAll()
    }

    @Test
    fun shouldReturnRepositoryIndex() {
        mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk)
            .andExpect(jsonPath("$._links.people").exists())
    }

    @Test
    fun shouldCreateEntity() {
        mockMvc.perform(post("/people").content(
            "{\"firstName\": \"Frodo\", \"lastName\":\"Baggins\"}"))
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", containsString("people/")))
    }

    @Test
    fun shouldRetrieveEntity() {
        val mvcResult = mockMvc.perform(post("/people").content(
            "{\"firstName\": \"Frodo\", \"lastName\":\"Baggins\"}"))
            .andExpect(status().isCreated).andReturn()

        val location = mvcResult.response.getHeader("Location")!!
        mockMvc.perform(get(location)).andExpect(status().isOk)
            .andExpect(jsonPath("$.firstName").value("Frodo"))
            .andExpect(jsonPath("$.lastName").value("Baggins"))
    }

    @Test
    fun shouldQueryEntity() {
        mockMvc.perform(post("/people").content(
            "{ \"firstName\": \"Frodo\", \"lastName\":\"Baggins\"}"))
            .andExpect(status().isCreated)

        mockMvc.perform(get("/people/search/findByLastName?name={name}", "Baggins"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.people[0].firstName").value("Frodo"))
    }

    @Test
    fun shouldUpdateEntity() {
        val mvcResult = mockMvc.perform(post("/people").content(
            "{\"firstName\": \"Frodo\", \"lastName\":\"Baggins\"}"))
            .andExpect(status().isCreated).andReturn()

        val location = mvcResult.response.getHeader("Location")!!

        mockMvc.perform(put(location).content(
            "{\"firstName\": \"Bilbo\", \"lastName\":\"Baggins\"}"))
            .andExpect(status().isNoContent)

        mockMvc.perform(get(location)).andExpect(status().isOk)
            .andExpect(jsonPath("$.firstName").value("Bilbo"))
            .andExpect(jsonPath("$.lastName").value("Baggins"))
    }

    @Test
    fun shouldPartiallyUpdateEntity() {
        val mvcResult = mockMvc.perform(post("/people").content(
            "{\"firstName\": \"Frodo\", \"lastName\":\"Baggins\"}"))
            .andExpect(status().isCreated).andReturn()

        val location = mvcResult.response.getHeader("Location")!!

        mockMvc.perform(patch(location).content("{\"firstName\": \"Bilbo Jr.\"}"))
            .andExpect(status().isNoContent)

        mockMvc.perform(get(location)).andExpect(status().isOk)
            .andExpect(jsonPath("$.firstName").value("Bilbo Jr."))
            .andExpect(jsonPath("$.lastName").value("Baggins"))
    }

    @Test
    fun shouldDeleteEntity() {
        val mvcResult = mockMvc.perform(post("/people").content(
            "{ \"firstName\": \"Bilbo\", \"lastName\":\"Baggins\"}"))
            .andExpect(status().isCreated).andReturn()

        val location = mvcResult.response.getHeader("Location")!!
        mockMvc.perform(delete(location)).andExpect(status().isNoContent)

        mockMvc.perform(get(location)).andExpect(status().isNotFound)
    }
}
