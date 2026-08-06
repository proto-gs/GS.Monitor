import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.awt.Cursor
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.prefs.Preferences
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    appThemeSetting: String,
    onThemeChange: (String) -> Unit,
    selectedTab: String,
    onTabChange: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    val prefs = remember { Preferences.userNodeForPackage(object {}::class.java) }

    val defaultUserAgent = "GS.Monitor/1.0"

    val strings = mapOf(
        "status_error" to "ERROR",
        "status_invalid" to "INVALID INPUT",
        "status_ssl" to "HTTPS SECURE",
        "status_http" to "HTTP INSECURE",
        "status_no_server" to "SERVER UNREACHABLE",
        "cookies_empty" to "No cookies found",
        "inspector_title" to "SERVER RESPONSE DATA",
        "btn_open_browser_emoji" to "OPEN SITE IN BROWSER 🌐",
        "search_log_placeholder" to "Search text inside log...",
        "search_too_big" to "Error: Log is too large",
        "search_error" to "error",
        "not_found" to "Nothing found"
    )
    val version = "1.0.3"

    var fadeVal by remember { mutableStateOf(1.0f) }
    val animatedFadeVal by animateFloatAsState(
        targetValue = fadeVal,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioNoBouncy
        ),
        label = "global_screen_fade"
    )

    var isBottomSheetOpen by remember { mutableStateOf(false) }
    var isWelcomeSettingsOpen by remember { mutableStateOf(false) }
    var isScanSettingsOpen by remember { mutableStateOf(false) }
    var isHistoryOpen by remember { mutableStateOf(false) }
    var isMenuExpanded by remember { mutableStateOf(false) }
    var isThemeDialogOpen by remember { mutableStateOf(false) }

    var isResponseInspectorSheetOpen by remember { mutableStateOf(false) }
    val inspectorSheetState = rememberModalBottomSheetState()
    var activeSearchTab by remember { mutableStateOf("BODY") }
    var searchQuery by remember { mutableStateOf("") }

    var followRedirectsSetting by remember { mutableStateOf(prefs.getBoolean("follow_redirects", true)) }
    var requestTimeoutSetting by remember { mutableStateOf(prefs.getInt("request_timeout", 10)) }
    var ignoreSslErrorsSetting by remember { mutableStateOf(prefs.getBoolean("ignore_ssl", false)) }
    var verifySslSetting by remember { mutableStateOf(prefs.getBoolean("verify_ssl", true)) }
    var customUserAgentSetting by remember { mutableStateOf(prefs.get("user_agent", defaultUserAgent)) }

    var urlInput by remember { mutableStateOf("") }
    var resText by remember { mutableStateOf("") }
    var resTextColor by remember { mutableStateOf(Color.White) }

    var safeText by remember { mutableStateOf("") }
    var safeTextColor by remember { mutableStateOf(Color.White) }
    var isLoading by remember { mutableStateOf(false) }

    var responseBodyText by remember { mutableStateOf("") }
    var responseHeadersText by remember { mutableStateOf("") }
    var responseCookiesText by remember { mutableStateOf("") }
    var lastValidUrl by remember { mutableStateOf("") }

    val scanHistoryList = remember { mutableStateListOf<String>() }

    val httpMethods = listOf("GET", "POST", "HEAD", "PUT")
    var selectedMethodIndex by remember { mutableStateOf(0) }
    val selectedMethod = httpMethods[selectedMethodIndex]

    val isDark = when (appThemeSetting) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val textColorPrimary = if (isDark) Color.White else Color(0xFF1C1B1F)
    val textColorSecondary = if (isDark) Color.Gray else Color(0xFF5E5E62)
    val settingsIconColor = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)
    val dropdownBgColor = if (isDark) Color(0xFF121212) else Color(0xFFF5F5F5)
    val dropdownTextColor = if (isDark) Color.White else Color.Black
    val cardBgColor = if (isDark) Color(0xFF1A1A1A) else Color(0xFFEEEEEE)

    // Монохромные/Чёрно-белые акценты интерфейса
    val monochromeAccent = if (isDark) Color.White else Color.Black
    val monochromeSecondary = if (isDark) Color(0xFF888888) else Color(0xFF666666)

    var searchQueryInput by remember { mutableStateOf("") }
    val searchResultsList = remember { mutableStateListOf<String>() }
    var isSearchLoading by remember { mutableStateOf(false) }

    val runSearch: () -> Unit = {
        val query = searchQueryInput.trim().lowercase().replace(" ", "")
        if (query.isNotEmpty()) {
            isSearchLoading = true
            searchResultsList.clear()

            scope.launch(Dispatchers.IO) {
                val topExtensions = listOf("com", "org", "net", "ru", "io", "me", "co", "cc", "app", "dev")
                val activeDomains = mutableListOf<String>()
                val candidateUrls = topExtensions.map { ext -> "$query.$ext" }

                candidateUrls.map { domain ->
                    launch {
                        try {
                            val address = InetAddress.getByName(domain)
                            val socket = Socket()
                            socket.connect(InetSocketAddress(address, 80), 1200)
                            socket.close()

                            synchronized(activeDomains) {
                                activeDomains.add(domain)
                            }
                        } catch (_: Exception) {}
                    }
                }.joinAll()

                withContext(Dispatchers.Main) {
                    if (activeDomains.isEmpty()) {
                        searchResultsList.add("Ничего не найдено")
                    } else {
                        val sorted = activeDomains.sortedBy { ext ->
                            when {
                                ext.endsWith(".com") -> 0
                                ext.endsWith(".ru") -> 1
                                else -> 2
                            }
                        }
                        searchResultsList.addAll(sorted)
                    }

                    val searchLogItem = "[ПОИСК] Ключевое слово: '$query' -> Найдено доменов: ${activeDomains.size}"
                    scanHistoryList.add(0, searchLogItem)
                    isSearchLoading = false
                }
            }
        }
    }

    val runScan: () -> Unit = {
        val url = urlInput.trim()
        val hasSpaces = url.contains(" ")
        val isInvalidProtocol = (url.startsWith("https:/") && !url.startsWith("https://")) ||
                (url.startsWith("http:/") && !url.startsWith("http://"))

        if (url.isEmpty() || hasSpaces || isInvalidProtocol) {
            resText = strings["status_error"] ?: "ERROR"
            resTextColor = monochromeAccent
            safeText = strings["status_invalid"] ?: "INVALID INPUT"
            safeTextColor = monochromeSecondary
            isLoading = false
        } else {
            isLoading = true
            resText = ""
            safeText = ""
            responseBodyText = ""
            responseHeadersText = ""
            responseCookiesText = ""
            lastValidUrl = ""

            scope.launch(Dispatchers.IO) {
                val fullUrl = if (url.startsWith("http")) url else "https://$url"
                try {
                    val clientBuilder = OkHttpClient.Builder()
                        .connectTimeout(requestTimeoutSetting.toLong(), TimeUnit.SECONDS)
                        .readTimeout(requestTimeoutSetting.toLong(), TimeUnit.SECONDS)
                        .followRedirects(followRedirectsSetting)

                    if (ignoreSslErrorsSetting && !verifySslSetting) {
                        try {
                            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                                override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
                                override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
                                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
                            })
                            val sslContext = SSLContext.getInstance("SSL")
                            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
                            clientBuilder.sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                            clientBuilder.hostnameVerifier { _, _ -> true }
                        } catch (_: Exception) {}
                    }

                    val client = clientBuilder.build()

                    val requestBuilder = Request.Builder()
                        .url(fullUrl)
                        .header("User-Agent", customUserAgentSetting)

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val emptyBody = ByteArray(0).toRequestBody(mediaType)

                    when (selectedMethod) {
                        "GET" -> requestBuilder.get()
                        "POST" -> requestBuilder.post(emptyBody)
                        "PUT" -> requestBuilder.put(emptyBody)
                        "HEAD" -> requestBuilder.head()
                    }

                    client.newCall(requestBuilder.build()).execute().use { response ->
                        val code = response.code
                        resText = "HTTP $code"
                        resTextColor = monochromeAccent
                        val isHttps = response.request.url.isHttps
                        safeText = if (isHttps) strings["status_ssl"] ?: "" else strings["status_http"] ?: ""
                        safeTextColor = monochromeSecondary
                        lastValidUrl = fullUrl
                        responseBodyText = response.body?.string() ?: ""
                        responseHeadersText = response.headers.joinToString("\n") { "${it.first}: ${it.second}" }
                        val cookies = response.headers("Set-Cookie")
                        responseCookiesText = if (cookies.isNotEmpty()) cookies.joinToString("\n") else strings["cookies_empty"] ?: ""

                        scanHistoryList.add(0, "[$selectedMethod] $fullUrl -> HTTP $code (VerifySSL: $verifySslSetting)")
                    }
                } catch (_: IllegalArgumentException) {
                    resText = strings["status_error"] ?: "ERROR"
                    resTextColor = monochromeAccent
                    safeText = strings["status_invalid"] ?: "INVALID INPUT"
                    safeTextColor = monochromeSecondary
                } catch (_: IOException) {
                    resText = strings["status_error"] ?: "ERROR"
                    resTextColor = monochromeAccent
                    safeText = strings["status_no_server"] ?: "SERVER UNREACHABLE"
                    safeTextColor = monochromeSecondary
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().alpha(animatedFadeVal.coerceIn(0f, 1f))) {
        if (selectedTab == "home") {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier.align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 24.dp)
                ) {
                    IconButton(
                        onClick = { isMenuExpanded = true },
                        modifier = Modifier.size(48.dp)
                            .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Меню",
                            tint = settingsIconColor,
                            modifier = Modifier.size(28.dp)
                        )
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
                    modifier = Modifier
                        .width(360.dp)
                        .align(Alignment.TopCenter)
                        .padding(top = 110.dp)
                ) {
                    Box(
                        modifier = Modifier.size(120.dp).clip(CircleShape)
                            .background(if (isDark) Color.White else Color(0xFF1C1B1F)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "GS",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.Black else Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "GS HTTP",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        color = textColorPrimary
                    )
                    Text(
                        "ENGINE BY G. SMERDOV",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColorSecondary,
                        letterSpacing = 2.sp
                    )
                }
            }
        } else if (selectedTab == "scan") {
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(if (isDark) Color(0xFF0A0A0A) else Color(0xFFFAFAFA))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isHistoryOpen = true },
                        modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                    ) {
                        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = "История",
                                tint = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Row {
                        if (lastValidUrl.isNotEmpty()) {
                            Button(
                                onClick = { isResponseInspectorSheetOpen = true },
                                colors = ButtonDefaults.buttonColors(containerColor = monochromeAccent, contentColor = if (isDark) Color.Black else Color.White),
                                modifier = Modifier.padding(end = 8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Инспектор", fontSize = 12.sp)
                            }
                        }
                        IconButton(
                            onClick = { isScanSettingsOpen = true },
                            modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Настройки",
                                tint = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)
                        .padding(top = 64.dp, start = 24.dp, end = 24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    TabRow(
                        selectedTabIndex = selectedMethodIndex,
                        containerColor = Color.Transparent,
                        contentColor = monochromeAccent,
                        divider = {},
                        indicator = { tabPositions ->
                            if (selectedMethodIndex < tabPositions.size) {
                                Box(
                                    modifier = Modifier
                                        .tabIndicatorOffset(tabPositions[selectedMethodIndex])
                                        .height(2.dp)
                                        .background(monochromeAccent)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.6f)
                    ) {
                        httpMethods.forEachIndexed { index, method ->
                            val isSelected = selectedMethodIndex == index
                            val tabTextColor = when {
                                isSelected && isDark -> Color.White
                                isSelected && !isDark -> Color.Black
                                else -> Color.Gray.copy(alpha = 0.6f)
                            }
                            Tab(
                                selected = isSelected,
                                onClick = { selectedMethodIndex = index },
                                text = {
                                    Text(
                                        text = method,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = tabTextColor
                                    )
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    TextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        placeholder = {
                            Text("Проверить URL", color = Color.Gray.copy(alpha = 0.6f))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = if (isDark) Color.White else Color.Black,
                            unfocusedTextColor = if (isDark) Color.White else Color.Black,
                            focusedIndicatorColor = monochromeAccent,
                            unfocusedIndicatorColor = monochromeSecondary
                        ),
                        textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Normal)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    if (resText.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBgCardColorInternal(cardBgColor))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(resTextColor.copy(alpha = 0.15f))
                                            .border(1.dp, resTextColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = resText,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = resTextColor
                                        )
                                    }
                                    if (safeText.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(safeTextColor.copy(alpha = 0.15f))
                                                .border(1.dp, safeTextColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
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
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp, start = 24.dp, end = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { if (!isLoading) runScan() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isLoading) Color.Gray else monochromeAccent, contentColor = if (isDark) Color.Black else Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isLoading) "ИДЕТ ЗАПРОС..." else "ЗАПУСТИТЬ СКАН", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (selectedTab == "search") {
            var selectedZoneIndex by remember { mutableStateOf(0) }
            val zones = listOf("ВСЕ", "COM", "ORG", "NET", "RU", "IO", "ME", "CO", "CC", "APP", "DEV")

            val filteredResults = remember(searchResultsList, selectedZoneIndex) {
                if (selectedZoneIndex == 0) {
                    searchResultsList
                } else {
                    val targetZone = "." + zones[selectedZoneIndex].lowercase()
                    searchResultsList.filter { site -> site.endsWith(targetZone) }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isDark) Color(0xFF0A0A0A) else Color(0xFFFAFAFA))
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isHistoryOpen = true },
                            modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = "История",
                                tint = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ПОИСК ПО СОВПАДЕНИЯМ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                    }

                    TextField(
                        value = searchQueryInput,
                        onValueChange = { searchQueryInput = it },
                        placeholder = {
                            Text(
                                text = "Введите ключевое слово (например, microsoft)",
                                color = Color.Gray.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = if (isDark) Color.White else Color.Black,
                            unfocusedTextColor = if (isDark) Color.White else Color.Black,
                            focusedIndicatorColor = monochromeAccent,
                            unfocusedIndicatorColor = monochromeSecondary
                        ),
                        textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Normal)
                    )

                    if (searchResultsList.isNotEmpty() && !searchResultsList.contains("Ничего не найдено")) {
                        ScrollableTabRow(
                            selectedTabIndex = selectedZoneIndex,
                            containerColor = Color.Transparent,
                            contentColor = monochromeAccent,
                            edgePadding = 0.dp
                        ) {
                            zones.forEachIndexed { index, zone ->
                                Tab(
                                    selected = selectedZoneIndex == index,
                                    onClick = { selectedZoneIndex = index },
                                    text = { Text(text = zone, fontSize = 12.sp) }
                                )
                            }
                        }
                    }

                    if (filteredResults.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBgColor)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "НАЙДЕННЫЕ САЙТЫ (${filteredResults.size})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                val searchScrollState = rememberScrollState()
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 240.dp)
                                        .background(
                                            if (isDark) Color(0xFF141414) else Color(0xFFEFEFEF),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .verticalScroll(searchScrollState)
                                            .padding(end = 16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        filteredResults.forEach { site ->
                                            Text(
                                                text = site,
                                                fontSize = 14.sp,
                                                color = textColorPrimary,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                                                    .pointerInput(Unit) {
                                                        detectTapGestures(onTap = {
                                                            if (site != "Ничего не найдено") {
                                                                urlInput = site
                                                                onTabChange("scan")
                                                            }
                                                        })
                                                    }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { if (!isSearchLoading) runSearch() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isSearchLoading) Color.Gray else monochromeAccent, contentColor = if (isDark) Color.Black else Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isSearchLoading) "ПОИСК..." else "НАЙТИ СОВПАДЕНИЯ", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (isBottomSheetOpen) {
        AlertDialog(
            onDismissRequest = { isBottomSheetOpen = false },
            title = {
                Text(
                    text = "ИНФОРМАЦИЯ О ПРИЛОЖЕНИИ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = dropdownTextColor
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Разработчик: Георгий Смердов", fontSize = 14.sp, color = dropdownTextColor)
                    Text("Версия: $version", fontSize = 14.sp, color = dropdownTextColor)
                    Text("Скачано: GitHub", fontSize = 14.sp, color = dropdownTextColor)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(dropdownTextColor.copy(alpha = 0.1f))
                    )

                    Text(
                        text = "ИСХОДНЫЙ КОД И РАЗРАБОТКА",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )

                    TextButton(
                        onClick = { uriHandler.openUri("https://github.com/g60373250-wq/GS.Monitor") },
                        modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR))),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource("github.png"),
                                contentDescription = "GitHub",
                                tint = dropdownTextColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text("Репозиторий проекта на GitHub ↗", fontSize = 13.sp, color = monochromeAccent)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { isBottomSheetOpen = false },
                    modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                ) {
                    Text("ГОТОВО", color = monochromeAccent, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = dropdownBgColor,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (isWelcomeSettingsOpen || isScanSettingsOpen) {
        AlertDialog(
            onDismissRequest = { isWelcomeSettingsOpen = false; isScanSettingsOpen = false },
            title = {
                Text(
                    "НАСТРОЙКИ ПРИЛОЖЕНИЯ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = dropdownTextColor
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .width(420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("СЕТЬ И ПОДКЛЮЧЕНИЕ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = monochromeSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Авто-редирект", color = dropdownTextColor, fontSize = 14.sp)
                        Switch(
                            checked = followRedirectsSetting,
                            onCheckedChange = {
                                followRedirectsSetting = it
                                prefs.putBoolean("follow_redirects", it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = if (isDark) Color.Black else Color.White,
                                checkedTrackColor = monochromeAccent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Таймаут запроса: ${requestTimeoutSetting}с", color = dropdownTextColor, fontSize = 14.sp)
                    Slider(
                        value = requestTimeoutSetting.toFloat(),
                        onValueChange = {
                            requestTimeoutSetting = it.toInt()
                            prefs.putInt("request_timeout", it.toInt())
                        },
                        valueRange = 2f..30f,
                        steps = 28,
                        colors = SliderDefaults.colors(
                            thumbColor = monochromeAccent,
                            activeTrackColor = monochromeAccent
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("БЕЗОПАСНОСТЬ И SSL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = monochromeSecondary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Проверять SSL", color = dropdownTextColor, fontSize = 14.sp)
                            Text("(строгая проверка сертификата)", color = Color.Gray, fontSize = 11.sp)
                        }
                        Switch(
                            checked = verifySslSetting,
                            onCheckedChange = {
                                verifySslSetting = it
                                prefs.putBoolean("verify_ssl", it)
                                if (it) {
                                    ignoreSslErrorsSetting = false
                                    prefs.putBoolean("ignore_ssl", false)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = if (isDark) Color.Black else Color.White,
                                checkedTrackColor = monochromeAccent
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Игнорировать SSL ошибки", color = dropdownTextColor, fontSize = 14.sp)
                            Text("(самоподписанные сертификаты)", color = Color.Gray, fontSize = 11.sp)
                        }
                        Switch(
                            checked = ignoreSslErrorsSetting,
                            onCheckedChange = {
                                ignoreSslErrorsSetting = it
                                prefs.putBoolean("ignore_ssl", it)
                                if (it) {
                                    verifySslSetting = false
                                    prefs.putBoolean("verify_ssl", false)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = if (isDark) Color.Black else Color.White,
                                checkedTrackColor = monochromeAccent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("User-Agent", color = dropdownTextColor, fontSize = 14.sp)
                        TextButton(
                            onClick = {
                                customUserAgentSetting = defaultUserAgent
                                prefs.put("user_agent", defaultUserAgent)
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("По умолчанию", fontSize = 12.sp, color = monochromeAccent)
                        }
                    }
                    TextField(
                        value = customUserAgentSetting,
                        onValueChange = {
                            customUserAgentSetting = it
                            prefs.put("user_agent", it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 13.sp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = monochromeAccent,
                            unfocusedIndicatorColor = monochromeSecondary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("ОФОРМЛЕНИЕ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = monochromeSecondary)

                    Button(
                        onClick = { isThemeDialogOpen = true },
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = monochromeAccent, contentColor = if (isDark) Color.Black else Color.White)
                    ) {
                        Text("Изменить тему оформления", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("ДАННЫЕ И ИСТОРИЯ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = monochromeSecondary)

                    Button(
                        onClick = {
                            scanHistoryList.clear()
                            urlInput = ""
                            searchQueryInput = ""
                            searchResultsList.clear()
                            resText = ""
                            safeText = ""
                            responseBodyText = ""
                            responseHeadersText = ""
                            responseCookiesText = ""
                            lastValidUrl = ""
                        },
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF333333) else Color(0xFFCCCCCC), contentColor = dropdownTextColor),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Очистить историю и ввод", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isWelcomeSettingsOpen = false; isScanSettingsOpen = false }) {
                    Text("ГОТОВО", color = monochromeAccent, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = dropdownBgColor,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (isHistoryOpen) {
        AlertDialog(
            onDismissRequest = { isHistoryOpen = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("История сканов", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = dropdownTextColor)
                    if (scanHistoryList.isNotEmpty()) {
                        TextButton(onClick = { scanHistoryList.clear() }) {
                            Text("Очистить", color = monochromeSecondary, fontSize = 12.sp)
                        }
                    }
                }
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth().width(450.dp).heightIn(max = 300.dp)) {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (scanHistoryList.isEmpty()) {
                            Text("История проверок пуста", color = Color.Gray, fontSize = 15.sp)
                        } else {
                            scanHistoryList.forEach { logItem ->
                                Text(logItem, color = dropdownTextColor, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isHistoryOpen = false }) {
                    Text("ГОТОВО", color = monochromeAccent, fontWeight = FontWeight.Bold)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = appThemeSetting == "system", onClick = { onThemeChange("system"); isThemeDialogOpen = false }, colors = RadioButtonDefaults.colors(selectedColor = monochromeAccent))
                        Text("Как в системе", color = dropdownTextColor)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = appThemeSetting == "light", onClick = { onThemeChange("light"); isThemeDialogOpen = false }, colors = RadioButtonDefaults.colors(selectedColor = monochromeAccent))
                        Text("Светлая", color = dropdownTextColor)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = appThemeSetting == "dark", onClick = { onThemeChange("dark"); isThemeDialogOpen = false }, colors = RadioButtonDefaults.colors(selectedColor = monochromeAccent))
                        Text("Тёмная", color = dropdownTextColor)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isThemeDialogOpen = false }) {
                    Text("Отмена", color = monochromeAccent)
                }
            },
            containerColor = dropdownBgColor,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (isResponseInspectorSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isResponseInspectorSheetOpen = false },
            sheetState = inspectorSheetState,
            containerColor = dropdownBgColor,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    strings["inspector_title"] ?: "SERVER RESPONSE DATA",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = dropdownTextColor
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (lastValidUrl.isNotEmpty()) {
                            uriHandler.openUri(lastValidUrl)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = monochromeAccent,
                        contentColor = if (isDark) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        strings["btn_open_browser_emoji"] ?: "OPEN SITE IN BROWSER 🌐",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isDark) Color(0xFF1A1A22) else Color(0xFFE0E0E5),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("BODY", "HEADERS", "COOKIES").forEach { tab ->
                        val isSelected = activeSearchTab == tab
                        TextButton(
                            onClick = { activeSearchTab = tab },
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) (if (isDark) Color(0xFF333338) else Color.White) else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                        ) {
                            Text(
                                tab,
                                color = if (isSelected) textColorPrimary else textColorSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = {
                        Text(strings["search_log_placeholder"] ?: "Search text inside log...")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = monochromeAccent,
                        unfocusedIndicatorColor = monochromeSecondary
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                val currentTextData = when (activeSearchTab) {
                    "BODY" -> responseBodyText
                    "HEADERS" -> responseHeadersText
                    else -> responseCookiesText
                }

                val filteredText = if (searchQuery.isEmpty()) {
                    currentTextData
                } else {
                    try {
                        if (currentTextData.length > 500_000) {
                            strings["search_too_big"] ?: "Error: Log is too large"
                        } else {
                            currentTextData.lines()
                                .filter { it.contains(searchQuery, ignoreCase = true) }
                                .joinToString("\n")
                        }
                    } catch (_: Exception) {
                        strings["search_error"] ?: "error"
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(
                            if (isDark) Color(0xFF121215) else Color(0xFFEBEBEF),
                            RoundedCornerShape(16.dp)
                        )
                        .border(
                            1.dp,
                            if (isDark) Color(0xFF2E2E35) else Color(0xFFD0D0D8),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Text(
                                text = if (filteredText.trim().isEmpty() && searchQuery.isNotEmpty())
                                    (strings["not_found"] ?: "Nothing found") else filteredText,
                                color = textColorPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun cardBgCardColorInternal(fallback: Color): Color = fallback