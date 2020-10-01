import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPatch
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import com.kiko.flat.dto.SuccessfulRequestDto
import com.kiko.init
import io.javalin.Javalin
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import java.time.LocalDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiTest {
    private lateinit var app: Javalin

    @BeforeEach
    internal fun setUp() {
        Thread.sleep(1000)
        app = init(1234)
        FuelManager.instance.basePath = "http://localhost:${app.port()}/"
    }

    @AfterEach
    internal fun tearDown() {
        app.stop()
    }

    @Test
    fun `should be success result for new request`() {
        val (_, response, result) = "/request"
            .httpPost()
            .header(Headers.CONTENT_TYPE, "application/json")
            .jsonBody(
                "{\n" +
                        "  \"flatId\": 1,\n" +
                        "  \"tenantId\": 11,\n" +
                        "  \"date\": \"2020-10-05T12:20:00\"\n" +
                        "}"
            ).response()

        val res = ObjectMapper().readValue(response.data, Map::class.java)

        var arr = (res["start"].toString() + " " + res["end"].toString()).split(" ")

        assertEquals(1, res["flatId"])
        assertEquals(LocalDateTime.parse("2020-10-05T12:20:00"), LocalDateTime.parse(arr[0] + "T" + arr[1]))
        assertEquals(LocalDateTime.parse("2020-10-05T12:40:00"), LocalDateTime.parse(arr[2] + "T" + arr[3]))
    }

    @Test
    fun `should be bad request on wrong date`() {
        val (_, response, result) = "/cancel"
            .httpPatch()
            .header(Headers.CONTENT_TYPE, "application/json")
            .jsonBody(
                "{\n" +
                        "  \"flatId\": 1,\n" +
                        "  \"tenantId\": 11,\n" +
                        "  \"date\": \"2020-10-05T12:21:00\"\n" +
                        "}\n"
            ).response()
        assertEquals(response.statusCode, 400)
    }

    @Test
    fun `should be successful on accept requested`() {
        val (_, response1, _) = "/request"
            .httpPost()
            .header(Headers.CONTENT_TYPE, "application/json")
            .jsonBody(
                "{\n" +
                        "  \"flatId\": 1,\n" +
                        "  \"tenantId\": 11,\n" +
                        "  \"date\": \"2020-10-05T12:20:00\"\n" +
                        "}"
            ).response()

        val (_, response2, _) = "/approve"
            .httpPut()
            .header(Headers.CONTENT_TYPE, "application/json")
            .jsonBody(
                "{\n" +
                        "  \"flatId\": 1,\n" +
                        "  \"currentTenantId\": 1,\n" +
                        "  \"date\": \"2020-10-05T12:20:00\"\n" +
                        "}"
            ).response()

        assertEquals(response1.statusCode, 200)
        assertEquals(response2.statusCode, 200)
    }

    @Test
    fun `should be exception on request rejected`() {
        val (_, response1, _) = "/request"
            .httpPost()
            .header(Headers.CONTENT_TYPE, "application/json")
            .jsonBody(
                "{\n" +
                        "  \"flatId\": 1,\n" +
                        "  \"tenantId\": 11,\n" +
                        "  \"date\": \"2020-10-05T12:20:00\"\n" +
                        "}"
            ).response()

        val (_, response2, _) = "/reject"
            .httpPut()
            .header(Headers.CONTENT_TYPE, "application/json")
            .jsonBody(
                "{\n" +
                        "  \"flatId\": 1,\n" +
                        "  \"currentTenantId\": 1,\n" +
                        "  \"date\": \"2020-10-05T12:20:00\"\n" +
                        "}"
            ).response()

        val (_, response3, _) = "/request"
            .httpPost()
            .header(Headers.CONTENT_TYPE, "application/json")
            .jsonBody(
                "{\n" +
                        "  \"flatId\": 1,\n" +
                        "  \"tenantId\": 11,\n" +
                        "  \"date\": \"2020-10-05T12:20:00\"\n" +
                        "}"
            ).response()

        assertEquals(response1.statusCode, 200)
        assertEquals(response2.statusCode, 200)
        assertEquals(response3.statusCode, 400)
    }

    @Test
    fun `async data mutations test`() {
        runBlocking {
            val httpAsync = async {
                "/request"
                    .httpPost()
                    .header(Headers.CONTENT_TYPE, "application/json")
                    .jsonBody(
                        "{\n" +
                                "  \"flatId\": 1,\n" +
                                "  \"tenantId\": 11,\n" +
                                "  \"date\": \"2020-10-05T12:20:00\"\n" +
                                "}"
                    ).response()
            }
            val httpAsync1 = async {
                "/request"
                    .httpPost()
                    .header(Headers.CONTENT_TYPE, "application/json")
                    .jsonBody(
                        "{\n" +
                                "  \"flatId\": 1,\n" +
                                "  \"tenantId\": 11,\n" +
                                "  \"date\": \"2020-10-05T12:20:00\"\n" +
                                "}"
                    ).response()
            }
            assertEquals(httpAsync.await().second.statusCode, 200)
            assertEquals(httpAsync1.await().second.statusCode, 400)
        }
    }
}