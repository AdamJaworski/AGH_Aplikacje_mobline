package com.example.apkmoblineprojekt

import android.content.Context
import com.example.apkmoblineprojekt.ai.AiAssistant
import com.example.apkmoblineprojekt.ai.FakeAiAssistant
import com.example.apkmoblineprojekt.data.CalendarSyncingTaskRepository
import com.example.apkmoblineprojekt.data.FakePlaceSearchRepository
import com.example.apkmoblineprojekt.data.InMemoryNoteRepository
import com.example.apkmoblineprojekt.data.InMemoryPlaceRepository
import com.example.apkmoblineprojekt.data.DataStoreSettingsRepository
import com.example.apkmoblineprojekt.data.InMemoryTaskRepository
import com.example.apkmoblineprojekt.data.NoteRepository
import com.example.apkmoblineprojekt.data.PlaceRepository
import com.example.apkmoblineprojekt.data.PlaceSearchRepository
import com.example.apkmoblineprojekt.data.SampleData
import com.example.apkmoblineprojekt.data.SettingsRepository
import com.example.apkmoblineprojekt.data.SharedPrefsDeletedEventStore
import com.example.apkmoblineprojekt.data.TaskRepository
import com.example.apkmoblineprojekt.location.GeocodingRepository
import com.example.apkmoblineprojekt.location.GeofenceHelper
import com.example.apkmoblineprojekt.location.GeofenceStore
import com.example.apkmoblineprojekt.location.GeofencingTaskRepository
import com.example.apkmoblineprojekt.system.AndroidDeviceCalendar
import com.example.apkmoblineprojekt.system.AndroidPermissionChecker
import com.example.apkmoblineprojekt.system.DeviceCalendar
import com.example.apkmoblineprojekt.system.PermissionChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Ręczne DI (HLD §4). Ekrany znają tylko interfejsy – żeby podpiąć prawdziwą funkcję,
 * wystarczy podmienić implementację tutaj:
 * - zadania, notatki, miejsca: in-memory → Room,
 * - ustawienia: już w DataStore,
 * - wyszukiwanie miejsc: dane przykładowe → Overpass API,
 * - AI: reguły na słowach kluczowych → AiClient (LLM).
 *
 * Kalendarz telefonu (CalendarContract) jest już prawdziwy: [taskRepository] to dekorator,
 * który synchronizuje zadania z wydarzeniami w telefonie, a na zewnątrz – dekorator
 * rejestrujący strefy geofence dla zadań z miejscem (HLD §5).
 */
class AppContainer(context: Context) {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val settingsRepository: SettingsRepository = DataStoreSettingsRepository(context.applicationContext)
    val deviceCalendar: DeviceCalendar = AndroidDeviceCalendar(context.applicationContext)
    val placeRepository: PlaceRepository = InMemoryPlaceRepository(SampleData.places())
    val geocodingRepository = GeocodingRepository(context.applicationContext)
    val geofenceStore = GeofenceStore(context.applicationContext)
    val geofenceHelper = GeofenceHelper(context.applicationContext, geofenceStore)
    val taskRepository: TaskRepository = GeofencingTaskRepository(
        delegate = CalendarSyncingTaskRepository(
            local = InMemoryTaskRepository(SampleData.tasks()),
            deviceCalendar = deviceCalendar,
            settingsRepository = settingsRepository,
            deletedEvents = SharedPrefsDeletedEventStore(context.applicationContext),
        ).also { it.start(appScope) },
        placeRepository = placeRepository,
        geocodingRepository = geocodingRepository,
        store = geofenceStore,
        helper = geofenceHelper,
    )
    val noteRepository: NoteRepository = InMemoryNoteRepository(SampleData.notes())
    val placeSearchRepository: PlaceSearchRepository = FakePlaceSearchRepository()
    val aiAssistant: AiAssistant = FakeAiAssistant()
    val permissionChecker: PermissionChecker = AndroidPermissionChecker(context.applicationContext)
}
