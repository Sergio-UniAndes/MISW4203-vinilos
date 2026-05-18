package com.misw4203.vinilos.feature.home.data.remote

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HttpCommentsServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var service: HttpCommentsService

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        service = HttpCommentsService(baseUrl = server.url("/").toString())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // -- getComments ----------------------------------------------------------

    @Test
    fun getComments_returnsParsedDtos_onSuccess() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                [
                  { "id": 1, "description": "Amazing record", "rating": 5 },
                  { "id": 2, "description": "Solid debut", "rating": 4 }
                ]
                """.trimIndent(),
            ),
        )

        val comments = service.getComments("42")

        assertEquals(2, comments.size)
        assertEquals(1L, comments[0].id)
        assertEquals("Amazing record", comments[0].description)
        assertEquals(5, comments[0].rating)

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/albums/42/comments", request.path)
    }

    @Test
    fun getComments_returnsEmpty_onEmptyArray() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))
        assertTrue(service.getComments("1").isEmpty())
    }

    @Test
    fun getComments_returnsEmpty_on404() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))
        assertTrue(service.getComments("1").isEmpty())
    }

    @Test
    fun getComments_returnsEmpty_onMalformedJson() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("not-json"))
        assertTrue(service.getComments("1").isEmpty())
    }

    @Test
    fun getComments_skipsNonObjectEntries() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """[ "garbage", { "id": 7, "description": "Valid", "rating": 3 }, 42 ]""",
            ),
        )

        val comments = service.getComments("1")

        assertEquals(1, comments.size)
        assertEquals("Valid", comments[0].description)
    }

    // -- postComment ----------------------------------------------------------

    @Test
    fun postComment_sendsExpectedBody_andParsesResponse() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                {
                  "id": 99,
                  "description": "It is an amazing album",
                  "rating": 5,
                  "collector": { "id": 1, "name": "Jaime" },
                  "album": { "id": 8 }
                }
                """.trimIndent(),
            ),
        )

        val result = service.postComment(
            albumId = "8",
            description = "It is an amazing album",
            rating = 5,
            collectorId = 1L,
        )

        assertNotNull(result)
        assertEquals(99L, result?.id)
        assertEquals("It is an amazing album", result?.description)
        assertEquals(5, result?.rating)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/albums/8/comments", request.path)
        assertTrue(request.getHeader("Content-Type")?.contains("application/json") == true)

        val sentBody = JSONObject(request.body.readUtf8())
        assertEquals("It is an amazing album", sentBody.getString("description"))
        assertEquals(5, sentBody.getInt("rating"))
        assertEquals(1L, sentBody.getJSONObject("collector").getLong("id"))
    }

    @Test
    fun postComment_returnsNull_on500() = runTest {
        server.enqueue(MockResponse().setResponseCode(500).setBody("""{ "message": "boom" }"""))

        val result = service.postComment("1", "x", rating = 3, collectorId = 1L)

        assertNull(result)
    }

    @Test
    fun postComment_returnsNull_on404_albumNotFound() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(404).setBody(
                """{ "statusCode": 404, "message": "The album with the given id was not found" }""",
            ),
        )

        val result = service.postComment("0", "x", rating = 3, collectorId = 1L)

        assertNull(result)
    }
}
