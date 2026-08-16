import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit
import java.util.prefs.Preferences
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

    val palette = MaterialTheme.colorScheme



    val prefs = remember { java.util.prefs.Preferences.userRoot().node("app_prefs") }
    val defaultUserAgent = "GS.Monitor/1.0"




    var currentLanguage by remember {
        mutableStateOf(prefs.get("app_lang", "ru"))
    }


    val strings = remember(currentLanguage) {
        getStringsForLanguage(currentLanguage)
    }





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
    var isLanguageOpen by remember { mutableStateOf(false) }

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

    val scanHistoryList = remember {
        val savedHistory = prefs.get("scan_history_items", "")
        val list = mutableStateListOf<String>()
        if (savedHistory.isNotEmpty()) {
            list.addAll(savedHistory.split("\n"))
        }
        list
    }

    val saveHistoryToPrefs: () -> Unit = {
        val combined = scanHistoryList.joinToString("\n")
        prefs.put("scan_history_items", combined)
    }

    val httpMethods = listOf("GET", "POST", "PUT", "HEAD", "DELETE", "PATCH", "OPTIONS", "TRACE", "CONNECT")
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
                        } catch (_: Exception) {
                        }
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
                    saveHistoryToPrefs()
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
            resText = strings.statusError
            resTextColor = monochromeAccent
            safeText = strings.statusInvalid
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
                                override fun checkClientTrusted(
                                    chain: Array<java.security.cert.X509Certificate>,
                                    authType: String
                                ) {
                                }

                                override fun checkServerTrusted(
                                    chain: Array<java.security.cert.X509Certificate>,
                                    authType: String
                                ) {
                                }

                                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
                            })
                            val sslContext = SSLContext.getInstance("Ssl")
                            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
                            clientBuilder.sslSocketFactory(
                                sslContext.socketFactory,
                                trustAllCerts[0] as X509TrustManager
                            )
                            clientBuilder.hostnameVerifier { _, _ -> true }
                        } catch (_: Exception) {
                        }
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
                        "DELETE" -> requestBuilder.delete(emptyBody)
                        "PATCH" -> requestBuilder.patch(emptyBody)
                        "OPTIONS" -> requestBuilder.method("OPTIONS", null)
                        "TRACE" -> requestBuilder.method("TRACE", null)
                        "CONNECT" -> requestBuilder.method("CONNECT", null)
                    }


                    client.newCall(requestBuilder.build()).execute().use { response ->
                        val code = response.code
                        resText = "HTTP $code"
                        resTextColor = monochromeAccent
                        val isHttps = response.request.url.isHttps
                        safeText = if (isHttps) strings.statusSsl else strings.statusHttp
                        safeTextColor = monochromeSecondary
                        lastValidUrl = fullUrl
                        responseBodyText = response.body?.string() ?: ""
                        responseHeadersText = response.headers.joinToString("\n") { "${it.first}: ${it.second}" }
                        val cookies = response.headers("Set-Cookie")
                        responseCookiesText =
                            if (cookies.isNotEmpty()) cookies.joinToString(separator = "\n") else strings.cookiesEmpty

                        scanHistoryList.add(
                            0,
                            "[$selectedMethod] $fullUrl -> HTTP $code (VerifySSL: $verifySslSetting)"
                        )
                        saveHistoryToPrefs()
                    }
                } catch (_: IllegalArgumentException) {
                    resText = strings.statusError
                    resTextColor = monochromeAccent
                    safeText = strings.statusInvalid
                    safeTextColor = monochromeSecondary
                } catch (_: IOException) {
                    resText = strings.statusError
                    resTextColor = monochromeAccent
                    safeText = strings.statusNoServer
                    safeTextColor = monochromeSecondary
                } finally {
                    isLoading = false
                }

            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().alpha(animatedFadeVal.coerceIn(0f, 1f))) {
        when (selectedTab) {
            "home" -> {
                HomeTabContent(
                    isDark = isDark,
                    textColorPrimary = textColorPrimary,
                    settingsIconColor = settingsIconColor,
                    dropdownBgColor = dropdownBgColor,
                    dropdownTextColor = dropdownTextColor,
                    uriHandler = uriHandler,
                    onMenuOpen = { isMenuExpanded = true },
                    isMenuExpanded = isMenuExpanded,
                    onMenuDismiss = { isMenuExpanded = false },
                    onOpenInfo = { isBottomSheetOpen = true },
                    onOpenSettings = { isWelcomeSettingsOpen = true },
                    strings = strings
                )
            }

            "scan" -> {
                ScanTabContent(
                    isDark = isDark,
                    monochromeAccent = monochromeAccent,
                    monochromeSecondary = monochromeSecondary,
                    cardBgColor = cardBgColor,
                    httpMethods = httpMethods,
                    selectedMethodIndex = selectedMethodIndex,
                    onMethodIndexChange = { selectedMethodIndex = it },
                    urlInput = urlInput,
                    onUrlInputChange = { urlInput = it },
                    resText = resText,
                    resTextColor = resTextColor,
                    safeText = safeText,
                    safeTextColor = safeTextColor,
                    isLoading = isLoading,
                    lastValidUrl = lastValidUrl,
                    onOpenHistory = { isHistoryOpen = true },
                    onOpenSettings = { isScanSettingsOpen = true },
                    onOpenInspector = { isResponseInspectorSheetOpen = true },
                    onRunScan = runScan,
                    strings = strings,
                    uriHandler = uriHandler,
                )
            }

            "search" -> {
                SearchTabContent(
                    isDark = isDark,
                    monochromeAccent = monochromeAccent,
                    monochromeSecondary = monochromeSecondary,
                    textColorPrimary = textColorPrimary,
                    cardBgColor = cardBgColor,
                    searchQueryInput = searchQueryInput,
                    onSearchQueryChange = { searchQueryInput = it },
                    searchResultsList = searchResultsList,
                    isSearchLoading = isSearchLoading,
                    onOpenHistory = { isHistoryOpen = true },
                    onRunSearch = runSearch,
                    onSelectSite = { site ->
                        urlInput = site
                        onTabChange("scan")
                    },
                    strings = strings
                )
            }
        }
    }

    if (isBottomSheetOpen) {
        InfoBottomSheetDialog(
            version = version,
            dropdownBgColor = dropdownBgColor,
            dropdownTextColor = dropdownTextColor,
            monochromeAccent = monochromeAccent,
            uriHandler = uriHandler,
            onDismiss = { isBottomSheetOpen = false },
            strings = strings,
        )
    }

    if (isWelcomeSettingsOpen || isScanSettingsOpen) {
        SettingsDialog(
            isDark = isDark,
            dropdownBgColor = dropdownBgColor,
            dropdownTextColor = dropdownTextColor,
            monochromeAccent = monochromeAccent,
            monochromeSecondary = monochromeSecondary,
            followRedirectsSetting = followRedirectsSetting,
            onFollowRedirectsChange = { followRedirectsSetting = it; prefs.putBoolean("follow_redirects", it) },
            requestTimeoutSetting = requestTimeoutSetting,
            onRequestTimeoutChange = { requestTimeoutSetting = it; prefs.putInt("request_timeout", it) },
            verifySslSetting = verifySslSetting,
            onVerifySslChange = {
                verifySslSetting = it
                prefs.putBoolean("verify_ssl", it)
                if (it) {
                    ignoreSslErrorsSetting = false
                    prefs.putBoolean("ignore_ssl", false)
                }
            },
            ignoreSslErrorsSetting = ignoreSslErrorsSetting,
            onIgnoreSslChange = {
                ignoreSslErrorsSetting = it
                prefs.putBoolean("ignore_ssl", it)
                if (it) {
                    verifySslSetting = false
                    prefs.putBoolean("verify_ssl", false)
                }
            },
            strings = strings,
            onLanguageDialogOpen = { isLanguageOpen = true }, // <--- ЗАПЯТАЯ ИСПРАВЛЕНА ТУТ
            customUserAgentSetting = customUserAgentSetting,
            defaultUserAgent = defaultUserAgent,
            onUserAgentChange = {
                customUserAgentSetting = it
                prefs.put("user_agent", it)
            },
            onOpenThemeDialog = { isThemeDialogOpen = true },
            onClearData = {
                scanHistoryList.clear()
                saveHistoryToPrefs()
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
            onDismiss = { isWelcomeSettingsOpen = false; isScanSettingsOpen = false }
        )
    }


    if (isHistoryOpen) {
        HistoryDialog(
            scanHistoryList = scanHistoryList,
            dropdownBgColor = dropdownBgColor,
            dropdownTextColor = dropdownTextColor,
            monochromeAccent = monochromeAccent,
            monochromeSecondary = monochromeSecondary,
            onClearHistory = {
                scanHistoryList.clear()
                saveHistoryToPrefs()
            },
            onDismiss = { isHistoryOpen = false },
            strings = strings
        )
    }

    if (isThemeDialogOpen) {
        ThemeSelectionDialog(
            appThemeSetting = appThemeSetting,
            dropdownBgColor = dropdownBgColor,
            dropdownTextColor = dropdownTextColor,
            monochromeAccent = monochromeAccent,
            onThemeChange = onThemeChange,
            onDismiss = { isThemeDialogOpen = false },
            strings = strings,
        )
    }



    LanguageDialog(
        isOpen = isLanguageOpen,
        currentLanguage = currentLanguage,
        onLanguageSelected = { selectedLang ->
            currentLanguage = selectedLang
        },
        onDismiss = { isLanguageOpen = false },
        strings = strings,
        backgroundColor = palette.surfaceContainer,
        textPrimaryColor = palette.onSurface,
        textSecondaryColor = palette.onSurfaceVariant
    )






    if (isResponseInspectorSheetOpen) {
        ResponseInspectorBottomSheet(
            inspectorSheetState = inspectorSheetState,
            dropdownBgColor = dropdownBgColor,
            dropdownTextColor = dropdownTextColor,
            monochromeAccent = monochromeAccent,
            monochromeSecondary = monochromeSecondary,
            textColorPrimary = textColorPrimary,
            textColorSecondary = textColorSecondary,
            isDark = isDark,
            strings = strings,
            lastValidUrl = lastValidUrl,
            uriHandler = uriHandler,
            activeSearchTab = activeSearchTab,
            onActiveSearchTabChange = { activeSearchTab = it },
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            responseBodyText = responseBodyText,
            responseHeadersText = responseHeadersText,
            responseCookiesText = responseCookiesText,
            onDismiss = { isResponseInspectorSheetOpen = false }
        )
    }
}







private fun cardBgCardColorInternal(fallback: Color): Color = fallback
