package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AssistantChatMessage
import com.example.data.ai.AssistantSender
import com.example.data.ai.FirebaseSmartBookingAssistantClient
import com.example.data.ai.SmartBookingAssistantClient
import com.example.data.model.UserProfile
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AssistantUiState {
  data object Idle : AssistantUiState

  data object Loading : AssistantUiState

  data class Success(val messageCount: Int) : AssistantUiState

  data class Error(val message: String) : AssistantUiState
}

class SmartBookingAssistantViewModel(
  private val client: SmartBookingAssistantClient = FirebaseSmartBookingAssistantClient(),
) : ViewModel() {

  private val _messages = MutableStateFlow<List<AssistantChatMessage>>(emptyList())
  val messages: StateFlow<List<AssistantChatMessage>> = _messages.asStateFlow()

  private val _uiState = MutableStateFlow<AssistantUiState>(AssistantUiState.Idle)
  val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

  private var lastAttemptedPrompt: String? = null
  private var initialPromptConsumed: Boolean = false

  fun submitInitialPromptIfNeeded(initialPrompt: String?, userProfile: UserProfile?) {
    val clean = initialPrompt?.trim().orEmpty()
    if (!initialPromptConsumed && clean.isNotEmpty()) {
      initialPromptConsumed = true
      sendMessage(clean, userProfile)
    }
  }

  fun sendMessage(rawPrompt: String, userProfile: UserProfile?) {
    val prompt = rawPrompt.trim()
    if (prompt.isEmpty() || _uiState.value is AssistantUiState.Loading) return

    val priorHistory = _messages.value
    val userMessage =
      AssistantChatMessage(
        id = "user_${UUID.randomUUID()}",
        sender = AssistantSender.USER,
        text = prompt,
      )
    _messages.value = priorHistory + userMessage
    lastAttemptedPrompt = prompt

    requestAssistantTurn(prompt = prompt, historyForCall = priorHistory, userProfile = userProfile)
  }

  fun retryLastMessage(userProfile: UserProfile?) {
    val prompt = lastAttemptedPrompt ?: return
    if (_uiState.value is AssistantUiState.Loading) return

    // History prior to the failed user message
    val historyWithoutLastUser =
      if (_messages.value.lastOrNull()?.sender == AssistantSender.USER) {
        _messages.value.dropLast(1)
      } else {
        _messages.value
      }

    requestAssistantTurn(
      prompt = prompt,
      historyForCall = historyWithoutLastUser,
      userProfile = userProfile,
    )
  }

  fun resetConversation() {
    _messages.value = emptyList()
    _uiState.value = AssistantUiState.Idle
    lastAttemptedPrompt = null
  }

  private fun requestAssistantTurn(
    prompt: String,
    historyForCall: List<AssistantChatMessage>,
    userProfile: UserProfile?,
  ) {
    viewModelScope.launch {
      _uiState.value = AssistantUiState.Loading
      try {
        val response =
          client.sendMessage(
            userMessage = prompt,
            history = historyForCall,
            userProfile = userProfile,
          )
        val assistantMsg =
          AssistantChatMessage(
            id = "ai_${UUID.randomUUID()}",
            sender = AssistantSender.ASSISTANT,
            text = response.reply,
            recommendations = response.recommendations,
            followUpPrompts = response.followUpPrompts,
          )
        _messages.value = _messages.value + assistantMsg
        _uiState.value = AssistantUiState.Success(_messages.value.size)
      } catch (e: TimeoutCancellationException) {
        Log.e(TAG, "Gemini Smart Booking Assistant request timed out", e)
        _uiState.value =
          AssistantUiState.Error(
            "Our concierge took too long to respond. Please check your connection and try again."
          )
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Log.e(TAG, "Gemini Smart Booking Assistant generation failed", e)
        _uiState.value =
          AssistantUiState.Error(
            e.localizedMessage?.takeIf { it.isNotBlank() }
              ?: "Unable to reach the Smart Booking Assistant. Please try again."
          )
      }
    }
  }

  private companion object {
    const val TAG = "SmartBookingVM"
  }
}
