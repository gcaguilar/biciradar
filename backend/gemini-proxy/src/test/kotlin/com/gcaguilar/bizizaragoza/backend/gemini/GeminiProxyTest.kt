package com.gcaguilar.bizizaragoza.backend.gemini

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class GeminiProxyTest {
  @Test
  fun `health endpoint is reachable`() = testApplication {
    application {
      module()
    }

    val response = client.get("/health")

    assertEquals(HttpStatusCode.OK, response.status)
    assertContains(response.bodyAsText(), "ok")
  }

  @Test
  fun `proxy returns service unavailable when api key is missing`() = testApplication {
    application {
      module(
        gateway = GeminiGateway(
          config = GeminiProxyConfiguration(apiKey = null, model = "gemini-2.5-flash"),
        ),
      )
    }

    val response = client.post("/api/v1/gemini/prompt") {
      contentType(ContentType.Application.Json)
      setBody("""{"prompt":"Ruta a Plaza España"}""")
    }

    assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
    assertContains(response.bodyAsText(), "GEMINI_API_KEY")
  }
}
