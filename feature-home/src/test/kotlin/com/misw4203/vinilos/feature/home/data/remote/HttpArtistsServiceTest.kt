package com.misw4203.vinilos.feature.home.data.remote

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HttpArtistsServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var service: HttpArtistsService

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        service = HttpArtistsService(baseUrl = server.url("/").toString())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getMusicians_returnsParsedDtos_onSuccess() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    [
                      {
                        "id": 100,
                        "name": "Ruben Blades",
                        "image": "https://example.com/rb.jpg",
                        "description": "Cantante panameño.",
                        "birthDate": "1948-07-16T00:00:00-05:00",
                        "albums": [],
                        "performerPrizes": []
                      },
                      {
                        "id": 101,
                        "name": "Joan Manuel Serrat",
                        "image": "https://example.com/jms.jpg",
                        "description": "Cantautor español.",
                        "birthDate": "1943-12-27T00:00:00-05:00"
                      }
                    ]
                    """.trimIndent(),
                ),
        )

        val result = service.getMusicians()

        assertEquals(2, result.size)
        assertEquals(100L, result[0].id)
        assertEquals("Ruben Blades", result[0].name)
        assertEquals("https://example.com/rb.jpg", result[0].image)
        assertEquals(101L, result[1].id)
        assertEquals("Joan Manuel Serrat", result[1].name)

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/musicians", request.path)
    }

    @Test
    fun getMusicians_returnsEmpty_on500() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))
        assertTrue(service.getMusicians().isEmpty())
    }

    @Test
    fun getMusicians_returnsEmpty_on404() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))
        assertTrue(service.getMusicians().isEmpty())
    }

    @Test
    fun getMusicians_returnsEmpty_onMalformedJson() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("not-json"))
        assertTrue(service.getMusicians().isEmpty())
    }

    @Test
    fun getMusicians_returnsEmpty_onEmptyArray() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))
        assertTrue(service.getMusicians().isEmpty())
    }

    @Test
    fun getMusicians_skipsNonObjectArrayEntries() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """[ "garbage", { "id": 7, "name": "Valid", "description": "x" }, 42 ]""",
            ),
        )

        val result = service.getMusicians()

        assertEquals(1, result.size)
        assertEquals("Valid", result[0].name)
    }
}
