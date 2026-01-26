package com.example.accessingdatarest

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

@SpringBootTest
@AutoConfigureMockMvc
class AccessingDataRestApplicationTests @Autowired constructor(
    private val mockMvc: MockMvc,
    private val personRepository: PersonRepository) {

    @BeforeEach
    fun deleteAllBeforeTests() {
        personRepository.deleteAll()
    }

    @Test
    fun shouldReturnRepositoryIndex() {
        mockMvc.get("/").andDo { print() }.andExpect {
            status { isOk() }
            jsonPath("$._links.people") { exists() }
        }
    }

    @Test
    fun shouldCreateEntity() {
        mockMvc.post("/people") {
            content = "{\"firstName\": \"Frodo\", \"lastName\":\"Baggins\"}"
        }.andExpect {
            status { isCreated() }
            header { string("Location", org.hamcrest.Matchers.containsString("people/")) }
        }
    }

    @Test
    fun shouldRetrieveEntity() {
        val mvcResult = mockMvc.post("/people") {
            content = "{\"firstName\": \"Frodo\", \"lastName\":\"Baggins\"}"
        }.andExpect {
            status { isCreated() }
        }.andReturn()

        val location = mvcResult.response.getHeader("Location")!!
        mockMvc.get(location).andExpect {
            status { isOk() }
            jsonPath("$.firstName") { value("Frodo") }
            jsonPath("$.lastName") { value("Baggins") }
        }
    }

    @Test
    fun shouldQueryEntity() {
        mockMvc.post("/people") {
            content = "{ \"firstName\": \"Frodo\", \"lastName\":\"Baggins\"}"
        }.andExpect {
            status { isCreated() }
        }

        mockMvc.get("/people/search/findByLastName?name={name}", "Baggins").andExpect {
            status { isOk() }
            jsonPath("$._embedded.people[0].firstName") { value("Frodo") }
        }
    }

    @Test
    fun shouldUpdateEntity() {
        val mvcResult = mockMvc.post("/people") {
            content = "{\"firstName\": \"Frodo\", \"lastName\":\"Baggins\"}"
        }.andExpect {
            status { isCreated() }
        }.andReturn()

        val location = mvcResult.response.getHeader("Location")!!

        mockMvc.put(location) {
            content = "{\"firstName\": \"Bilbo\", \"lastName\":\"Baggins\"}"
        }.andExpect {
            status { isNoContent() }
        }

        mockMvc.get(location).andExpect {
            status { isOk() }
            jsonPath("$.firstName") { value("Bilbo") }
            jsonPath("$.lastName") { value("Baggins") }
        }
    }

    @Test
    fun shouldPartiallyUpdateEntity() {
        val mvcResult = mockMvc.post("/people") {
            content = "{\"firstName\": \"Frodo\", \"lastName\":\"Baggins\"}"
        }.andExpect {
            status { isCreated() }
        }.andReturn()

        val location = mvcResult.response.getHeader("Location")!!

        mockMvc.patch(location) {
            content = "{\"firstName\": \"Bilbo Jr.\"}"
        }.andExpect {
            status { isNoContent() }
        }

        mockMvc.get(location).andExpect {
            status { isOk() }
            jsonPath("$.firstName") { value("Bilbo Jr.") }
            jsonPath("$.lastName") { value("Baggins") }
        }
    }

    @Test
    fun shouldDeleteEntity() {
        val mvcResult = mockMvc.post("/people") {
            content = "{ \"firstName\": \"Bilbo\", \"lastName\":\"Baggins\"}"
        }.andExpect {
            status { isCreated() }
        }.andReturn()

        val location = mvcResult.response.getHeader("Location")!!
        mockMvc.delete(location).andExpect {
            status { isNoContent() }
        }

        mockMvc.get(location).andExpect {
            status { isNotFound() }
        }
    }
}
