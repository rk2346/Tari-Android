package com.example.tarimobileas.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tarimobileas.network.LMStudioClient
import com.example.tarimobileas.network.Message
import com.example.tarimobileas.data.DatabaseProvider
import com.example.tarimobileas.data.MessageEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.tarimobileas.network.LMStudioException
import java.util.Properties

// ---------- helper to read the property ----------
private fun Application.loadDefaultHost(): String {
    return try {
        val props = Properties()
        assets.open("config.properties").use { props.load(it) }
        props.getProperty("host") ?: "http://192.168.178.107:1234"
    } catch (e: Exception) {
        "http://192.168.178.107:1234"
    }
}

class TariViewModel(application: Application) : AndroidViewModel(application) {

    //----------- Host Address ------
    val currentHost: String get() = client.host


    //---------- Whitelisted model IDs
    val WHITELIST = mapOf(
        "openai/gpt-oss-120b" to "Chat GPT 120b",
        "openai/gpt-oss-20b"  to "Chat GPT 20b",
        "llama-4-scout-17b-16e-instruct" to "Llama‑4 Scout 17b"
    )

    // ---------- UI state ----------
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    // ---------- client ----------
    private val client = LMStudioClient(
        host = application.loadDefaultHost(),
        appContext = getApplication()
    )

    val defaultHosts = listOf(
        "https://fedora.sx1zy999pu6wh4w4.myfritz.net/",
        "http://192.168.178.107:1234"
    )

    // ---------- Room ----------
    private val messageDao = DatabaseProvider.getDatabase(getApplication()).messageDao()

    init {

        viewModelScope.launch {
            messageDao.getAll().collectLatest { entities ->
                val msgs = entities.map { Message(it.role, it.content) }
                _uiState.value = _uiState.value.copy(chatHistory = msgs)
            }
        }
    }

    // ---------- actions ----------

    fun clearChat() = viewModelScope.launch {

        messageDao.clearAll()


        _uiState.value = _uiState.value.copy(chatHistory = emptyList())
    }

    fun selectModel(modelId: String) = viewModelScope.launch {

        _uiState.value = _uiState.value.copy(selectedModel = modelId)
    }


    fun sendMessage(text: String) {
        viewModelScope.launch {

            val userMsg = Message("user", text)
            insertPersisted(userMsg)


            val newHistory = _uiState.value.chatHistory + userMsg
            val replyText = client.generateText(newHistory.map { Message(it.role, it.content) })
            val assistantMsg = Message("assistant", replyText)
            insertPersisted(assistantMsg)


            _uiState.value = _uiState.value.copy(
                chatHistory = newHistory + assistantMsg
            )
        }
    }

    private suspend fun insertPersisted(msg: Message) {
        messageDao.insert(MessageEntity(role = msg.role, content = msg.content))
    }

    // ---------- export ----------
    /** Write the whole conversation to a user‑chosen Uri (plain text). */
    suspend fun exportConversation(uriString: String) {
        val uri = android.net.Uri.parse(uriString)
        val context = getApplication<Application>()
        context.contentResolver.openOutputStream(uri)?.use { out ->
            val formatted = _uiState.value.chatHistory.joinToString("\n\n") {
                "${it.role.capitalize()}: ${it.content}"
            }
            out.write(formatted.toByteArray())
        }
    }



    fun tryConnect(
        hostInput: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            // Update the client’s host first
            client.host = hostInput.trimEnd('/')
            try {
                val models = client.getModels()
                _uiState.value = _uiState.value.copy(models = models)
                onSuccess()
            } catch (e: LMStudioException) {
                onFailure(e.message ?: "Unable to connect")
            }
        }
    }}

