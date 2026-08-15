import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.awt.Cursor
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ScrollableTabRow
@Composable
fun ScanTabContent(
    isDark: Boolean,
    monochromeAccent: Color,
    monochromeSecondary: Color,
    cardBgColor: Color,
    httpMethods: List<String>,
    selectedMethodIndex: Int,
    onMethodIndexChange: (Int) -> Unit,
    urlInput: String,
    onUrlInputChange: (String) -> Unit,
    resText: String,
    resTextColor: Color,
    safeText: String,
    safeTextColor: Color,
    isLoading: Boolean,
    lastValidUrl: String,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenInspector: () -> Unit,
    onRunScan: () -> Unit
) {
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
                onClick = onOpenHistory,
                modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
            ) {
                Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = "Story",
                        tint = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Row {
                if (lastValidUrl.isNotEmpty()) {
                    Button(
                        onClick = onOpenInspector,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = monochromeAccent,
                            contentColor = if (isDark) Color.Black else Color.White
                        ),
                        modifier = Modifier.padding(end = 8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Answer", fontSize = 12.sp)
                    }
                }
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
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
            ScrollableTabRow(
                selectedTabIndex = selectedMethodIndex,
                containerColor = Color.Transparent,
                contentColor = monochromeAccent,
                edgePadding = 0.dp,
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
                modifier = Modifier.fillMaxWidth()
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
                        onClick = { onMethodIndexChange(index) },
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
                onValueChange = onUrlInputChange,
                placeholder = {
                    Text("Check URL", color = Color.Gray.copy(alpha = 0.6f))
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
                onClick = { if (!isLoading) onRunScan() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLoading) monochromeAccent.copy(alpha = 0.5f) else monochromeAccent,
                    contentColor = if (isDark) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = if (isDark) Color.Black else Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                    Text(
                        text = if (isLoading) "Examination..." else "Run scan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
