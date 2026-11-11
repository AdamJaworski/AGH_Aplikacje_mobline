package com.example.apkmoblineprojekt.ai

import com.example.apkmoblineprojekt.data.model.AiSuggestion
import com.example.apkmoblineprojekt.data.model.LocationType
import com.example.apkmoblineprojekt.data.model.PlaceCategory
import com.example.apkmoblineprojekt.data.model.TaskLocation
import kotlinx.coroutines.delay

/** Podpowiedzi podzadań i miejsca dla zadania (HLD §6). Do AI trafia tylko treść zadania. */
interface AiAssistant {
    suspend fun suggest(title: String, description: String): AiSuggestion
}

/** Atrapa do czasu podpięcia LLM: dopasowuje kilka słów kluczowych. */
class FakeAiAssistant : AiAssistant {
    override suspend fun suggest(title: String, description: String): AiSuggestion {
        delay(600)
        val text = "$title $description".lowercase()
        return when {
            "żarów" in text || "lamp" in text -> suggestion(
                PlaceCategory.HARDWARE_STORE,
                "Sprawdź gwint w lampie",
                "Porównaj barwę światła",
                "Zachowaj paragon",
            )
            "paczk" in text -> suggestion(PlaceCategory.PARCEL_LOCKER, "Sprawdź kod odbioru")
            "lek" in text || "aptek" in text -> suggestion(
                PlaceCategory.PHARMACY,
                "Weź receptę",
                "Zapytaj o zamiennik",
            )
            "kupi" in text || "zakup" in text -> suggestion(
                PlaceCategory.GROCERY,
                "Zrób listę zakupów",
                "Weź torbę",
            )
            else -> AiSuggestion(listOf("Rozbij zadanie na kroki", "Ustal termin"), location = null)
        }
    }

    private fun suggestion(category: PlaceCategory, vararg subtasks: String) = AiSuggestion(
        subtasks = subtasks.toList(),
        location = TaskLocation(LocationType.CATEGORY, category = category),
    )
}
