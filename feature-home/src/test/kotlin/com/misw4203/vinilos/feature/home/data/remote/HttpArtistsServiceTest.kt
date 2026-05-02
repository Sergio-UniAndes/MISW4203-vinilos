package com.misw4203.vinilos.feature.home.data.remote

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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

    // -- getMusicians ---------------------------------------------------------

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

    // -- getMusician ----------------------------------------------------------

    @Test
    fun getMusician_returnsParsedDtoWithAlbums_onSuccess() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "id": 100,
                      "name": "Ruben Blades",
                      "image": "https://example.com/rb.jpg",
                      "description": "Cantante panameño.",
                      "birthDate": "1948-07-16T00:00:00-05:00",
                      "albums": [
                        {
                          "id": 1,
                          "name": "Buscando America",
                          "cover": "https://example.com/ba.jpg",
                          "releaseDate": "1984-08-01T00:00:00-05:00",
                          "description": "Salsa álbum",
                          "genre": "Salsa",
                          "recordLabel": "Elektra"
                        },
                        {
                          "id": 2,
                          "name": "Maestra Vida",
                          "genre": "Salsa"
                        }
                      ],
                      "performerPrizes": []
                    }
                    """.trimIndent(),
                ),
        )

        val musician = service.getMusician(100L)

        assertNotNull(musician)
        assertEquals(100L, musician!!.id)
        assertEquals("Ruben Blades", musician.name)
        assertEquals(2, musician.albums.size)
        assertEquals(1L, musician.albums[0].id)
        assertEquals("Buscando America", musician.albums[0].name)
        assertEquals("https://example.com/ba.jpg", musician.albums[0].cover)
        assertEquals("Salsa", musician.albums[0].genre)
        assertEquals("Elektra", musician.albums[0].recordLabel)

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/musicians/100", request.path)
    }

    @Test
    fun getMusician_returnsParsedDto_withEmptyAlbums() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{ "id": 7, "name": "Solo", "description": "x" }""",
            ),
        )

        val musician = service.getMusician(7L)

        assertNotNull(musician)
        assertEquals(7L, musician!!.id)
        assertTrue(musician.albums.isEmpty())
    }

    @Test
    fun getMusician_returnsNull_on404() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))
        assertNull(service.getMusician(999L))
    }

    @Test
    fun getMusician_returnsNull_on500() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))
        assertNull(service.getMusician(1L))
    }

    @Test
    fun getMusician_returnsNull_onMalformedJson() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{ broken"))
        assertNull(service.getMusician(1L))
    }
}
