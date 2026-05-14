package com.misw4203.vinilos.feature.home.data.remote

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HttpCollectorsServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var service: HttpCollectorsService

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        service = HttpCollectorsService(baseUrl = server.url("/").toString())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // -- getCollectors --------------------------------------------------------

    @Test
    fun getCollectors_returnsParsedDtos_onSuccess() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    [
                      {
                        "id": 1,
                        "name": "Jaime Andrés Monsalve",
                        "telephone": "3102178976",
                        "email": "j.monsalve@gmail.com",
                        "comments": [],
                        "favoritePerformers": [],
                        "collectorAlbums": []
                      },
                      {
                        "id": 2,
                        "name": "María Alejandra Palacios",
                        "telephone": "3502889087",
                        "email": "j.palacios@outlook.es",
                        "comments": [],
                        "favoritePerformers": [],
                        "collectorAlbums": [{}, {}]
                      }
                    ]
                    """.trimIndent(),
                ),
        )

        val result = service.getCollectors()

        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals("Jaime Andrés Monsalve", result[0].name)
        assertEquals("3102178976", result[0].telephone)
        assertEquals("j.monsalve@gmail.com", result[0].email)
        assertEquals(0, result[0].albumCount)
        assertEquals(2L, result[1].id)
        assertEquals(2, result[1].albumCount)

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/collectors", request.path)
    }

    @Test
    fun getCollectors_parsesAlbumCount_fromCollectorAlbumsArray() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    [
                      {
                        "id": 10,
                        "name": "Test Collector",
                        "telephone": "123",
                        "email": "test@test.com",
                        "collectorAlbums": [{}, {}, {}]
                      }
                    ]
                    """.trimIndent(),
                ),
        )

        val result = service.getCollectors()

        assertEquals(1, result.size)
        assertEquals(3, result[0].albumCount)
    }

    @Test
    fun getCollectors_returnsZeroAlbumCount_whenCollectorAlbumsMissing() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""[ { "id": 5, "name": "No Albums" } ]"""),
        )

        val result = service.getCollectors()

        assertEquals(1, result.size)
        assertEquals(0, result[0].albumCount)
    }

    @Test
    fun getCollectors_returnsEmpty_on500() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))
        assertTrue(service.getCollectors().isEmpty())
    }

    @Test
    fun getCollectors_returnsEmpty_on404() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))
        assertTrue(service.getCollectors().isEmpty())
    }

    @Test
    fun getCollectors_returnsEmpty_onMalformedJson() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("not-json"))
        assertTrue(service.getCollectors().isEmpty())
    }

    @Test
    fun getCollectors_returnsEmpty_onEmptyArray() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))
        assertTrue(service.getCollectors().isEmpty())
    }

    @Test
    fun getCollectors_skipsNonObjectArrayEntries() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """[ "garbage", { "id": 7, "name": "Valid", "telephone": "1", "email": "v@v.com" }, 42 ]""",
            ),
        )

        val result = service.getCollectors()

        assertEquals(1, result.size)
        assertEquals("Valid", result[0].name)
    }
}
