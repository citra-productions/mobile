package com.citra.point

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CitraTheme(false) {
                SplashScreen {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onDone: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    LaunchedEffect(Unit) { delay(1800); onDone() }
    Box(Modifier.fillMaxSize().background(cs.surface)) {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            CitraMark(96.dp)
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("Citra", color = CitraRed, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
                    fontSize = 48.sp, letterSpacing = 2.sp)
                Spacer(Modifier.width(12.dp))
                Text("Point", color = cs.primary, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic, fontSize = 30.sp, modifier = Modifier.padding(bottom = 6.dp))
            }
            Spacer(Modifier.height(40.dp))
            LinearProgressIndicator(Modifier.width(120.dp).height(4.dp).clip(RoundedCornerShape(2.dp)),
                trackColor = cs.outlineVariant)
            Text("Memuat data...", fontSize = 12.sp, lineHeight = 20.sp, color = cs.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp))
        }
        Column(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Powered by", fontSize = 12.sp, lineHeight = 20.sp, color = cs.onSurfaceVariant)
            Image(painterResource(R.drawable.logofont_citra), null,
                Modifier.height(36.dp).width(118.dp), contentScale = ContentScale.Fit)
        }
    }
}
