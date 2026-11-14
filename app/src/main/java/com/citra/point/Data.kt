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
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// ================================================================ Model untuk tampilan (dipakai semua layar)
data class ProductCategory(val id: String, val name: String)          // id = slug dari API
data class Photo(val url: String, val alt: String)
data class Faq(val question: String, val answer: String)
data class Product(
    val id: String,               // slug dari API
    val categoryId: String, val categoryName: String,
    val name: String, val price: String, val packaging: String, val desc: String,
    val photos: List<Photo>
)

// ================================================================ Akses data eksternal (REST API)
// Dokumentasi: https://cp-api.atalariq.dev/docs
object ApiConfig {
    const val BASE_URL = "https://cp-api.atalariq.dev/api/v1/"
}

// Bentuk JSON apa adanya dari server (DTO)
data class MetaDto(@SerializedName("last_page") val lastPage: Int? = null)
data class PageDto<T>(val data: List<T>? = null, val meta: MetaDto? = null)
data class CategoryDto(val slug: String? = null, val title: String? = null)
data class PhotoDto(val url: String? = null, @SerializedName("alt_text") val altText: String? = null, val position: Int? = null)
data class PackagingDto(val title: String? = null)
data class ProductDto(
    val slug: String? = null, val title: String? = null, val description: String? = null,
    @SerializedName("price_min") val priceMin: Long? = null,
    @SerializedName("price_max") val priceMax: Long? = null,
    val category: CategoryDto? = null,
    val packagings: List<PackagingDto>? = null,
    val photos: List<PhotoDto>? = null
)
data class FaqDto(val id: Int? = null, val question: String? = null, val answer: String? = null)

interface CitraApi {
    @GET("categories") suspend fun categories(): PageDto<CategoryDto>
    @GET("products") suspend fun products(@Query("page") page: Int, @Query("per_page") perPage: Int): PageDto<ProductDto>
    @GET("faqs") suspend fun faqs(): PageDto<FaqDto>
}

object Api {
    val service: CitraApi by lazy {
        Retrofit.Builder().baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(CitraApi::class.java)
    }
}

private fun rupiah(n: Long) = "Rp" + n.toString().reversed().chunked(3).joinToString(".").reversed()

private fun formatPrice(min: Long?, max: Long?): String = when {
    min == null && max == null -> "Hubungi kami"
    min == null || max == null || min == max -> rupiah(min ?: max!!)
    else -> "${rupiah(min)} - ${rupiah(max)}"
}

private fun ProductDto.toProduct() = Product(
    id = slug.orEmpty(),
    categoryId = category?.slug.orEmpty(), categoryName = category?.title.orEmpty(),
    name = title.orEmpty(), price = formatPrice(priceMin, priceMax),
    packaging = packagings.orEmpty().mapNotNull { it.title }.joinToString(", ").ifBlank { "-" },
    desc = description.orEmpty(),
    photos = photos.orEmpty().sortedBy { it.position ?: 0 }
        .mapNotNull { p -> p.url?.let { Photo(it, p.altText.orEmpty()) } }
)

// ================================================================ Penyimpanan internal (DataStore)
private val Context.citraStore by preferencesDataStore(name = "citra_prefs")

class Prefs(private val ctx: Context) {
    private val FAV = stringSetPreferencesKey("favorites")
    private val DARK = booleanPreferencesKey("dark")
    private val EN = booleanPreferencesKey("english")
    private val HIST = stringPreferencesKey("history")
    private val CACHE_CAT = stringPreferencesKey("cache_categories")
    private val CACHE_PROD = stringPreferencesKey("cache_products")
    private val CACHE_FAQ = stringPreferencesKey("cache_faqs")

    val favorites = ctx.citraStore.data.map { p -> (p[FAV] ?: emptySet()).toList().sorted() }
    val dark = ctx.citraStore.data.map { it[DARK] ?: false }
    val english = ctx.citraStore.data.map { it[EN] ?: false }
    val history = ctx.citraStore.data.map { p ->
        p[HIST]?.split("\n")?.filter { it.isNotBlank() } ?: listOf("piagam")
    }

    suspend fun toggleFav(id: String) {
        ctx.citraStore.edit { p ->
            val cur = p[FAV] ?: emptySet()
            p[FAV] = if (id in cur) cur - id else cur + id
        }
    }
    suspend fun setDark(v: Boolean) { ctx.citraStore.edit { it[DARK] = v } }
    suspend fun setEnglish(v: Boolean) { ctx.citraStore.edit { it[EN] = v } }
    suspend fun setHistory(list: List<String>) { ctx.citraStore.edit { it[HIST] = list.joinToString("\n") } }

    // Cache data dari API, dipakai saat offline
    suspend fun loadCache(): Triple<String?, String?, String?> {
        val p = ctx.citraStore.data.first()
        return Triple(p[CACHE_CAT], p[CACHE_PROD], p[CACHE_FAQ])
    }
    suspend fun saveCache(cat: String, prod: String, faq: String) {
        ctx.citraStore.edit { it[CACHE_CAT] = cat; it[CACHE_PROD] = prod; it[CACHE_FAQ] = faq }
    }
}

// ================================================================ Data bersama semua Activity
/** Katalog dari API. Disimpan di level aplikasi, jadi Activity baru langsung punya datanya (tanpa loading ulang). */
object Catalog {
    var categories by mutableStateOf<List<ProductCategory>>(emptyList())
    var products by mutableStateOf<List<Product>>(emptyList())
    var faqs by mutableStateOf<List<Faq>>(emptyList())
    var isLoading by mutableStateOf(false)
    var isOffline by mutableStateOf(false)
    private var fetched = false

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val gson = Gson()
    private val categoryOrder = listOf("selempang", "plakat", "souvenir", "pin", "samir-medali")

    /** Baca cache dari penyimpanan internal. */
    suspend fun restore(prefs: Prefs) {
        val (c, p, f) = prefs.loadCache()
        try {
            c?.let { categories = gson.fromJson(it, Array<ProductCategory>::class.java).toList() }
            p?.let { products = gson.fromJson(it, Array<Product>::class.java).toList() }
            f?.let { faqs = gson.fromJson(it, Array<Faq>::class.java).toList() }
        } catch (_: Exception) { }
    }

    /** Ambil data terbaru dari API. force = true dipakai tombol "Coba lagi". */
    fun load(prefs: Prefs, force: Boolean = false) {
        if (isLoading || (fetched && !force)) return
        isLoading = true; isOffline = false
        scope.launch {
            try {
                val prods = fetchAllProducts()
                val used = prods.map { it.categoryId }.toSet()
                val cats = Api.service.categories().data.orEmpty()
                    .mapNotNull { d -> if (d.slug != null && d.title != null) ProductCategory(d.slug, d.title) else null }
                    .filter { it.id in used }                       // sembunyikan kategori kosong (mis. "Tes")
                    .sortedBy { categoryOrder.indexOf(it.id).let { i -> if (i < 0) 99 else i } }
                val faqList = try {
                    Api.service.faqs().data.orEmpty().mapNotNull { d ->
                        if (d.question != null && d.answer != null) Faq(d.question, d.answer) else null
                    }
                } catch (_: Exception) { faqs }
                categories = cats; products = prods; faqs = faqList
                fetched = true
                prefs.saveCache(gson.toJson(cats), gson.toJson(prods), gson.toJson(faqList))
            } catch (e: Exception) {
                isOffline = true                                     // gagal: tampilkan data tersimpan + banner offline
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun fetchAllProducts(): List<Product> {
        val out = mutableListOf<Product>()
        var page = 1
        while (true) {
            val res = Api.service.products(page, 50)
            out += res.data.orEmpty().map { it.toProduct() }
            if (page >= (res.meta?.lastPage ?: 1)) break
            page++
        }
        return out
    }
}

/** Tema, bahasa, favorit, dan riwayat. Dibaca sekali di awal supaya perpindahan layar tidak berkedip. */
object UiPrefs {
    var favorites by mutableStateOf<List<String>>(emptyList())
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
            Catalog.restore(prefs)
        }
    }
}

// ================================================================ ViewModel
class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = Prefs(app)

    var query by mutableStateOf("")
    var chip by mutableStateOf<String?>(null)

    val isLoading: Boolean get() = Catalog.isLoading
    val isOffline: Boolean get() = Catalog.isOffline
    val categories: List<ProductCategory> get() = Catalog.categories
    val products: List<Product> get() = Catalog.products
    val faqs: List<Faq> get() = Catalog.faqs

    val favorites: List<String> get() = UiPrefs.favorites
    val darkTheme: Boolean get() = UiPrefs.dark
    val english: Boolean get() = UiPrefs.english
    val history: List<String> get() = UiPrefs.history

    init {
        UiPrefs.ensureLoaded(prefs)
        viewModelScope.launch { prefs.favorites.collect { UiPrefs.favorites = it } }
        viewModelScope.launch { prefs.dark.collect { UiPrefs.dark = it } }
        viewModelScope.launch { prefs.english.collect { UiPrefs.english = it } }
        viewModelScope.launch { prefs.history.collect { UiPrefs.history = it } }
        Catalog.load(prefs)
    }

    /** Dipanggil tombol "Coba lagi". */
    fun load() { Catalog.load(prefs, force = true) }

    fun toggleFav(id: String) { viewModelScope.launch { prefs.toggleFav(id) } }
    fun setDark(v: Boolean) { viewModelScope.launch { prefs.setDark(v) } }
    fun changeLanguage(v: Boolean) { viewModelScope.launch { prefs.setEnglish(v) } }

    fun addHistory(q: String) {
        if (q.isBlank()) return
        val next = (listOf(q) + history.filter { it != q }).take(5)
        viewModelScope.launch { prefs.setHistory(next) }
    }

    fun filtered() = products.filter { p ->
        (chip == null || p.categoryId == chip) &&
                (query.isBlank() || p.name.contains(query, true) || p.categoryName.contains(query, true))
    }
}