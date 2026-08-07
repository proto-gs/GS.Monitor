import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ThemeSelectionDialog(
    appThemeSetting: String,
    dropdownBgColor: Color,
    dropdownTextColor: Color,
    monochromeAccent: Color,
    onThemeChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите тему", color = dropdownTextColor) },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = appThemeSetting == "system", onClick = { onThemeChange("system"); onDismiss() }, colors = RadioButtonDefaults.colors(selectedColor = monochromeAccent))
                    Text("Как в системе", color = dropdownTextColor)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = appThemeSetting == "light", onClick = { onThemeChange("light"); onDismiss() }, colors = RadioButtonDefaults.colors(selectedColor = monochromeAccent))
                    Text("Светлая", color = dropdownTextColor)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = appThemeSetting == "dark", onClick = { onThemeChange("dark"); onDismiss() }, colors = RadioButtonDefaults.colors(selectedColor = monochromeAccent))
                    Text("Тёмная", color = dropdownTextColor)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = monochromeAccent)
            }
        },
        containerColor = dropdownBgColor,
        shape = RoundedCornerShape(24.dp)
    )
}