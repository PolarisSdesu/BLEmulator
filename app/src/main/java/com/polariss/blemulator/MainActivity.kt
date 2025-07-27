package com.polariss.blemulator

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.polariss.blemulator.ui.theme.BLEmulatorTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            BLEmulatorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    App(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun App(modifier: Modifier) {
    val isUnlocked = remember { mutableStateOf(false) }

    MaterialTheme {
        Column(modifier = modifier) {
            TitleText()
            BootloaderStatus(isUnlocked.value)
            if (!isUnlocked.value) {
                BootLoaderContent(onUnlock = { isUnlocked.value = true })
            }
        }
    }
}


@Composable
fun TitleText() {
    Box(
        modifier = Modifier.padding(start = 27.dp, top = 20.dp)
    ) {
        Text(
            text = "设备解锁状态",
            fontSize = 33.sp
        )
    }
}

@Composable
fun BootloaderStatus(isUnlocked: Boolean) {
    val isDark = isSystemInDarkTheme()
    val imageRes = if (isUnlocked) R.drawable.bootloader_unlock else R.drawable.bootloader_lock
    val text = if (isUnlocked) "当前设备已解锁" else "当前设备已锁定\n手机数据处于安全状态"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 57.dp, bottom = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            colorFilter = if (isDark) null else ColorFilter.tint(Color(0x99000000)),
            modifier = Modifier.scale(1.1f)
        )
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = text,
            textAlign = TextAlign.Center,
        )
    }
}


@Composable
fun BootLoaderContent(onUnlock: () -> Unit) {
    val scrollState = rememberScrollState()

    CompositionLocalProvider(
        LocalTextStyle provides TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Medium)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 80.dp)
            ) {
                Text(
                    text = "为什么需要锁定当前设备？",
                    color = Color(0xFF8B8B8B),
                )
                Divider()
                Text(
                    text = "锁定设备可以保证手机安全，避免系统被篡改或个人数据泄露。如果您需要 Fastboot 刷机，请先解锁。\n注意：设备解锁后，“查找手机”功能将无法使用。",
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "如果您已经了解风险，但依然需要解锁，怎么办？",
                    color = Color(0xFF8B8B8B),
                )
                Divider()
                Text(
                    text = """
            1. 确保手机中已经插入 SIM 卡。
            2. 关闭手机 WLAN 并打开数据连接，确保网络可用。
            3. 点击底部“绑定账号和设备”按钮，将您的小米账号与当前设备绑定。
            4. 绑定成功后，请访问以下网址，下载解锁工具（解锁需在电脑上进行）：
        """.trimIndent(),
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "https://unlock.update.miui.com",
                    color = Color(0xFF0C90FF)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                BindButton("绑定账号和设备", onUnlock = onUnlock)
            }
        }
    }
}


@Composable
fun BindButton(text: String, onUnlock: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val clickCount = remember { mutableIntStateOf(0) }

    val backgroundColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF0F0F0)
    val contentColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF000000)

    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = {
            scope.launch {
                clickCount.intValue++
                if (clickCount.intValue >= 10) {
                    onUnlock()
                } else {
                    showToast(context, "正在申请绑定账号和设备...")
                    delay(3000)
                    showToast(context, "绑定失败，请前往小米社区内测中心申请授权后重试")
                }
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        Text(text, fontSize = 19.sp)
    }
}


@Composable
fun Divider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 2.dp),
        thickness = 1.2.dp,
        DividerDefaults.color
    )
}

fun showToast(toastContext: Context, message: String) {
    Toast.makeText(toastContext, message, Toast.LENGTH_SHORT).show()
}