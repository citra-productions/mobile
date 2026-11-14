package com.citra.point

import android.app.Application
import android.content.Context
import androidx.compose.runtime.*
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

data class ProductCategory(val id: String, val name: String)
data class Product(
    val id: Int, val categoryId: String, val name: String,
    val price: String, val packaging: String, val desc: String
) {
    val categoryName get() = Sample.categories.first { it.id == categoryId }.name
}

/** Data contoh. Ganti dengan Retrofit (REST API) saat backend siap. */
object Sample {
    val categories = listOf(
        ProductCategory("selempang", "Selempang"), ProductCategory("plakat", "Plakat"),
        ProductCategory("souvenir", "Souvenir"), ProductCategory("pin", "Pin"),
        ProductCategory("samir", "Samir & Medali")
    )
    val products = List(12) { i ->
        val c = categories[i % categories.size]
        Product(
            i + 1, c.id, "${c.name} Akrilik ${i + 1}", "Rp50.000 - Rp75.000", "Box Mika",
            "Produk custom dengan ukiran nama & logo instansi, cocok untuk penghargaan wisuda maupun kenang-kenangan."
        )
    }
}

// ---------------------------------------------------------------- Penyimpanan internal (DataStore)
// Dipakai bersama oleh semua Activity, jadi favorit / tema / bahasa / riwayat tidak hilang antar layar
// maupun saat aplikasi ditutup. (Bisa diganti Room untuk favorit kalau dosen meminta.)
private val Context.citraStore by preferencesDataStore(name = "citra_prefs")

class Prefs(private val ctx: Context) {
    private val FAV = stringSetPreferencesKey("favorites")
    private val DARK = booleanPreferencesKey("dark")
    private val EN = booleanPreferencesKey("english")
    private val HIST = stringPreferencesKey("history")

    val favorites = ctx.citraStore.data.map { p -> (p[FAV] ?: emptySet()).mapNotNull { it.toIntOrNull() }.sorted() }
    val dark = ctx.citraStore.data.map { it[DARK] ?: false }
    val english = ctx.citraStore.data.map { it[EN] ?: false }
    val history = ctx.citraStore.data.map { p ->
        p[HIST]?.split("\n")?.filter { it.isNotBlank() } ?: listOf("piagam")
    }

    suspend fun toggleFav(id: Int) {
        ctx.citraStore.edit { p ->
            val cur = p[FAV] ?: emptySet()
            val s = id.toString()
            p[FAV] = if (s in cur) cur - s else cur + s
        }
    }
    suspend fun setDark(v: Boolean) { ctx.citraStore.edit { it[DARK] = v } }
    suspend fun setEnglish(v: Boolean) { ctx.citraStore.edit { it[EN] = v } }
    suspend fun setHistory(list: List<String>) { ctx.citraStore.edit { it[HIST] = list.joinToString("\n") } }
}

object UiPrefs {
    var favorites by mutableStateOf<List<Int>>(emptyList())
    var dark by mutableStateOf(false)
    var english by mutableStateOf(false)
    var history by mutableStateOf<List<String>>(emptyList())
    private var loaded = false

    fun ensureLoaded(prefs: Prefs) {
        if (loaded) return
        loaded = true
        runBlocking {
            favorites = prefs.favorites.first()
            dark = prefs.dark.first()
            english = prefs.english.first()
            history = prefs.history.first()
        }
    }
}

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = Prefs(app)

    var isLoading by mutableStateOf(true); private set
    var isOffline by mutableStateOf(false); private set   // set true saat gagal memuat dari API
    var query by mutableStateOf("")
    var chip by mutableStateOf<String?>(null)

    val favorites: List<Int> get() = UiPrefs.favorites
    val darkTheme: Boolean get() = UiPrefs.dark
    val english: Boolean get() = UiPrefs.english
    val history: List<String> get() = UiPrefs.history

    init {
        UiPrefs.ensureLoaded(prefs)
        viewModelScope.launch { prefs.favorites.collect { UiPrefs.favorites = it } }
        viewModelScope.launch { prefs.dark.collect { UiPrefs.dark = it } }
        viewModelScope.launch { prefs.english.collect { UiPrefs.english = it } }
        viewModelScope.launch { prefs.history.collect { UiPrefs.history = it } }
        load()
    }

    fun load() {
        viewModelScope.launch {
            isLoading = true; isOffline = false
            delay(1500)                 // simulasi request jaringan
            isLoading = false
        }
    }

    fun toggleFav(id: Int) { viewModelScope.launch { prefs.toggleFav(id) } }
    fun setDark(v: Boolean) { viewModelScope.launch { prefs.setDark(v) } }
    fun changeLanguage(v: Boolean) { viewModelScope.launch { prefs.setEnglish(v) } }

    fun addHistory(q: String) {
        if (q.isBlank()) return
        val next = (listOf(q) + history.filter { it != q }).take(5)
        viewModelScope.launch { prefs.setHistory(next) }
    }

    fun filtered() = Sample.products.filter { p ->
        (chip == null || p.categoryId == chip) &&
                (query.isBlank() || p.name.contains(query, true) || p.categoryName.contains(query, true))
    }
}
