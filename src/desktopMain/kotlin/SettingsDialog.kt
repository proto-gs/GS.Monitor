import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun SettingsDialog(
    isDark: Boolean,
    dropdownBgColor: Color,
    dropdownTextColor: Color,
    monochromeAccent: Color,
    monochromeSecondary: Color,
    followRedirectsSetting: Boolean,
    onFollowRedirectsChange: (Boolean) -> Unit,
    requestTimeoutSetting: Int,
    onRequestTimeoutChange: (Int) -> Unit,
    verifySslSetting: Boolean,
    onVerifySslChange: (Boolean) -> Unit,
    ignoreSslErrorsSetting: Boolean,
    onIgnoreSslChange: (Boolean) -> Unit,
    customUserAgentSetting: String,
    defaultUserAgent: String,
    onUserAgentChange: (String) -> Unit,
    onOpenThemeDialog: () -> Unit,
    onClearData: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Application settings",
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
                Text("Network and Connectivity", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = monochromeSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Auto-redirect", color = dropdownTextColor, fontSize = 14.sp)
                    Switch(
                        checked = followRedirectsSetting,
                        onCheckedChange = onFollowRedirectsChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = if (isDark) Color.Black else Color.White,
                            checkedTrackColor = monochromeAccent
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Request timeout: ${requestTimeoutSetting}с", color = dropdownTextColor, fontSize = 14.sp)
                Slider(
                    value = requestTimeoutSetting.toFloat(),
                    onValueChange = { onRequestTimeoutChange(it.toInt()) },
                    valueRange = 2f..30f,
                    steps = 28,
                    colors = SliderDefaults.colors(
                        thumbColor = monochromeAccent,
                        activeTrackColor = monochromeAccent
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Security and SSL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = monochromeSecondary)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Check SSL", color = dropdownTextColor, fontSize = 14.sp)
                        Text("(Strict certificate verification)", color = Color.Gray, fontSize = 11.sp)
                    }
                    Switch(
                        checked = verifySslSetting,
                        onCheckedChange = onVerifySslChange,
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
                        Text("Ignore SSL errors", color = dropdownTextColor, fontSize = 14.sp)
                        Text("(self-signed certificates)", color = Color.Gray, fontSize = 11.sp)
                    }
                    Switch(
                        checked = ignoreSslErrorsSetting,
                        onCheckedChange = onIgnoreSslChange,
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
                        onClick = { onUserAgentChange(defaultUserAgent) },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Default", fontSize = 12.sp, color = monochromeAccent)
                    }
                }
                TextField(
                    value = customUserAgentSetting,
                    onValueChange = onUserAgentChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 13.sp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = monochromeAccent,
                        unfocusedIndicatorColor = monochromeSecondary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Registration", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = monochromeSecondary)

                Button(
                    onClick = onOpenThemeDialog,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = monochromeAccent, contentColor = if (isDark) Color.Black else Color.White)
                ) {
                    Text("Change the design theme", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Data and history", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = monochromeSecondary)

                Button(
                    onClick = onClearData,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF333333) else Color(0xFFCCCCCC), contentColor = dropdownTextColor),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Clear history and input", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Ready", color = monochromeAccent, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = dropdownBgColor,
        shape = RoundedCornerShape(24.dp)
    )
}