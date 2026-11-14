package com.citra.point

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import android.graphics.drawable.ColorDrawable

const val EXTRA_PRODUCT_ID = "product_id"
const val EXTRA_CATEGORY_ID = "category_id"
const val EXTRA_QUERY = "query"

fun Context.findActivity(): Activity? {
    var c = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}

fun Context.finishActivity() { findActivity()?.finish() }

// ---- Explicit Intent antar-Activity ----
fun Context.openProduct(id: Int) =
    startActivity(Intent(this, ProductDetailActivity::class.java).putExtra(EXTRA_PRODUCT_ID, id))

fun Context.openCategory(id: String) =
    startActivity(Intent(this, CategoryListActivity::class.java).putExtra(EXTRA_CATEGORY_ID, id))

/** Pindah tab navbar bawah. Tumpukan selalu: Home lalu tab yang dipilih. */
fun Context.openTab(route: String) {
    // 1. Kembali ke Home dan tutup semua Activity di atasnya
    startActivity(
        Intent(this, MainActivity::class.java).addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NO_ANIMATION
        )
    )
    // 2. Kalau tab yang dipilih bukan Home, buka di atas Home
    val target = when (route) {
        "search" -> SearchActivity::class.java
        "favorites" -> FavoriteActivity::class.java
        "others" -> OthersActivity::class.java
        else -> null
    }
    if (target != null) {
        startActivity(Intent(this, target).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
    }
}

/** Kerangka tiap Activity: Scaffold + navbar bawah dengan tab [selected] aktif. */
@Composable
fun CitraScaffold(selected: String, content: @Composable () -> Unit) {
    val ctx = LocalContext.current
    Scaffold(
        contentWindowInsets = WindowInsets(0),   // status bar ditangani LogoBar
        bottomBar = { CitraNavBar(selected) { ctx.openTab(it) } }
    ) { pad -> Box(Modifier.fillMaxSize().padding(pad)) { content() } }
}

fun ComponentActivity.paintWindow() {
    UiPrefs.ensureLoaded(Prefs(applicationContext))
    window.setBackgroundDrawable(ColorDrawable(if (UiPrefs.dark) 0xFF0F1320.toInt() else 0xFFFFFFFF.toInt()))
}

/** Dipakai semua Activity: edge-to-edge, ViewModel, tema, dan navbar. */
fun ComponentActivity.setCitraContent(tab: String, content: @Composable (AppViewModel) -> Unit) {
    enableEdgeToEdge()
    paintWindow()
    setContent {
        val vm: AppViewModel = viewModel()
        CitraTheme(vm.darkTheme, vm.english) { CitraScaffold(tab) { content(vm) } }
    }
}
