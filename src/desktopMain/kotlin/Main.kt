import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.awt.Cursor
import java.io.IOException
import java.lang.System
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit
import java.io.File
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import kotlin.io.path.Path
import kotlin.io.path.appendText
import kotlin.io.path.exists
import kotlin.io.path.readLines
import kotlin.io.path.writeText
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.FindInPage



const val VERSION = "1.0.2"
val currentUserId = System.getProperty("user.name") ?: "unknown_user"


private fun getHistoryFile(): java.io.File {
    val userHome = System.getProperty("user.home") ?: ""

    val appDir = java.io.File(userHome, ".gs_monitor")
    if (!appDir.exists()) {
        appDir.mkdirs()
    }
    return java.io.File(appDir, "scan_history.txt")
}

fun saveToKotlinHistory(text: String) {
    runCatching {
        val file = getHistoryFile()
        file.appendText("$text\n", Charsets.UTF_8)
    }.onFailure { it.printStackTrace() }
}

fun loadKotlinHistory(): List<String> {
    return runCatching {
        val file = getHistoryFile()
        if (file.exists()) file.readLines(Charsets.UTF_8) else emptyList()
    }.getOrDefault(emptyList())
}

fun clearKotlinHistoryFile() {
    runCatching {
        val file = getHistoryFile()
        if (file.exists()) {
            file.writeText("", Charsets.UTF_8)
        }
    }.onFailure { it.printStackTrace() }
}





val globalHttpClient = OkHttpClient.Builder()
    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
    .build()

fun main() {

    application {
        val windowState = rememberWindowState(size = DpSize(850.dp, 650.dp))

        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "GS.Monitor",
            icon = painterResource("icon.ico"),
            undecorated = false,
            transparent = false
        ) {
            LaunchedEffect(Unit) {
                try {
                    window.rootPane.putClientProperty("apple.awt.fullWindowContent", true)
                    window.rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
                    window.title = "GS.Monitor"
                } catch (e: Exception) {

                }
            }
            var appThemeSetting by remember { mutableStateOf("system") }
            val isDarkTheme = when (appThemeSetting) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }
            MaterialTheme(colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {

                    var selectedTab by remember { mutableStateOf("home") }
                    var isSidebarExpanded by remember { mutableStateOf(false) }
                    val sidebarWidth by animateDpAsState(if (isSidebarExpanded) 200.dp else 72.dp)

                    Row(modifier = Modifier.fillMaxSize()) {

                        Column(
                            modifier = Modifier
                                .width(sidebarWidth)
                                .fillMaxHeight()
                                .background(if (isDarkTheme) Color(0xFF141517) else Color(0xFFE8E9ED)),
                            horizontalAlignment = if (isSidebarExpanded) Alignment.Start else Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))


                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = if (isSidebarExpanded) 16.dp else 0.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = if (isSidebarExpanded) Arrangement.SpaceBetween else Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isDarkTheme) Color.White else Color(0xFF1C1B1F)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "GS",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkTheme) Color.Black else Color.White
                                    )
                                }
                                if (isSidebarExpanded) {
                                    IconButton(
                                        onClick = { isSidebarExpanded = false },
                                        modifier = Modifier.size(32.dp)
                                            .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Свернуть", tint = Color.Gray)
                                    }
                                }
                            }
                            if (!isSidebarExpanded) {
                                IconButton(
                                    onClick = { isSidebarExpanded = true },
                                    modifier = Modifier.size(32.dp)
                                        .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Развернуть", tint = Color.Gray)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))


                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = if (isSidebarExpanded) 16.dp else 0.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = if (isSidebarExpanded) Arrangement.Start else Arrangement.Center
                            ) {
                                IconButton(
                                    onClick = { selectedTab = "home" },
                                    modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Home,
                                        contentDescription = "Главная",
                                        tint = if (selectedTab == "home") Color(0xFF2979FF) else Color.Gray
                                    )
                                }
                                if (isSidebarExpanded) {
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        "Главная",
                                        color = if (selectedTab == "home") Color(0xFF2979FF) else Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = if (isSidebarExpanded) 16.dp else 0.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = if (isSidebarExpanded) Arrangement.Start else Arrangement.Center
                            ) {
                                IconButton(
                                    onClick = { selectedTab = "scan" },
                                    modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = "Сканер",
                                        tint = if (selectedTab == "scan") Color(0xFF2979FF) else Color.Gray
                                    )
                                }
                                if (isSidebarExpanded) {
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        "Сканер URL",
                                        color = if (selectedTab == "scan") Color(0xFF2979FF) else Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = if (isSidebarExpanded) 16.dp else 0.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = if (isSidebarExpanded) Arrangement.Start else Arrangement.Center
                            ) {
                                IconButton(
                                    onClick = { selectedTab = "search" },
                                    modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                                ) {
                                    Icon(

                                        imageVector = Icons.Filled.FindInPage,
                                        contentDescription = "Поиск совпадений",
                                        tint = if (selectedTab == "search") Color(0xFF2979FF) else Color.Gray
                                    )
                                }
                                if (isSidebarExpanded) {
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        "Поиск совпадений",
                                        color = if (selectedTab == "search") Color(0xFF2979FF) else Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                        }
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            MainAppScreen(
                                appThemeSetting = appThemeSetting,
                                onThemeChange = { appThemeSetting = it },
                                selectedTab = selectedTab,
                                onTabChange = { selectedTab = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

