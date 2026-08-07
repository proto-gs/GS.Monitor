import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HistoryDialog(
    scanHistoryList: List<String>,
    dropdownBgColor: Color,
    dropdownTextColor: Color,
    monochromeAccent: Color,
    monochromeSecondary: Color,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("История сканов", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = dropdownTextColor)
                if (scanHistoryList.isNotEmpty()) {
                    TextButton(onClick = onClearHistory) {
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
            TextButton(onClick = onDismiss) {
                Text("Готово", color = monochromeAccent, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = dropdownBgColor,
        shape = RoundedCornerShape(24.dp)
    )
}