import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.awt.Cursor

@Composable
fun InfoBottomSheetDialog(
    version: String,
    dropdownBgColor: Color,
    dropdownTextColor: Color,
    monochromeAccent: Color,
    uriHandler: androidx.compose.ui.platform.UriHandler,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Информация о приложении",
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
                    text = "Исходный код и разработка",
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
                onClick = onDismiss,
                modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
            ) {
                Text("Готово", color = monochromeAccent, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = dropdownBgColor,
        shape = RoundedCornerShape(24.dp)
    )
}