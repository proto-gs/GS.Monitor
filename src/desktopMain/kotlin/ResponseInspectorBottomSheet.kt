import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponseInspectorBottomSheet(
    inspectorSheetState: SheetState,
    dropdownBgColor: Color,
    dropdownTextColor: Color,
    monochromeAccent: Color,
    monochromeSecondary: Color,
    textColorPrimary: Color,
    textColorSecondary: Color,
    isDark: Boolean,
    strings: AppStrings,
    lastValidUrl: String,
    uriHandler: androidx.compose.ui.platform.UriHandler,
    activeSearchTab: String,
    onActiveSearchTabChange: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    responseBodyText: String,
    responseHeadersText: String,
    responseCookiesText: String,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                text = strings.inspectorTitle,
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
                    text = strings.btnOpenBrowserEmoji,
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
                listOf("Body", "Headers", "Cookies").forEach { tab ->
                    val isSelected = activeSearchTab == tab
                    TextButton(
                        onClick = { onActiveSearchTabChange(tab) },
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
                onValueChange = onSearchQueryChange,
                label = {
                    Text(text = strings.searchLogPlaceholder)
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
                "Body" -> responseBodyText
                "Headers" -> responseHeadersText
                else -> responseCookiesText
            }

            val filteredText = if (searchQuery.isEmpty()) {
                currentTextData
            } else {
                try {
                    if (currentTextData.length > 500_000) {
                        strings.searchTooBig
                    } else {
                        currentTextData.lines()
                            .filter { it.contains(other = searchQuery, ignoreCase = true) }
                            .joinToString(separator = "\n")
                    }
                } catch (_: Exception) {
                    strings.searchError
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
                            text = if (filteredText.trim().isEmpty() && searchQuery.isNotEmpty()) {
                                strings.notFound
                            } else {
                                filteredText
                            },
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