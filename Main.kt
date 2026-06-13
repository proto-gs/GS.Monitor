import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.draw.clip
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import androidx.compose.foundation.isSystemInDarkTheme
import java.awt.Cursor
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.res.painterResource


const val VERSION = "1.0"
fun main() = application {
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
            } catch (e: Exception) {}
        }

        var appThemeSetting by remember { mutableStateOf("system") }
        val isDarkTheme = when (appThemeSetting) {
            "dark" -> true
            "light" -> false
            else -> isSystemInDarkTheme()
        }

        MaterialTheme(colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Column(modifier = Modifier.fillMaxSize()) {

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
                                        Icon(Icons.Default.KeyboardArrowLeft, "Свернуть", tint = Color.Gray)
                                    }
                                }
                            }

                            if (!isSidebarExpanded) {
                                IconButton(
                                    onClick = { isSidebarExpanded = true },
                                    modifier = Modifier.size(32.dp)
                                        .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                                ) {
                                    Icon(Icons.Default.KeyboardArrowRight, "Развернуть", tint = Color.Gray)
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




@Composable
fun MainAppScreen(
    appThemeSetting: String,
    onThemeChange: (String) -> Unit,
    selectedTab: String,
    onTabChange: (String) -> Unit
) {


    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    var fadeVal by remember { mutableStateOf(1.0f) }
    val animatedFadeVal by animateFloatAsState(
        targetValue = fadeVal,
        animationSpec = androidx.compose.animation.core.spring(
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow,
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy
        ),
        label = "global_screen_fade"
    )
    /// var currentView by remember { mutableStateOf("welcome") }
    var isBottomSheetOpen by remember { mutableStateOf(false) }
    var isWelcomeSettingsOpen by remember { mutableStateOf(false) }
    var isScanSettingsOpen by remember { mutableStateOf(false) }
    var isHistoryOpen by remember { mutableStateOf(false) }
    var isMenuExpanded by remember { mutableStateOf(false) }
    var isThemeDialogOpen by remember { mutableStateOf(false) }

    var followRedirectsSetting by remember { mutableStateOf(true) }
    var requestTimeoutSetting by remember { mutableStateOf(10) }
    var checkSslSetting by remember { mutableStateOf(false) }

    var urlInput by remember { mutableStateOf("") }
    var resText by remember { mutableStateOf("") }
    var resTextColor by remember { mutableStateOf(Color.White) }







    var safeText by remember { mutableStateOf("") }
    var safeTextColor by remember { mutableStateOf(Color.White) }
    var isLoading by remember { mutableStateOf(false) }

    val scanHistoryList = remember { mutableStateListOf<String>() }
    val httpMethods = listOf("GET", "POST", "HEAD", "PUT")
    var selectedMethodIndex by remember { mutableStateOf(0) }
    val responseHeadersList = remember { mutableStateListOf<Pair<String, String>>() }

    var welcomeScale by remember { mutableStateOf(1.0f) }
    var scanScale by remember { mutableStateOf(1.0f) }

    val springSpec = androidx.compose.animation.core.spring<Float>(stiffness = 450f, dampingRatio = 0.75f)
    val gsCheckVal by animateFloatAsState(targetValue = scanScale, animationSpec = springSpec)

    val isDark = when (appThemeSetting) {
        "dark" -> true
        "light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    val textColorPrimary = if (isDark) Color.White else Color(0xFF1C1B1F)
    val textColorSecondary = if (isDark) Color.Gray else Color(0xFF5E5E62)
    val settingsIconColor = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)
    val dropdownBgColor = if (isDark) Color(0xFF16161A) else Color(0xFFF3F3F7)
    val dropdownTextColor = if (isDark) Color.White else Color.Black
    val cardBgColor = if (isDark) Color(0xFF1E1F24) else Color(0xFFF0F1F5)

    val switchView: (String) -> Unit = { target ->
        fadeVal = 1.0f

        if (target == "main") {
            onTabChange("scan")
        } else if (target == "welcome") {
            onTabChange("home")
        }
    }





val runScan: () -> Unit = {
        val url = urlInput.trim()
        if (url.isNotEmpty()) {
            isLoading = true
            resText = "АНАЛИЗ..."
            resTextColor = Color.White
            safeText = ""
            responseHeadersList.clear()

            scope.launch(Dispatchers.IO) {
                val fullUrl = if (url.startsWith("http")) url else "https://$url"
                val client = OkHttpClient.Builder()
                    .connectTimeout(requestTimeoutSetting.toLong(), java.util.concurrent.TimeUnit.SECONDS)
                    .followRedirects(followRedirectsSetting)
                    .build()

                val method = httpMethods[selectedMethodIndex]
                val requestBuilder = Request.Builder().url(fullUrl)

                if (method == "POST" || method == "PUT") {
                    requestBuilder.method(method, okhttp3.RequestBody.create(null, ByteArray(0)))
                } else {
                    requestBuilder.method(method, null)
                }

                try {
                    client.newCall(requestBuilder.build()).execute().use { response ->
                        val code = response.code
                        resText = "HTTP $code"
                        resTextColor = if (code < 400) Color(0xFF00E676) else Color(0xFFFFB74D)

                        val isHttps = response.request.url.isHttps
                        safeText = if (isHttps) "БЕЗОПАСНО (SSL)" else "НЕБЕЗОПАСНО (HTTP)"
                        safeTextColor = if (isHttps) Color(0xFF00E676) else Color(0xFFFF1744)

                        scanHistoryList.add(0, "[$method] $url -> HTTP $code")

                        val headers = response.headers
                        for (i in 0 until headers.size) {
                            responseHeadersList.add(Pair(headers.name(i), headers.value(i)))
                        }
                    }
                } catch (e: IOException) {
                    resText = "ОШИБКА"
                    resTextColor = Color(0xFFFF1744)
                    safeText = "СЕРВЕР НЕДОСТУПЕН"
                    safeTextColor = Color.Gray
                    scanHistoryList.add(0, "[$method] $url -> ОШИБКА")
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().alpha(animatedFadeVal.coerceIn(0f, 1f))) {
        // ИЗМЕНЕНО: Теперь проверяем вкладку из бокового меню
        if (selectedTab == "home") {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(top = 16.dp, end = 24.dp)) {
                    IconButton(onClick = { isMenuExpanded = true }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Filled.Settings, "Настройки", tint = settingsIconColor, modifier = Modifier.size(28.dp))
                    }

                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false },
                        modifier = Modifier.background(dropdownBgColor),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Информация", color = dropdownTextColor) },
                            onClick = { isMenuExpanded = false; isBottomSheetOpen = true }
                        )
                        DropdownMenuItem(
                            text = { Text("Сайт разработчика", color = dropdownTextColor) },
                            onClick = { isMenuExpanded = false; uriHandler.openUri("https://gs-ht.ru") }
                        )
                        DropdownMenuItem(
                            text = { Text("Настройки", color = dropdownTextColor) },
                            onClick = { isMenuExpanded = false; isWelcomeSettingsOpen = true }
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxHeight().width(320.dp).align(Alignment.Center)
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier.size(100.dp).clip(CircleShape).background(if (isDark) Color.White else Color(0xFF1C1B1F)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("GS", fontSize = 38.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.Black else Color.White)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("GS HTTP", fontSize = 36.sp, fontWeight = FontWeight.Black, color = textColorPrimary)
                    Text("ENGINE BY G. SMERDOV", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = textColorSecondary, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.weight(1f))

                    AnimatedButton(
                        text = "ПРИСТУПИТЬ",
                        textColor = Color.White,
                        bgColor = Color(0xFF2979FF),
                        scale = welcomeScale,
                        onPressDown = { welcomeScale = 0.95f },
                        onPressUp = { welcomeScale = 1.0f },
                        onClick = { switchView("main") }
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

        } else if (selectedTab == "scan") {


            Box(modifier = Modifier.fillMaxSize().background(if (isDark) Color(0xFF0F1014) else Color(0xFFF9F9FB))) {


                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(onClick = { isHistoryOpen = true }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "История",
                            tint = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { isScanSettingsOpen = true }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Настройки",
                            tint = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(

                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(top = 64.dp, start = 24.dp, end = 24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    TabRow(
                        selectedTabIndex = selectedMethodIndex,
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFF2979FF),
                        divider = {},
                        indicator = { tabPositions ->
                            if (selectedMethodIndex < tabPositions.size) {
                                Box(modifier = Modifier.tabIndicatorOffset(tabPositions[selectedMethodIndex]).height(2.dp).background(Color(0xFF2979FF)))
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.4f)
                    ) {
                        httpMethods.forEachIndexed { index, method ->
                            val isSelected = selectedMethodIndex == index
                            val tabTextColor = when {
                                isSelected && isDark -> Color.White
                                isSelected && !isDark -> Color(0xFF1C1B1F)
                                else -> Color.Gray.copy(alpha = 0.6f)
                            }
                            Tab(
                                selected = isSelected,
                                onClick = { selectedMethodIndex = index },
                                text = { Text(text = method, fontSize = 15.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = tabTextColor) }
                            )
                        }
                    }



                Spacer(modifier = Modifier.height(24.dp))

                TextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    placeholder = { Text("Проверить URL", color = Color.Gray.copy(alpha = 0.6f)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = if (isDark) Color.White else Color.Black,
                        unfocusedTextColor = if (isDark) Color.White else Color.Black,
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Normal)
                )

                Spacer(modifier = Modifier.height(24.dp))

                    if (resText.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBgColor)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (resText == "АНАЛИЗ...") Color.Gray.copy(alpha = 0.2f) else resTextColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = resText,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (resText == "АНАЛИЗ...") textColorPrimary else resTextColor
                                        )
                                    }

                                    if (safeText.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(safeTextColor.copy(alpha = 0.15f))
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = safeText,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = safeTextColor
                                            )
                                        }
                                    }
                                }

                                if (responseHeadersList.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "ЗАГОЛОВКИ ОТВЕТА (${responseHeadersList.size})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    val scrollState = rememberScrollState()
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 240.dp)
                                            .background(if (isDark) Color(0xFF141517) else Color(0xFFE8E9ED), RoundedCornerShape(12.dp))
                                            .padding(8.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .verticalScroll(scrollState)
                                                .padding(end = 16.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            responseHeadersList.forEach { header ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = header.first,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFF2979FF),
                                                        modifier = Modifier.weight(0.35f)
                                                    )
                                                    Text(
                                                        text = header.second,
                                                        fontSize = 13.sp,
                                                        color = textColorPrimary,
                                                        modifier = Modifier.weight(0.65f)
                                                    )
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(1.dp)
                                                        .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))
                                                )
                                            }
                                        }

                                        VerticalScrollbar(
                                            adapter = rememberScrollbarAdapter(scrollState),
                                            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
                                        )
                                    }
                                }
                            }
                        }
                    }
            }



        Column(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 24.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedButton(
                text = if (isLoading) "ИДЕТ ЗАПРОС..." else "ЗАПУСТИТЬ СКАН",
                textColor = Color.White,
                bgColor = if (isLoading) Color.Gray else Color(0xFF2979FF),
                scale = gsCheckVal,
                onPressDown = { if (!isLoading) scanScale = 0.93f },
                onPressUp = { scanScale = 1.0f },
                onClick = { if (!isLoading) runScan() }
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { switchView("welcome") }) {
                Text("← ВЕРНУТЬСЯ", color = Color(0xFF2979FF).copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
}



                            if (isBottomSheetOpen) {
                                AlertDialog(
                                    onDismissRequest = { isBottomSheetOpen = false },
                                    title = { Text("ИНФОРМАЦИЯ", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = dropdownTextColor) },
                                    text = {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Text("Разработчик: Георгий Смердов", fontSize = 14.sp, color = dropdownTextColor.copy(alpha = 0.7f))
                                            Text("Движок: PhysicsEngine 1.0", fontSize = 13.sp, color = dropdownTextColor.copy(alpha = 0.4f), fontStyle = FontStyle.Italic)
                                            Text("Версия: $VERSION", fontSize = 12.sp, color = dropdownTextColor.copy(alpha = 0.3f))
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(onClick = { isBottomSheetOpen = false }) { Text("ГОТОВО", color = Color(0xFF2979FF)) }
                                    },
                                    containerColor = dropdownBgColor,
                                    shape = RoundedCornerShape(24.dp)
                                )
                            }

                            if (isWelcomeSettingsOpen || isScanSettingsOpen) {
                                AlertDialog(
                                    onDismissRequest = { isWelcomeSettingsOpen = false; isScanSettingsOpen = false },
                                    title = { Text("НАСТРОЙКИ ПРИЛОЖЕНИЯ", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = dropdownTextColor) },
                                    text = {
                                        Column(modifier = Modifier.fillMaxWidth().width(400.dp)) {
                                            Text("СЕТЬ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Column {
                                                    Text("Таймаут запроса", color = dropdownTextColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                                    Text("Максимальное время ожидания", color = Color.Gray, fontSize = 11.sp)
                                                }
                                                Button(
                                                    onClick = { requestTimeoutSetting = when(requestTimeoutSetting) { 5 -> 10; 10 -> 15; else -> 5 } },
                                                    colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF1E2127) else Color(0xFFE0E0E6)),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text("$requestTimeoutSetting сек", color = dropdownTextColor, fontSize = 13.sp)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Column {
                                                    Text("Авто-редирект", color = dropdownTextColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                                    Text("Следовать перенаправлениям 3xx", color = textColorSecondary, fontSize = 11.sp)
                                                }
                                                Switch(checked = followRedirectsSetting, onCheckedChange = { followRedirectsSetting = it })
                                            }
                                            Spacer(modifier = Modifier.height(20.dp))
                                            Text("БЕЗОПАСНОСТЬ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Column {
                                                    Text("Проверить SSL", color = dropdownTextColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                                    Text("Блокировать небезопасные связи", color = Color.Gray, fontSize = 11.sp)
                                                }
                                                Switch(checked = checkSslSetting, onCheckedChange = { checkSslSetting = it })
                                            }
                                            Spacer(modifier = Modifier.height(20.dp))
                                            Text("ОФОРМЛЕНИЕ И ДАННЫЕ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Button(
                                                onClick = { isThemeDialogOpen = true },
                                                modifier = Modifier.fillMaxWidth().height(40.dp),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = if(isDark) Color(0xFF2A2A30) else Color(0xFFE0E0E6), contentColor = dropdownTextColor)
                                            ) {
                                                val themeText = when(appThemeSetting) { "dark" -> "Тёмная"; "light" -> "Светлая"; else -> "Как в системе" }
                                                Text("Тема оформления: $themeText", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Button(
                                                onClick = { urlInput = ""; resText = ""; safeText = ""; scanHistoryList.clear(); responseHeadersList.clear(); isWelcomeSettingsOpen = false; isScanSettingsOpen = false },
                                                modifier = Modifier.fillMaxWidth().height(40.dp),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = if(isDark) Color(0xFF222226) else Color(0xFFFFEBEE), contentColor = Color(0xFFFF1744))
                                            ) {
                                                Text("Очистить историю и ввод", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(onClick = { isWelcomeSettingsOpen = false; isScanSettingsOpen = false }) { Text("ГОТОВО", color = Color(0xFF2979FF)) }
                                    },
                                    containerColor = dropdownBgColor,
                                    shape = RoundedCornerShape(24.dp)
                                )
                            }

                            if (isHistoryOpen) {
                                AlertDialog(
                                    onDismissRequest = { isHistoryOpen = false },
                                    title = { Text("История сканов", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = dropdownTextColor) },
                                    text = {
                                        val historyScrollState = rememberScrollState()
                                        Box(modifier = Modifier.fillMaxWidth().width(450.dp).heightIn(max = 300.dp)) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .verticalScroll(historyScrollState)
                                                    .padding(end = 16.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                if (scanHistoryList.isEmpty()) {
                                                    Text("История проверок пуста", color = Color.Gray, fontSize = 15.sp, modifier = Modifier.padding(vertical = 16.dp))
                                                } else {
                                                    scanHistoryList.forEach { logItem ->
                                                        Text(logItem, color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF1C1B1F), fontSize = 14.sp, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
                                                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)))
                                                    }
                                                }
                                            }
                                            VerticalScrollbar(
                                                adapter = rememberScrollbarAdapter(historyScrollState),
                                                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
                                            )
                                        }
                                    },
                                    confirmButton = {
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            TextButton(onClick = { scanHistoryList.clear() }) { Text("ОЧИСТИТЬ", color = Color(0xFFFF4D4D), fontWeight = FontWeight.Bold) }
                                            TextButton(onClick = { isHistoryOpen = false }) { Text("ГОТОВО", color = Color(0xFF2979FF), fontWeight = FontWeight.Bold) }
                                        }
                                    },
                                    containerColor = dropdownBgColor,
                                    shape = RoundedCornerShape(24.dp)
                                )
                            }

                            if (isThemeDialogOpen) {
                                AlertDialog(
                                    onDismissRequest = { isThemeDialogOpen = false },
                                    title = { Text("Выберите тему", color = dropdownTextColor) },
                                    text = {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                                RadioButton(selected = appThemeSetting == "system", onClick = { onThemeChange("system"); isThemeDialogOpen = false })
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Как в системе", color = dropdownTextColor)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                                RadioButton(selected = appThemeSetting == "light", onClick = { onThemeChange("light"); isThemeDialogOpen = false })
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Светлая", color = dropdownTextColor)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                                RadioButton(selected = appThemeSetting == "dark", onClick = { onThemeChange("dark"); isThemeDialogOpen = false })
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Тёмная", color = dropdownTextColor)
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(onClick = { isThemeDialogOpen = false }) { Text("Отмена", color = Color(0xFF2979FF)) }
                                    },
                                    containerColor = dropdownBgColor,
                                    shape = RoundedCornerShape(24.dp)
                                )
                            }
                        }

                        @Composable
                        fun AnimatedButton(
                            text: String,
                            textColor: Color,
                            bgColor: Color,
                            scale: Float,
                            onPressDown: () -> Unit,
                            onPressUp: () -> Unit,
                            onClick: () -> Unit
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(320.dp)
                                    .height(56.dp)
                                    .graphicsLayer(scaleX = scale, scaleY = scale)
                                    .clip(RoundedCornerShape(size = 24.dp))
                                    .background(bgColor)
                                    .pointerInput(Unit) {
                                        detectTapGestures(onPress = {
                                            onPressDown()
                                            try {
                                                tryAwaitRelease()
                                            } catch (e: Exception) {
                                            }
                                            onPressUp()
                                            onClick()
                                        })
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = text, fontWeight = FontWeight.Bold, color = textColor, fontSize = 16.sp)
                            }
                        }





























