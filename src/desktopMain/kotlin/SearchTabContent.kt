import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.awt.Cursor

@Composable
fun SearchTabContent(
    isDark: Boolean,
    monochromeAccent: Color,
    monochromeSecondary: Color,
    textColorPrimary: Color,
    cardBgColor: Color,
    searchQueryInput: String,
    onSearchQueryChange: (String) -> Unit,
    searchResultsList: List<String>,
    isSearchLoading: Boolean,
    onOpenHistory: () -> Unit,
    onRunSearch: () -> Unit,
    strings: AppStrings,
    onSelectSite: (String) -> Unit
) {
    var selectedZoneIndex by remember { mutableStateOf(0) }
    val zones = listOf("ALL", "COM", "ORG", "NET", "RU", "IO", "ME", "CO", "CC", "APP", "DEV")

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
                    onClick = onOpenHistory,
                    modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                ) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = strings.story_searches,
                        tint = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.searches_matches,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
            }

            TextField(
                value = searchQueryInput,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = strings.example_keyword,
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

            if (searchResultsList.isNotEmpty() && !searchResultsList.contains(strings.notFound)) {
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
                            text = "${strings.sites_found} (${filteredResults.size})",
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
                                                    if (site != strings.notFound) {
                                                        onSelectSite(site)
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
                onClick = { if (!isSearchLoading) onRunSearch() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isSearchLoading) Color.Gray else monochromeAccent, contentColor = if (isDark) Color.Black else Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isSearchLoading) strings.searchLoading else strings.findMatches,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}