package com.malenia.app.ai

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Ejemplo de integración con OpenAI API para tu startup
 * Configura tu API key en un archivo seguro (no en el código)
 */

// Data classes para OpenAI API
data class OpenAIRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<Message>,
    val max_tokens: Int = 150,
    val temperature: Double = 0.7
)

data class Message(
    val role: String, // "user", "assistant", "system"
    val content: String
)

data class OpenAIResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)

// Interface para la API
interface OpenAIService {
    @POST("v1/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: OpenAIRequest
    ): OpenAIResponse
}

// Cliente HTTP configurado
object AIClient {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openai.com/")
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val openAIService: OpenAIService = retrofit.create(OpenAIService::class.java)
}

// Repository para manejar las llamadas a IA
class AIRepository {
    private val apiKey = "TU_API_KEY_AQUÍ" // ¡Usar variables de entorno en producción!
    
    suspend fun generateResponse(userMessage: String): String {
        return try {
            val request = OpenAIRequest(
                messages = listOf(
                    Message("system", "Eres un asistente inteligente para una startup de soluciones con IA."),
                    Message("user", userMessage)
                )
            )
            
            val response = AIClient.openAIService.createChatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )
            
            response.choices.firstOrNull()?.message?.content ?: "Error al generar respuesta"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
    
    suspend fun analyzeText(text: String): String {
        val request = OpenAIRequest(
            messages = listOf(
                Message("system", "Analiza el siguiente texto y proporciona insights útiles."),
                Message("user", text)
            )
        )
        
        return try {
            val response = AIClient.openAIService.createChatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )
            response.choices.firstOrNull()?.message?.content ?: "Error en análisis"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}

/**
 * Uso en tu Activity o Composable:
 * 
 * class MainActivity : ComponentActivity() {
 *     private val aiRepository = AIRepository()
 *     
 *     private fun processUserInput(userText: String) {
 *         lifecycleScope.launch {
 *             val response = aiRepository.generateResponse(userText)
 *             // Mostrar respuesta en la UI
 *         }
 *     }
 * }
 */