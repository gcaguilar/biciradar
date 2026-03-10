package com.gcaguilar.bizizaragoza.backend.gemini

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.cio.EngineMain
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun main(args: Array<String>) {
  EngineMain.main(args)
}

fun Application.module(
  gateway: GeminiGateway = GeminiGateway(),
) {
  install(ContentNegotiation) {
    json(Json {
      ignoreUnknownKeys = true
      explicitNulls = false
    })
  }
  install(StatusPages) {
    exception<Throwable> { call, cause ->
      call.respond(
        HttpStatusCode.InternalServerError,
        GeminiPromptResponse(answer = cause.message ?: "Gemini proxy failure."),
      )
    }
  }

  routing {
    get("/health") {
      call.respond(mapOf("status" to "ok"))
    }
    post("/api/v1/gemini/prompt") {
      val request = call.receive<GeminiPromptRequest>()
      val response = gateway.prompt(request)
      call.respond(response.status, response.body)
    }
  }
}

class GeminiGateway(
  private val config: GeminiProxyConfiguration = GeminiProxyConfiguration.fromEnvironment(),
  private val httpClient: HttpClient = HttpClient(CIO) {
    install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
      json(Json {
        ignoreUnknownKeys = true
        explicitNulls = false
      })
    }
  },
) {
  suspend fun prompt(request: GeminiPromptRequest): ProxyResponse {
    val apiKey = config.apiKey
      ?: return ProxyResponse(
        status = HttpStatusCode.ServiceUnavailable,
        body = GeminiPromptResponse(
          answer = "Gemini no está configurado. Define GEMINI_API_KEY en el proxy.",
        ),
      )

    val renderedPrompt = buildPrompt(request)
    val payload = GeminiApiRequest(
      contents = listOf(
        GeminiContent(parts = listOf(GeminiTextPart(renderedPrompt))),
      ),
    )
    val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/${config.model}:generateContent?key=$apiKey"
    val apiResponse = httpClient.post(endpoint) {
      contentType(ContentType.Application.Json)
      setBody(payload)
    }.body<GeminiApiResponse>()

    val answer = apiResponse.candidates
      .firstOrNull()
      ?.content
      ?.parts
      ?.firstOrNull()
      ?.text
      ?.trim()
      .orEmpty()
      .ifBlank { "Gemini no devolvió contenido útil." }

    return ProxyResponse(
      status = HttpStatusCode.OK,
      body = GeminiPromptResponse(answer = answer, recommendedStationId = request.selectedStationId),
    )
  }

  internal fun buildPrompt(request: GeminiPromptRequest): String {
    val context = request.selectedStationId?.let { "Estación seleccionada: $it. " }.orEmpty()
    return buildString {
      append("Eres el asistente de Bizi Zaragoza. Responde en español, de forma breve y accionable. ")
      append(context)
      append("Petición del usuario: ")
      append(request.prompt)
    }
  }
}

data class ProxyResponse(
  val status: HttpStatusCode,
  val body: GeminiPromptResponse,
)

data class GeminiProxyConfiguration(
  val apiKey: String?,
  val model: String,
) {
  companion object {
    fun fromEnvironment(): GeminiProxyConfiguration = GeminiProxyConfiguration(
      apiKey = System.getenv("GEMINI_API_KEY"),
      model = System.getenv("GEMINI_MODEL") ?: "gemini-2.5-flash",
    )
  }
}

@Serializable
data class GeminiPromptRequest(
  val prompt: String,
  val selectedStationId: String? = null,
)

@Serializable
data class GeminiPromptResponse(
  val answer: String,
  val recommendedStationId: String? = null,
)

@Serializable
private data class GeminiApiRequest(
  val contents: List<GeminiContent>,
)

@Serializable
private data class GeminiApiResponse(
  val candidates: List<GeminiCandidate> = emptyList(),
)

@Serializable
private data class GeminiCandidate(
  val content: GeminiContent,
)

@Serializable
private data class GeminiContent(
  val parts: List<GeminiTextPart>,
)

@Serializable
private data class GeminiTextPart(
  @SerialName("text") val text: String,
)
