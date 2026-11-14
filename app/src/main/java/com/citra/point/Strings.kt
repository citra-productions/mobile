package com.citra.point

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

/** true = tampilan Bahasa Inggris. Diisi oleh CitraTheme(...). */
val LocalEnglish = compositionLocalOf { false }

/** Untuk teks yang mengandung angka/variabel: tr("Menampilkan $n produk", "Showing $n products") */
@Composable
fun tr(id: String, en: String): String = if (LocalEnglish.current) en else id

/**
 * Text() milik kita sendiri. Karena berada di package yang sama, semua pemanggilan Text("...")
 * di file lain otomatis lewat sini. Kalau bahasa = English dan teksnya ada di kamus [Translations],
 * teks diganti. Teks yang tidak ada di kamus (nama produk, deskripsi, dll.) tampil apa adanya.
 */
@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current
) {
    val shown = if (LocalEnglish.current) Translations[text] ?: text else text
    androidx.compose.material3.Text(
        text = shown, modifier = modifier, color = color, fontSize = fontSize, fontStyle = fontStyle,
        fontWeight = fontWeight, fontFamily = fontFamily, letterSpacing = letterSpacing,
        textDecoration = textDecoration, textAlign = textAlign, lineHeight = lineHeight,
        overflow = overflow, softWrap = softWrap, maxLines = maxLines, minLines = minLines,
        onTextLayout = onTextLayout ?: {}, style = style
    )
}

/** Kamus Indonesia -> English. Tambah baris baru di sini kalau ada teks baru. */
val Translations: Map<String, String> = mapOf(
    // Navbar & umum
    "Favorit" to "Favorites",
    "Kembali" to "Back",
    "Kembali ke Others Activity" to "Back to Others",
    "Coba lagi" to "Try again",
    "Semua" to "All",
    "Memuat data..." to "Loading data...",
    "Chat melalui WhatsApp" to "Chat via WhatsApp",
    "kategori:" to "category:",
    // Banner offline
    "Mode offline · Menampilkan data tersimpan" to "Offline mode · Showing saved data",
    "Belum dapat memuat pembaruan." to "Unable to load updates yet.",
    // Home
    "Memuat kategori dan produk..." to "Loading categories and products...",
    "Kategori Produk" to "Product Categories",
    "Produk Unggulan" to "Featured Products",
    "Lihat semua" to "See all",
    // Search
    "Cari produk atau kategori..." to "Search products or categories...",
    "Pencarian terakhir" to "Recent searches",
    "Butuh koneksi untuk mencari" to "Connection needed to search",
    "Periksa koneksi internet kamu, lalu coba lagi." to "Check your internet connection, then try again.",
    "Produk tidak ditemukan" to "No products found",
    "Coba kata kunci lain atau ubah kategori." to "Try another keyword or change the category.",
    // Detail produk
    "Spesifikasi" to "Specifications",
    "Deskripsi" to "Description",
    // Favorit
    "Produk Favorite" to "Favorite Products",
    "Belum ada produk yang difavoritkan" to "No favorite products yet",
    "Belum ada Favorit" to "No Favorites Yet",
    "Ketuk ikon bintang pada produk untuk menyimpannya di sini." to "Tap the star icon on a product to save it here.",
    "Jelajahi produk" to "Browse products",
    // Others
    "KONTAK" to "CONTACT",
    "TEMA APLIKASI" to "APP THEME",
    "BAHASA" to "LANGUAGE",
    "Pertanyaan umum" to "Frequently asked questions",
    "Lihat jawaban cepat untuk pertanyaan yang sering ditanyakan." to "See quick answers to common questions.",
    "Hubungi admin" to "Contact admin",
    "Butuh bantuan lebih lanjut? Hubungi tim admin kami." to "Need more help? Contact our admin team.",
    "Mode tampilan" to "Display mode",
    "Terang" to "Light",
    "Gelap" to "Dark",
    "Bahasa aplikasi" to "App language",
    // Kontak
    "Lokasi" to "Location",
    // FAQ
    "Temukan jawaban untuk pertanyaan umum seputar produk dan pemesanan." to "Find answers to common questions about products and orders.",
    "Bagaimana cara memesan produk?" to "How do I order a product?",
    "Pilih produk di katalog, lalu buka halaman detail. Gunakan tombol WhatsApp untuk menanyakan ketersediaan dan mendiskusikan pesanan dengan tim Citra." to
            "Pick a product in the catalog, then open its detail page. Use the WhatsApp button to ask about availability and discuss your order with the Citra team.",
    "Di mana saya bisa melihat harga produk?" to "Where can I see product prices?",
    "Rentang harga tampil di kartu produk dan halaman detail." to "The price range is shown on the product card and the detail page.",
    "Bisakah produk memakai desain saya?" to "Can the product use my own design?",
    "Bisa. Kirim desain Anda melalui WhatsApp." to "Yes. Send your design via WhatsApp.",
    "Bagaimana cara menanyakan pengiriman?" to "How do I ask about shipping?",
    "Hubungi admin melalui WhatsApp untuk info ongkos dan estimasi kirim." to "Contact the admin via WhatsApp for shipping cost and delivery estimates.",
    "Bagaimana cara mengecek status pesanan?" to "How do I check my order status?",
    "Hubungi admin melalui WhatsApp dengan menyebutkan nama pemesan." to "Contact the admin via WhatsApp and mention the name on the order.",
    "Belum menemukan jawaban?" to "Can't find an answer?",
    "Sampaikan pertanyaan Anda langsung melalui WhatsApp." to "Send your question directly via WhatsApp."
)