package com.citra.point

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun Context.openUrl(url: String) = startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

fun <T> pageItems(list: List<T>, page: Int, size: Int = 4) = list.drop((page - 1) * size).take(size)
fun pageCount(n: Int, size: Int = 4) = maxOf(1, (n + size - 1) / size)

// ---------------------------------------------------------------- Logo
/** Logo segitiga Citra (res/drawable/logo_citra.png). */
@Composable
fun CitraMark(dimension: Dp) {
    Image(painterResource(R.drawable.logo_citra), null, Modifier.size(dimension))
}

/** Tulisan "Citra Productions" versi teks (cadangan). */
@Composable
fun CitraWordmark(citraSize: TextUnit = 28.sp, subSize: TextUnit = 8.sp) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text("Citra", color = CitraRed, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
            fontSize = citraSize, letterSpacing = 1.sp)
        Spacer(Modifier.width(4.dp))
        Text("Productions", color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold, fontSize = subSize, modifier = Modifier.padding(bottom = 4.dp))
    }
}

@Composable
fun LogoBar() {
    Column(Modifier.background(MaterialTheme.colorScheme.surface).statusBarsPadding()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            CitraMark(32.dp)
            Spacer(Modifier.width(10.dp))
            Image(
                painterResource(R.drawable.logofont_citra), null,
                Modifier.height(32.dp).width(108.dp), contentScale = ContentScale.Fit
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

// ---------------------------------------------------------------- Navbar bawah
private data class NavItem(val route: String, val label: String, val icon: ImageVector)

@Composable
fun CitraNavBar(selected: String, onSelect: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val items = listOf(
        NavItem("home", "Home", Icons.Outlined.Home),
        NavItem("search", "Search", Icons.Outlined.Search),
        NavItem("favorites", "Favorit", Icons.Outlined.FavoriteBorder),
        NavItem("others", "Others", Icons.Filled.MoreHoriz)
    )
    Column(Modifier.background(cs.surface)) {
        HorizontalDivider(color = cs.navBorder)
        Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(top = 8.dp, bottom = 12.dp)) {
            items.forEach { item ->
                val active = item.route == selected
                val tint = if (active) cs.onPrimaryContainer else cs.navInactive
                Column(
                    Modifier.weight(1f).clickable(
                        interactionSource = remember { MutableInteractionSource() }, indication = null
                    ) { onSelect(item.route) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        Modifier.size(60.dp, 30.dp).clip(RoundedCornerShape(18.dp))
                            .background(if (active) cs.primaryContainer else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) { Icon(item.icon, item.label, Modifier.size(22.dp), tint = tint) }
                    Text(item.label, fontSize = 12.sp, lineHeight = 16.sp, color = tint,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}

// ---------------------------------------------------------------- Umum
@Composable
fun BackRow(text: String, strong: Boolean = false, onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onBack).padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(24.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 16.sp, lineHeight = 24.sp,
            fontWeight = if (strong) FontWeight.Medium else FontWeight.Normal,
            color = if (strong) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SectionTitle(text: String) =
    Text(text, fontSize = 21.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold)

/** Placeholder foto produk. Ganti dengan Image(painterResource(...)) atau Coil AsyncImage dari API. */
@Composable
fun ProductImage(modifier: Modifier = Modifier) {
    Box(modifier.background(Color(0xFF8D8478)), contentAlignment = Alignment.Center) {
        Icon(Icons.Default.EmojiEvents, null, Modifier.fillMaxSize(0.5f), tint = Color.White.copy(alpha = 0.8f))
    }
}

@Composable
fun ProductCard(p: Product, fav: Boolean, onFav: () -> Unit, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Card(
        onClick = onClick, modifier = modifier, shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        border = BorderStroke(1.dp, cs.outlineVariant)
    ) {
        Column(Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(8.dp))) {
                ProductImage(Modifier.fillMaxSize())
                IconButton(onFav, Modifier.align(Alignment.TopEnd).padding(top = 3.dp, end = 3.dp).size(32.dp)) {
                    Icon(if (fav) Icons.Filled.Star else Icons.Outlined.StarBorder, "Favorit",
                        Modifier.size(24.dp), tint = Color.White)
                }
            }
            Column {
                Text(p.categoryName, fontSize = 12.sp, lineHeight = 20.sp, color = cs.onSurfaceVariant)
                Text(p.name, fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(p.price, fontSize = 12.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, color = cs.priceColor)
        }
    }
}

/** Grid 2 kolom tanpa LazyGrid, jadi aman di dalam Column yang bisa di-scroll. */
@Composable
fun ProductGrid(
    list: List<Product>, favorites: List<Int>, onFav: (Int) -> Unit,
    gap: Dp = 12.dp, onOpen: (Product) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        list.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                pair.forEach { p ->
                    ProductCard(p, p.id in favorites, { onFav(p.id) }, { onOpen(p) }, Modifier.weight(1f))
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun CategoryCard(c: ProductCategory, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Card(
        onClick = onClick, modifier = modifier, shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) cs.primary else cs.outlineVariant)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Placeholder foto kategori (lingkaran 50dp). Ganti dengan foto dari API / export Figma.
            Box(Modifier.size(50.dp).clip(CircleShape).background(Color(0xFF9A9186)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Category, null, Modifier.size(24.dp), tint = Color.White.copy(alpha = 0.85f))
            }
            Text(c.name, fontSize = 16.sp, lineHeight = 24.sp, maxLines = 1)
        }
    }
}

@Composable
fun CategoryGrid(selected: String?, onClick: (String) -> Unit) {
    val cs = Sample.categories
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            cs.take(3).forEach { c -> CategoryCard(c, c.id == selected, { onClick(c.id) }, Modifier.weight(1f)) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CategoryCard(cs[3], cs[3].id == selected, { onClick(cs[3].id) }, Modifier.weight(1f))
            CategoryCard(cs[4], cs[4].id == selected, { onClick(cs[4].id) }, Modifier.weight(2f))
        }
    }
}

// ---------------------------------------------------------------- Skeleton (state Loading)
@Composable
fun SkeletonBox(modifier: Modifier, shape: Shape = RoundedCornerShape(6.dp)) {
    val a by rememberInfiniteTransition(label = "sk").animateFloat(
        0.4f, 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "a"
    )
    Box(modifier.alpha(a).background(MaterialTheme.colorScheme.outlineVariant, shape))
}

@Composable
private fun SkeletonCategory(modifier: Modifier) {
    Box(modifier.height(108.dp).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))) {
        Column(Modifier.fillMaxSize().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            SkeletonBox(Modifier.size(50.dp), CircleShape)
            Spacer(Modifier.height(14.dp))
            SkeletonBox(Modifier.width(72.dp).height(12.dp))
        }
    }
}

@Composable
fun SkeletonHome() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { repeat(3) { SkeletonCategory(Modifier.weight(1f)) } }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonCategory(Modifier.weight(1f)); SkeletonCategory(Modifier.weight(2f))
        }
    }
}

@Composable
private fun SkeletonProduct(modifier: Modifier) {
    Column(
        modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)).padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SkeletonBox(Modifier.fillMaxWidth().aspectRatio(1f), RoundedCornerShape(8.dp))
        SkeletonBox(Modifier.width(44.dp).height(10.dp))
        SkeletonBox(Modifier.width(118.dp).height(14.dp))
        SkeletonBox(Modifier.fillMaxWidth(0.8f).height(10.dp))
        Spacer(Modifier.height(2.dp))
    }
}

@Composable
fun SkeletonProducts() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(2) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonProduct(Modifier.weight(1f)); SkeletonProduct(Modifier.weight(1f))
            }
        }
    }
}

// ---------------------------------------------------------------- Pagination
@Composable
private fun PageBox(box: Dp, selected: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(8.dp)
    Box(
        Modifier.size(box).clip(shape)
            .background(if (selected) cs.primary else Color.Transparent)
            .then(if (selected) Modifier else Modifier.border(1.dp, cs.pageBorder, shape))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
fun Pagination(page: Int, total: Int, onPage: (Int) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp),
        Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), Alignment.CenterVertically
    ) {
        PageBox(28.dp, false, { if (page > 1) onPage(page - 1) }) {
            Icon(Icons.Default.ChevronLeft, "Sebelumnya", Modifier.size(16.dp), tint = cs.onSurface)
        }
        (1..total).forEach { i ->
            val sel = i == page
            PageBox(28.dp, sel, { onPage(i) }) {
                Text("$i", fontSize = 12.sp, lineHeight = 16.sp,
                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                    color = if (sel) cs.onPrimary else cs.onSurface)
            }
        }
        PageBox(28.dp, false, { if (page < total) onPage(page + 1) }) {
            Icon(Icons.Default.ChevronRight, "Berikutnya", Modifier.size(16.dp), tint = cs.onSurface)
        }
    }
}

// ---------------------------------------------------------------- Banner & empty state
@Composable
fun OfflineBanner(onRetry: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surfaceVariant),
        border = BorderStroke(1.dp, cs.outlineVariant)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WifiOff, null, Modifier.size(14.dp), tint = cs.priceColor)
                Spacer(Modifier.width(6.dp))
                Text("Mode offline · Menampilkan data tersimpan", fontSize = 12.sp, lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold, color = cs.priceColor)
            }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Belum dapat memuat pembaruan.", fontSize = 12.sp, lineHeight = 20.sp, color = cs.onSurfaceVariant)
                OutlinedButton(
                    onRetry, Modifier.height(32.dp), shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    border = BorderStroke(1.dp, cs.outlineVariant)
                ) { Text("Coba lagi", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = cs.priceColor) }
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector, title: String, description: String,
    buttonText: String, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier.fillMaxWidth().padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, Modifier.size(44.dp), tint = cs.primary.copy(alpha = 0.4f))
        Spacer(Modifier.height(8.dp))
        Text(title, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(description, fontSize = 12.sp, lineHeight = 18.sp, color = cs.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Button(
            onClick, Modifier.height(34.dp), shape = CircleShape,
            contentPadding = PaddingValues(horizontal = 22.dp),
            colors = ButtonDefaults.buttonColors(containerColor = cs.primary, contentColor = cs.onPrimary)
        ) { Text(buttonText, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
    }
}

@Composable
fun WhatsAppButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick, modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = Color.White)
    ) {
        Icon(Icons.Default.Chat, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
        Text("Chat melalui WhatsApp", fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}