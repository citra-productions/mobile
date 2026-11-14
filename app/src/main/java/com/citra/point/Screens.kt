package com.citra.point

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// GANTI dengan data asli
const val WA_URL = "https://wa.me/6281234567890"
const val IG_URL = "https://instagram.com/citraproductions"
const val MAPS_URL = "geo:0,0?q=Citra+Productions"

// ================================================================ HOME
@Composable
fun HomeScreen(vm: AppViewModel) {
    val ctx = LocalContext.current
    val skeleton = vm.isLoading && vm.products.isEmpty()
    Column(Modifier.fillMaxSize()) {
        LogoBar()
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (vm.isLoading) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Memuat kategori dan produk...", fontSize = 12.sp, lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LinearProgressIndicator(Modifier.width(72.dp).height(4.dp).clip(RoundedCornerShape(2.dp)),
                        trackColor = MaterialTheme.colorScheme.outlineVariant)
                }
            }
            if (vm.isOffline && vm.products.isNotEmpty()) OfflineBanner { vm.load() }
            if (!skeleton && vm.products.isEmpty()) {
                EmptyState(
                    Icons.Default.WifiOff, "Tidak dapat memuat data",
                    "Periksa koneksi internet kamu, lalu coba lagi.", "Coba lagi", { vm.load() },
                    Modifier.padding(top = 80.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionTitle("Kategori Produk")
                    if (skeleton) SkeletonHome() else CategoryGrid(vm.categories, null) { ctx.openCategory(it) }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        SectionTitle("Produk Unggulan")
                        Text("Lihat semua", fontSize = 12.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { ctx.openTab("search") })
                    }
                    if (skeleton) SkeletonProducts()
                    else ProductGrid(vm.products.take(4), vm.favorites, vm::toggleFav, gap = 8.dp) {
                        ctx.openProduct(it.id)
                    }
                }
            }
        }
    }
}

// ================================================================ SEARCH
@Composable
private fun CitraChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(8.dp)
    Box(
        Modifier.clip(shape).background(if (selected) cs.primary else Color.Transparent)
            .border(1.dp, cs.outline, shape).clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(label, fontSize = 16.sp, lineHeight = 24.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = if (selected) cs.onPrimary else cs.onSurface)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(vm: AppViewModel, onOpenInput: () -> Unit) {
    val ctx = LocalContext.current
    val cs = MaterialTheme.colorScheme
    var page by remember(vm.query, vm.chip) { mutableIntStateOf(1) }
    val list = vm.filtered()
    Column(Modifier.fillMaxSize()) {
        LogoBar()
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val barShape = RoundedCornerShape(12.dp)
                Row(
                    Modifier.fillMaxWidth().height(48.dp).clip(barShape).border(1.dp, cs.outline, barShape)
                        .clickable { onOpenInput() }.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Search, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(vm.query.ifBlank { "Cari produk atau kategori..." }, fontSize = 16.sp, lineHeight = 24.sp,
                        color = if (vm.query.isBlank()) cs.onSurfaceVariant else cs.onSurface)
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CitraChip("Semua", vm.chip == null) { vm.chip = null }
                    vm.categories.forEach { c -> CitraChip(c.name, vm.chip == c.id) { vm.chip = c.id } }
                }
            }
            when {
                vm.isLoading && vm.products.isEmpty() -> SkeletonProducts()
                vm.isOffline && vm.products.isEmpty() -> EmptyState(
                    Icons.Default.WifiOff, "Butuh koneksi untuk mencari",
                    "Periksa koneksi internet kamu, lalu coba lagi.", "Coba lagi", { vm.load() },
                    Modifier.padding(top = 150.dp)
                )
                list.isEmpty() -> EmptyState(
                    Icons.Default.SearchOff, "Produk tidak ditemukan",
                    "Coba kata kunci lain atau ubah kategori.", "Reset filter",
                    { vm.query = ""; vm.chip = null }, Modifier.padding(top = 150.dp)
                )
                else -> {
                    if (vm.query.isNotBlank()) {
                        Text(tr("Menampilkan ${list.size} produk", "Showing ${list.size} products"),
                            fontSize = 12.sp, lineHeight = 14.sp, color = cs.onSurfaceVariant)
                    }
                    ProductGrid(pageItems(list, page), vm.favorites, vm::toggleFav) { ctx.openProduct(it.id) }
                    Pagination(page, pageCount(list.size)) { page = it }
                }
            }
        }
    }
}

@Composable
fun SearchInputScreen(vm: AppViewModel, initial: String, onSubmit: (String) -> Unit, onBack: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    var text by remember { mutableStateOf(initial) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onBack, Modifier.size(32.dp)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali") }
            Spacer(Modifier.width(4.dp))
            OutlinedTextField(
                text, { text = it }, Modifier.weight(1f).focusRequester(focus), singleLine = true,
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("Cari produk atau kategori...", color = cs.onSurfaceVariant) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = cs.primary, unfocusedBorderColor = cs.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSubmit(text.trim()) })
            )
        }
        Spacer(Modifier.height(16.dp))
        Text("Pencarian terakhir", fontSize = 12.sp, lineHeight = 20.sp, color = cs.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        if (vm.history.isNotEmpty()) {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(cs.outline).padding(5.dp)) {
                vm.history.forEach { h ->
                    Row(
                        Modifier.fillMaxWidth().height(42.dp).clickable { onSubmit(h) }.padding(horizontal = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.History, null, Modifier.size(20.dp), tint = cs.onSurfaceVariant)
                        Spacer(Modifier.width(12.dp))
                        Text(h, fontSize = 16.sp, lineHeight = 24.sp, color = cs.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ================================================================ KATEGORI
@Composable
fun CategoryScreen(initialId: String, vm: AppViewModel) {
    val ctx = LocalContext.current
    var id by remember { mutableStateOf(initialId) }
    var page by remember(id) { mutableIntStateOf(1) }
    val list = vm.products.filter { it.categoryId == id }
    Column(Modifier.fillMaxSize()) {
        LogoBar()
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BackRow("Kembali", strong = true) { ctx.finishActivity() }
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Kategori Produk")
                CategoryGrid(vm.categories, id) { id = it }
                ProductGrid(pageItems(list, page), vm.favorites, vm::toggleFav) { ctx.openProduct(it.id) }
                if (list.isNotEmpty()) Pagination(page, pageCount(list.size)) { page = it }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ================================================================ DETAIL PRODUK
@Composable
fun ProductDetailScreen(id: String, vm: AppViewModel) {
    val ctx = LocalContext.current
    val p = vm.products.firstOrNull { it.id == id }
    if (p == null) {
        Column(Modifier.fillMaxSize()) {
            LogoBar()
            BackRow("Kembali", strong = true) { ctx.finishActivity() }
            if (vm.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(20.dp))
            else Text("Produk tidak ditemukan", Modifier.padding(20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        ProductDetailContent(p, vm)
    }
}

@Composable
private fun ProductDetailContent(p: Product, vm: AppViewModel) {
    val ctx = LocalContext.current
    val cs = MaterialTheme.colorScheme
    val n = maxOf(1, p.photos.size)
    val pager = rememberPagerState(pageCount = { n })
    val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            LogoBar()
            Column(Modifier.verticalScroll(rememberScrollState())) {
                BackRow("Kembali", strong = true) { ctx.finishActivity() }
                Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box {
                            HorizontalPager(pager) { i ->
                                ProductImage(p.photos.getOrNull(i)?.url,
                                    Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(8.dp)))
                            }
                            val alt = p.photos.getOrNull(pager.currentPage)?.alt.orEmpty().ifBlank { p.name.lowercase() }
                            Text("${pager.currentPage + 1}/$n $alt", fontSize = 12.sp, lineHeight = 20.sp,
                                color = NavyDeep, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
                                    .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 16.dp))
                        }
                        if (p.photos.size > 1) {
                            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                repeat(n) { i ->
                                    val shape = RoundedCornerShape(4.dp)
                                    ProductImage(p.photos.getOrNull(i)?.url, Modifier.size(44.dp).clip(shape)
                                        .border(if (i == pager.currentPage) 1.dp else 0.dp, cs.primary, shape)
                                        .clickable { scope.launch { pager.animateScrollToPage(i) } })
                                }
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(p.price, fontSize = 28.sp, lineHeight = 36.sp)
                        Text(p.name, fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Spesifikasi", fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold)
                        Column {
                            SpecRow("kategori:", p.categoryName)
                            HorizontalDivider(color = cs.outlineVariant)
                            SpecRow("packaging:", p.packaging)
                            HorizontalDivider(color = cs.outlineVariant)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Deskripsi", fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold)
                        Text(p.desc, fontSize = 16.sp, lineHeight = 24.sp, color = cs.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(88.dp))
                }
            }
        }
        FloatingActionButton(
            { vm.toggleFav(p.id) }, Modifier.align(Alignment.BottomEnd).padding(16.dp).size(64.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            containerColor = cs.primary, contentColor = cs.onPrimary
        ) {
            Icon(if (p.id in vm.favorites) Icons.Filled.Star else Icons.Outlined.StarBorder, "Favorit", Modifier.size(32.dp))
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) = Row(Modifier.padding(vertical = 8.dp)) {
    Text(label, Modifier.width(132.dp), fontSize = 16.sp, lineHeight = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(value, fontSize = 16.sp, lineHeight = 24.sp)
}

// ================================================================ FAVORIT
@Composable
fun FavoritesScreen(vm: AppViewModel) {
    val ctx = LocalContext.current
    var page by remember { mutableIntStateOf(1) }
    val list = vm.products.filter { it.id in vm.favorites }
    if (page > pageCount(list.size)) page = pageCount(list.size)
    Column(Modifier.fillMaxSize()) {
        LogoBar()
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle("Produk Favorite")
            if (list.isEmpty()) {
                Text("Belum ada produk yang difavoritkan", fontSize = 12.sp, lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    EmptyState(Icons.Outlined.StarBorder, "Belum ada Favorit",
                        "Ketuk ikon bintang pada produk untuk menyimpannya di sini.", "Jelajahi produk",
                        { ctx.openTab("search") })
                }
            } else {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        tr("Menampilkan ${list.size} dari ${list.size} produk yang di favoritkan",
                            "Showing ${list.size} of ${list.size} favorite products"),
                        fontSize = 12.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurface
                    )
                    ProductGrid(pageItems(list, page), vm.favorites, vm::toggleFav, gap = 8.dp) { ctx.openProduct(it.id) }
                    Pagination(page, pageCount(list.size)) { page = it }
                }
            }
        }
    }
}

// ================================================================ OTHERS
@Composable
private fun SectionLabel(t: String) =
    Text(t, fontSize = 12.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

@Composable
private fun SettingCard(
    icon: ImageVector, title: String, subtitle: String? = null,
    onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit = {}
) {
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(cs.surfaceVariant)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(20.dp)); Spacer(Modifier.width(12.dp))
            Text(title, Modifier.weight(1f), fontSize = 16.sp, lineHeight = 19.sp, fontWeight = FontWeight.SemiBold)
            Icon(Icons.Filled.ArrowRight, null, Modifier.size(20.dp))
        }
        if (subtitle != null) Text(subtitle, fontSize = 16.sp, lineHeight = 24.sp, color = cs.onSurfaceVariant)
        content()
    }
}

@Composable
private fun OptionChip(selected: Boolean, icon: ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier.height(36.dp).clip(shape).background(if (selected) cs.selectedOption else cs.surface)
            .border(1.dp, if (selected) cs.primary else cs.outline, shape)
            .clickable(onClick = onClick).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(16.dp), tint = if (selected) cs.onSurface else cs.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 14.sp, lineHeight = 16.sp, maxLines = 1,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) cs.onSurface else cs.onSurfaceVariant)
    }
}

@Composable
fun OthersScreen(vm: AppViewModel) {
    val ctx = LocalContext.current
    Column(Modifier.fillMaxSize()) {
        LogoBar()
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("FAQ")
                SettingCard(Icons.Outlined.Email, "Pertanyaan umum",
                    "Lihat jawaban cepat untuk pertanyaan yang sering ditanyakan.",
                    onClick = { ctx.startActivity(Intent(ctx, FaqActivity::class.java)) })
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("KONTAK")
                SettingCard(Icons.Outlined.Email, "Hubungi admin",
                    "Butuh bantuan lebih lanjut? Hubungi tim admin kami.",
                    onClick = { ctx.startActivity(Intent(ctx, ContactActivity::class.java)) })
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("TEMA APLIKASI")
                SettingCard(Icons.Outlined.LightMode, "Mode tampilan") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OptionChip(!vm.darkTheme, Icons.Outlined.LightMode, "Terang", Modifier.weight(1f)) { vm.setDark(false) }
                        OptionChip(vm.darkTheme, Icons.Outlined.DarkMode, "Gelap", Modifier.weight(1f)) { vm.setDark(true) }
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("BAHASA")
                SettingCard(Icons.Outlined.Language, "Bahasa aplikasi") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OptionChip(!vm.english, Icons.Outlined.Language, "Bahasa Indonesia", Modifier.weight(1f)) { vm.changeLanguage(false) }
                        OptionChip(vm.english, Icons.Outlined.Language, "English", Modifier.weight(1f)) { vm.changeLanguage(true) }
                    }
                }
            }
        }
    }
}

// ================================================================ KONTAK
@Composable
fun ContactScreen() {
    val ctx = LocalContext.current
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize()) {
        LogoBar()
        BackRow("Kembali ke Others Activity") { ctx.finishActivity() }
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedCard({ ctx.openUrl(MAPS_URL) }, Modifier.weight(1f), shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, cs.outlineVariant)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.LocationOn, null, Modifier.size(24.dp))
                        Text("Lokasi", fontSize = 16.sp, lineHeight = 24.sp)
                    }
                }
                OutlinedCard({ ctx.openUrl(IG_URL) }, Modifier.weight(1f), shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, cs.outlineVariant)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Ganti dengan ikon Instagram (export SVG dari Figma -> vector drawable)
                        Icon(Icons.Outlined.CameraAlt, null, Modifier.size(24.dp))
                        Text("Instagram", fontSize = 16.sp, lineHeight = 24.sp)
                    }
                }
            }
            WhatsAppButton({ ctx.openUrl(WA_URL) })
        }
    }
}

// ================================================================ FAQ
@Composable
fun FaqScreen(vm: AppViewModel) {
    val ctx = LocalContext.current
    val cs = MaterialTheme.colorScheme
    val defaultFaqs = listOf(
        "Bagaimana cara memesan produk?" to "Pilih produk di katalog, lalu buka halaman detail. Gunakan tombol WhatsApp untuk menanyakan ketersediaan dan mendiskusikan pesanan dengan tim Citra.",
        "Di mana saya bisa melihat harga produk?" to "Rentang harga tampil di kartu produk dan halaman detail.",
        "Bisakah produk memakai desain saya?" to "Bisa. Kirim desain Anda melalui WhatsApp.",
        "Bagaimana cara menanyakan pengiriman?" to "Hubungi admin melalui WhatsApp untuk info ongkos dan estimasi kirim.",
        "Bagaimana cara mengecek status pesanan?" to "Hubungi admin melalui WhatsApp dengan menyebutkan nama pemesan."
    )
    // FAQ diambil dari API; kalau belum ada data (offline & belum pernah dimuat) pakai daftar bawaan
    val faqs = if (vm.faqs.isNotEmpty()) vm.faqs.map { it.question to it.answer } else defaultFaqs
    Column(Modifier.fillMaxSize()) {
        LogoBar()
        Column(Modifier.verticalScroll(rememberScrollState())) {
            BackRow("Kembali ke Others Activity") { ctx.finishActivity() }
            Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text("FAQ", fontSize = 24.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Temukan jawaban untuk pertanyaan umum seputar produk dan pemesanan.",
                    fontSize = 16.sp, lineHeight = 24.sp, color = cs.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                faqs.forEachIndexed { i, (q, a) ->
                    var open by remember { mutableStateOf(i == 0) }
                    Column(Modifier.fillMaxWidth().clickable { open = !open }.padding(vertical = 16.dp, horizontal = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(q, Modifier.weight(1f), fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium)
                            Icon(if (open) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null,
                                Modifier.size(20.dp), tint = cs.primary)
                        }
                        if (open) Text(a, Modifier.padding(top = 8.dp), fontSize = 16.sp, lineHeight = 24.sp, color = cs.onSurfaceVariant)
                    }
                    HorizontalDivider(color = cs.outlineVariant)
                }
                Spacer(Modifier.height(12.dp))
                OutlinedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, cs.outline)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Belum menemukan jawaban?", fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold)
                        Text("Sampaikan pertanyaan Anda langsung melalui WhatsApp.", fontSize = 16.sp, lineHeight = 24.sp,
                            color = cs.onSurfaceVariant)
                        WhatsAppButton({ ctx.openUrl(WA_URL) })
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}