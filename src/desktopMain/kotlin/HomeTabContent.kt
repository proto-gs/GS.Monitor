import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.awt.Cursor
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale

@Composable
fun HomeTabContent(
    isDark: Boolean,
    textColorPrimary: Color,
    settingsIconColor: Color,
    dropdownBgColor: Color,
    dropdownTextColor: Color,
    uriHandler: androidx.compose.ui.platform.UriHandler,
    onMenuOpen: () -> Unit,
    isMenuExpanded: Boolean,
    onMenuDismiss: () -> Unit,
    onOpenInfo: () -> Unit,
    onOpenSettings: () -> Unit,
    strings: AppStrings,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 24.dp)
        ) {
            IconButton(
                onClick = onMenuOpen,
                modifier = Modifier.size(48.dp)
                    .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Menu",
                    tint = settingsIconColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            DropdownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = onMenuDismiss,
                modifier = Modifier.background(dropdownBgColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                DropdownMenuItem(
                    text = { Text(text = strings.home_info, color = dropdownTextColor) },
                    onClick = { onMenuDismiss(); onOpenInfo() }
                )
                DropdownMenuItem(
                    text = { Text(text = strings.home_site, color = dropdownTextColor) },
                    onClick = { onMenuDismiss(); uriHandler.openUri("https://gs-ht.ru") }
                )
                DropdownMenuItem(
                    text = { Text(text = strings.home_settings, color = dropdownTextColor) },
                    onClick = { onMenuDismiss(); onOpenSettings() }
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
                Image(
                    painter = painterResource("ic_gs_ht.png"),
                    contentDescription = "Аватар",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "GS Monitor",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = textColorPrimary
            )
        }
    }
}