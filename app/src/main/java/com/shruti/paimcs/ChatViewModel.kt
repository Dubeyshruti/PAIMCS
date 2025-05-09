package com.shruti.paimcs

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var llmInference: LlmInference
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    private val modelAssetName = "gemma3-1B-it-int4.task"
    private var isModelReady = false

    fun initGemma() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val modelFile = File(getApplication<Application>().filesDir, modelAssetName)
                if (!modelFile.exists()) {
                    getApplication<Application>().assets.open(modelAssetName).use { input ->
                        modelFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                Log.d("GemmaDebug", "Model file path: ${modelFile.absolutePath}")
                Log.d("GemmaDebug", "Model exists: ${modelFile.exists()}, size: ${modelFile.length()}")

                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile.absolutePath)
                    .setMaxTopK(64)
                    .build()

                llmInference = LlmInference.createFromOptions(getApplication(), options)
                isModelReady = true

                _chatMessages.value += ChatMessage("Welcome!", false)

            } catch (e: Exception) {
                Log.e("GemmaDebug", "Model init failed", e)
                _chatMessages.value += ChatMessage("❌ Model failed to load: ${e.message}", false)
            }
        }
    }

    fun sendMessage(prompt: String) {
        viewModelScope.launch {
            if (!isModelReady) {
                _chatMessages.value += ChatMessage("⚠️ Not ready yet!", false)
                return@launch
            }

            _chatMessages.value += ChatMessage(prompt, true)
            _chatMessages.value += ChatMessage("💬 Thinking...", false)

            try {
                val response = withContext(Dispatchers.IO) {
                    llmInference.generateResponse(prompt)
                }

                _chatMessages.value = _chatMessages.value.dropLast(1) + ChatMessage(response, false)

            } catch (e: Exception) {
                Log.e("GemmaDebug", "LLM inference failed", e)
                _chatMessages.value = _chatMessages.value.dropLast(1) + ChatMessage("❌ Error: ${e.message}", false)
            }
        }
    }
}