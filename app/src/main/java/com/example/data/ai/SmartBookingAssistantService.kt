package com.example.data.ai

import com.example.data.model.RitualAddOn
import com.example.data.model.SpaCatalog
import com.example.data.model.SpaTherapist
import com.example.data.model.SpaTreatment
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

enum class AssistantSender {
  USER,
  ASSISTANT,
}

data class RecommendedRitual(
  val treatment: SpaTreatment,
  val matchReason: String,
  val suggestedTherapist: SpaTherapist? = null,
  val suggestedAddOn: RitualAddOn? = null,
)

data class AssistantTurnResponse(
  val reply: String,
  val recommendations: List<RecommendedRitual> = emptyList(),
  val followUpPrompts: List<String> = emptyList(),
)

data class AssistantChatMessage(
  val id: String,
  val sender: AssistantSender,
  val text: String,
  val recommendations: List<RecommendedRitual> = emptyList(),
  val followUpPrompts: List<String> = emptyList(),
)

interface SmartBookingAssistantClient {
  suspend fun sendMessage(
    userMessage: String,
    history: List<AssistantChatMessage>,
    userProfile: UserProfile?,
  ): AssistantTurnResponse
}

class FirebaseSmartBookingAssistantClient : SmartBookingAssistantClient {

  private val model by lazy {
    val treatmentIds = SpaCatalog.treatments.map { it.id }
    val therapistIds = SpaCatalog.therapists.map { it.id }
    val addOnIds = SpaCatalog.addOns.map { it.id }

    val assistantSchema =
      Schema.obj(
        properties =
          mapOf(
            "reply" to
              Schema.string(
                description =
                  "Warm, concise SalonX concierge reply explaining why the suggested rituals fit the guest's mood, skin type, or occasion (2-4 sentences)."
              ),
            "recommendations" to
              Schema.array(
                items =
                  Schema.obj(
                    properties =
                      mapOf(
                        "treatmentId" to
                          Schema.enumeration(
                            values = treatmentIds,
                            description = "Exact ID of the matching SalonX treatment.",
                          ),
                        "matchReason" to
                          Schema.string(
                            description =
                              "Short 1-sentence reason connecting this ritual's botanicals/technique to the guest's mood or skin type."
                          ),
                        "suggestedTherapistId" to
                          Schema.enumeration(
                            values = therapistIds,
                            description = "Best matching SalonX practitioner ID.",
                          ),
                        "suggestedAddOnId" to
                          Schema.enumeration(
                            values = addOnIds,
                            description = "Optional complementary add-on enhancement ID.",
                          ),
                      ),
                    optionalProperties = listOf("suggestedTherapistId", "suggestedAddOnId"),
                  ),
                description = "Up to 3 personalized ritual recommendations from the SalonX catalog.",
              ),
            "followUpPrompts" to
              Schema.array(
                items = Schema.string(),
                description = "2 to 3 short conversational follow-up suggestions for the guest.",
              ),
          ),
        optionalProperties = listOf("followUpPrompts"),
      )

    val config = generationConfig {
      responseMimeType = "application/json"
      responseSchema = assistantSchema
      temperature = 0.7f
    }

    Firebase.ai.generativeModel(
      modelName = MODEL_NAME,
      generationConfig = config,
      systemInstruction = content { text(buildSystemCatalogPrompt()) },
    )
  }

  override suspend fun sendMessage(
    userMessage: String,
    history: List<AssistantChatMessage>,
    userProfile: UserProfile?,
  ): AssistantTurnResponse {
    val mappedHistory: List<Content> =
      history.map { msg ->
        val role = if (msg.sender == AssistantSender.USER) "user" else "model"
        content(role) { text(msg.text) }
      }

    val contextualPrompt =
      buildString {
        if (userProfile != null && history.isEmpty()) {
          append("Guest profile context — Preferred pressure: ")
          append(userProfile.preferredPressure)
          if (!userProfile.aromatherapyNote.isNullOrBlank()) {
            append(", Preferred aromatherapy note: ")
            append(userProfile.aromatherapyNote)
          }
          append("\nGuest request: ")
        }
        append(userMessage.trim())
      }

    val accumulated = StringBuilder()
    withTimeout(AI_TIMEOUT_MS) {
      val chat = model.startChat(history = mappedHistory)
      chat.sendMessageStream(contextualPrompt).collect { chunk ->
        val chunkText = chunk.text
        if (!chunkText.isNullOrEmpty()) {
          accumulated.append(chunkText)
        }
      }
    }

    val rawJson = accumulated.toString().trim()
    if (rawJson.isEmpty()) {
      throw IllegalStateException("Smart Booking Assistant returned an empty response.")
    }
    return parseAssistantJson(rawJson)
  }

  private fun parseAssistantJson(rawJson: String): AssistantTurnResponse {
    val root = JSONObject(rawJson)
    val reply =
      root.optString("reply").trim().takeIf { it.isNotEmpty() }
        ?: throw IllegalStateException("Smart Booking Assistant response missed a reply.")

    val recsArray = root.optJSONArray("recommendations")
    val recommendations =
      if (recsArray != null) {
        (0 until recsArray.length()).mapNotNull { index ->
          val item = recsArray.optJSONObject(index) ?: return@mapNotNull null
          val treatmentId = item.optString("treatmentId")
          val treatment =
            SpaCatalog.treatments.firstOrNull { it.id == treatmentId } ?: return@mapNotNull null
          val reason =
            item.optString("matchReason").trim().ifEmpty { treatment.subtitle }
          val therapistId = item.optString("suggestedTherapistId")
          val therapist = SpaCatalog.therapists.firstOrNull { it.id == therapistId }
          val addOnId = item.optString("suggestedAddOnId")
          val addOn = SpaCatalog.addOns.firstOrNull { it.id == addOnId }
          RecommendedRitual(
            treatment = treatment,
            matchReason = reason,
            suggestedTherapist = therapist,
            suggestedAddOn = addOn,
          )
        }
      } else {
        emptyList()
      }

    val followUpArray = root.optJSONArray("followUpPrompts")
    val followUps =
      if (followUpArray != null) {
        (0 until followUpArray.length())
          .mapNotNull { idx -> followUpArray.optString(idx).trim().takeIf { it.isNotEmpty() } }
          .take(3)
      } else {
        emptyList()
      }

    return AssistantTurnResponse(
      reply = reply,
      recommendations = recommendations,
      followUpPrompts = followUps,
    )
  }

  private fun buildSystemCatalogPrompt(): String {
    val treatmentsSummary =
      SpaCatalog.treatments.joinToString("\n") { t ->
        "- id=${t.id} | title=${t.title} | category=${t.category} | duration=${t.durationMinutes}m | price=$${t.priceUsd} | botanicals=${t.botanicalNotes.joinToString()} | summary=${t.description}"
      }
    val therapistsSummary =
      SpaCatalog.therapists.joinToString("\n") { th ->
        "- id=${th.id} | name=${th.name} | title=${th.title} | specialty=${th.specialty} | categories=${th.availableCategories.joinToString()}"
      }
    val addOnsSummary =
      SpaCatalog.addOns.joinToString("\n") { a ->
        "- id=${a.id} | name=${a.name} | +${a.durationExtraMinutes}m | +$${a.priceUsd} | ${a.description}"
      }

    return """
      You are the SalonX Smart Booking Assistant, a warm, knowledgeable luxury spa & skincare concierge.
      Help guests pick the best rituals, practitioners, and add-ons based on their current mood, stress level, skin type (dry, oily, sensitive, dull, combination, aging), time availability, or occasion.
      Recommend ONLY from the official SalonX catalog below and reference their exact IDs in structured fields.
      If the guest asks a clarifying question without specifying skin or mood details yet, still offer a thoughtful reply and pick the closest gentle starter ritual or 2 contrasting options, plus helpful followUpPrompts.
      Never invent treatments, prices, or specialists outside this catalog.

      TREATMENTS:
      $treatmentsSummary

      PRACTITIONERS:
      $therapistsSummary

      ADD-ONS:
      $addOnsSummary
    """.trimIndent()
  }

  private companion object {
    const val MODEL_NAME = "gemini-3.8-flash"
    const val AI_TIMEOUT_MS = 60_000L
  }
}
