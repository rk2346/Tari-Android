package com.example.tarimobileas.network

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit



/**
 * Exception that mirrors the Python `LMStudioError`.
 */
class LMStudioException(message: String) : RuntimeException(message)

/**
 * Simple data class used by the UI to keep chat history.
 * (defined here for convenience – you can move it to a separate file if you wish)
 */
@Serializable
data class Message(
    val role: String,
    val content: String
)

/**
 * The client that talks to an LM‑Studio instance.
 *
 * All public functions are `suspend` and must be called from a coroutine scope
 * (e.g. viewModelScope in a ViewModel).
 */
class LMStudioClient(
    var host: String = "http://192.168.178.107:1234",
    private val appContext: Context   // needed for the optional log‑file helper
) {

    /** --------------------------------------------------------------------
     *  Configuration & helpers
     *  -------------------------------------------------------------------- */

    init {
        // Normalise the base URL once (remove trailing slash)
        host = host.trimEnd('/')
    }

    private val json = Json { ignoreUnknownKeys = true }

    // OkHttp client – shared for all requests, with a modest timeout.
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()



    /** --------------------------------------------------------------------
     *  Public API
     *  -------------------------------------------------------------------- */

    /**
     * Retrieve the list of model IDs known to the LM‑Studio server.
     *
     * @throws LMStudioException on network failure or non‑200 response.
     */
    suspend fun getModels(): List<String> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$host/v1/models")
            .header("Content-Type", "application/json")
            .get()
            .build()

        executeRequest(request) { bodyString ->
            // Expected shape: {"data":[{"id":"model‑name",...}, …]}
            val responseObj = json.decodeFromString(ModelsResponse.serializer(), bodyString)
            responseObj.data.mapNotNull { it.id }
        }
    }

    /**
     * Tell the server which model should be used for subsequent chat calls.
     *
     * The original Python version performed a GET with a JSON payload (odd but works).
     */
    fun setModel(modelName: String) {
        try {

            val jsonBody = """{"name":"$modelName"}"""
            val requestBody = RequestBody.create(
                "application/json".toMediaTypeOrNull(),
                jsonBody
            )


            val request = Request.Builder()
                .url("$host/v1/models")
                .post(requestBody)
                .header("Content-Type", "application/json")   // <‑‑ important
                .build()


            executeRequest(request) { }

        } catch (e: Exception) {

            throw LMStudioException("Failed to switch model: ${e.message}")
        }
    }


    /**
     * Send a chat history to the server and obtain the assistant’s response.
     *
     * @param history List of previous messages (role = "user" | "assistant").
     * @return The assistant reply **including** any leading newline (`\n`) just
     *         like the Python version did.
     */
    suspend fun generateText(history: List<Message>): String = withContext(Dispatchers.IO) {

        val payload = ChatRequest(
            model = "openai/gpt-oss-120b",
            messages = listOf(Message("system", SYSTEM_PROMPT)) + history,
            tools = TOOLS,                     // static definition below
            toolChoice = "auto"
        )
        val jsonBody = json.encodeToString(ChatRequest.serializer(), payload)
        val requestBody = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            jsonBody
        )

        val request = Request.Builder()
            .url("$host/v1/chat/completions")
            .post(requestBody)
            .build()

        executeRequest(request) { bodyString ->
            // Parse the OpenAI‑compatible response.
            val chatResp = json.decodeFromString(ChatResponse.serializer(), bodyString)

            val choice = chatResp.choices.firstOrNull()
                ?: throw LMStudioException("LM Studio returned an empty choices array.")

            val message = choice.message
            when {
                !message.content.isNullOrBlank() -> "\n${message.content}"
                !message.toolCalls.isNullOrEmpty() -> {

                    writeLogFile(
                        dirName = "Protokolle",
                        fileName = "${org.threeten.bp.LocalDate.now()}_Tari.txt",
                        content = history.joinToString("\n") { it.content }
                    )
                    ""
                }
                else -> throw LMStudioException("LM Studio returned an empty response.")
            }
        }
    }

    /** --------------------------------------------------------------------
     *  Private utilities
     *  -------------------------------------------------------------------- */

    /**
     * Executes the given OkHttp request, checks for HTTP errors and forwards the
     * response body to [onSuccess] if everything is fine.
     */
    private inline fun <T> executeRequest(
        request: Request,
        crossinline onSuccess: (bodyString: String) -> T
    ): T {
        try {
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string()
                    ?: throw LMStudioException("Empty response body from $host")

                if (!response.isSuccessful) {
                    throw LMStudioException(
                        "Failed request – HTTP ${response.code}: $body"
                    )
                }
                return onSuccess(body)
            }
        } catch (e: IOException) {
            // Network‑level failure
            throw LMStudioException("Network error while contacting $host: ${e.message}")
        }
    }

    /**
     * Writes a text file into the app‑private storage directory.
     *
     * Mirrors the Python `write_file("./Protokolle", ...)` side effect.
     */
    private fun writeLogFile(dirName: String, fileName: String, content: String) {
        // This runs on the caller’s dispatcher (already IO), so no extra withContext needed.
        val dir = File(appContext.filesDir, dirName)
        if (!dir.exists()) dir.mkdirs()

        val target = File(dir, fileName)
        target.writeText(content)
    }

    /** --------------------------------------------------------------------
     *  Companion objects – static data that mimics the original Python constants
     *  -------------------------------------------------------------------- */
    companion object {
        // System prompt from your Python script (kept verbatim)
        private const val SYSTEM_PROMPT = """
                Du bist ein deutsch sprechender Assistent. Du bist der 
                persönliche Assistent von Nini. Dein Name ist Tari. D
                
                You are not allowed to make any tool calls. Only give message responds to the user.
        """

        // The “tools” definition for later usage

        private val TOOLS = listOf(
            Tool(
                type = "function",
                function = FunctionSpec(
                    name = "get_files_info",
                    description = "Check if file exists and get information about a file like size and directory.",
                    parameters = Parameters(
                        type = "object",
                        properties = mapOf(
                            "working_directory" to Property(type = "string", description = "The set working directory"),
                            "directory=\".\"" to Property(type = "string", description = "The file name/path")
                        ),
                        required = listOf("working_directory")
                    )
                )
            ),
            Tool(
                type = "function",
                function = FunctionSpec(
                    name = "get_file_content",
                    description = "Check if file exists and get content of a file.",
                    parameters = Parameters(
                        type = "object",
                        properties = mapOf(
                            "working_directory" to Property(type = "string", description = "The set working directory"),
                            "file_path" to Property(type = "string", description = "The file name/path")
                        ),
                        required = listOf("working_directory", "file_path")
                    )
                )
            ),
            Tool(
                type = "function",
                function = FunctionSpec(
                    name = "run_python_file",
                    description = "Check if file exists and run python scripts/files.",
                    parameters = Parameters(
                        type = "object",
                        properties = mapOf(
                            "working_directory" to Property(type = "string", description = "The set working directory"),
                            "file_path" to Property(type = "string", description = "The file name/path"),
                            "args=[]" to Property(type = "list", description = "Optional Arguments to run together with the script.")
                        ),
                        required = listOf("working_directory", "file_path")
                    )
                )
            ),
            Tool(
                type = "function",
                function = FunctionSpec(
                    name = "write_file",
                    description = "Check if file exists and write into existing file or create new one.",
                    parameters = Parameters(
                        type = "object",
                        properties = mapOf(
                            "working_directory" to Property(type = "string", description = "The set working directory"),
                            "file_path" to Property(type = "string", description = "The file name/path"),
                            "content" to Property(type = "string", description = "Content of the file")
                        ),
                        required = listOf("working_directory", "file_path", "content")
                    )
                )
            )
        )
    }
}

/** --------------------------------------------------------------------
 *  Serialisation models – these map one‑to‑one with the JSON structures
 *  expected/returned by LM‑Studio.
 *  -------------------------------------------------------------------- */

/* ---------- Request payloads ---------- */

@Serializable
private data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    val tools: List<Tool>,
    @SerialName("tool_choice") val toolChoice: String
)

@Serializable
private data class SetModelRequest(val name: String)

/* ---------- Tool description (static) ---------- */

@Serializable
private data class Tool(
    val type: String,
    val function: FunctionSpec
)

@Serializable
private data class FunctionSpec(
    val name: String,
    val description: String,
    val parameters: Parameters
)

@Serializable
private data class Parameters(
    val type: String,
    val properties: Map<String, Property>,
    val required: List<String>
)

@Serializable
private data class Property(
    val type: String,
    val description: String
)

/* ---------- Response payloads ---------- */

@Serializable
private data class ChatResponse(
    val choices: List<Choice>
)

@Serializable
private data class Choice(
    @SerialName("message") val message: AssistantMessage
)

@Serializable
private data class AssistantMessage(
    val role: String,
    val content: String? = null,
    @SerialName("tool_calls") val toolCalls: List<ToolCall>? = null
)

@Serializable
private data class ToolCall(
    val id: String,
    val type: String,
    val function: FunctionCall
)

@Serializable
private data class FunctionCall(
    val name: String,
    val arguments: String
)

/* ---------- Models list response ---------- */

@Serializable
private data class ModelsResponse(
    val data: List<ModelInfo>
)

@Serializable
private data class ModelInfo(
    val id: String? = null,

)
