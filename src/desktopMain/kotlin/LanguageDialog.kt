import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import java.util.prefs.Preferences

@Composable
fun LanguageDialog(
    isOpen: Boolean,
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    strings: AppStrings,
    backgroundColor: Color,
    textPrimaryColor: Color,
    textSecondaryColor: Color
) {
    if (!isOpen) return

    val prefs = remember { Preferences.userRoot().node("app_prefs") }

    DialogWindow(
        onCloseRequest = onDismiss,
        title = strings.langTitle,
        state = androidx.compose.ui.window.rememberDialogState(width = 380.dp, height = 280.dp)
    ) {
        Surface(
            color = backgroundColor,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = strings.langTitle,
                    color = textPrimaryColor,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                val languages = listOf("ru" to "Русский", "en" to "English")

                languages.forEach { (code, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                try {
                                    prefs.put("app_lang", code)
                                    onLanguageSelected(code)
                                    onDismiss()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = currentLanguage == code,
                            onClick = {
                                try {
                                    prefs.put("app_lang", code)
                                    onLanguageSelected(code)
                                    onDismiss()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = textPrimaryColor,
                                unselectedColor = textSecondaryColor
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = label, color = textPrimaryColor)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = strings.cancel, color = textPrimaryColor)
                    }
                }
            }
        }
    }
}
