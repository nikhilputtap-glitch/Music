package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary

enum class NeonButtonStyle {
    PRIMARY, // Electric Blue to Neon Purple
    SECONDARY, // Glass border
    DANGER // Neon Magenta/Red
}

@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    style: NeonButtonStyle = NeonButtonStyle.PRIMARY,
    enabled: Boolean = true,
    testTag: String = "neon_button"
) {
    val backgroundBrush = when (style) {
        NeonButtonStyle.PRIMARY -> Brush.horizontalGradient(
            listOf(ElectricBlue, NeonPurple)
        )
        NeonButtonStyle.SECONDARY -> Brush.horizontalGradient(
            listOf(Color(0xFF241A40), Color(0xFF160F2C))
        )
        NeonButtonStyle.DANGER -> Brush.horizontalGradient(
            listOf(NeonMagenta, Color(0xFFFF4081))
        )
    }

    val contentColor = when (style) {
        NeonButtonStyle.PRIMARY -> AmoledBlack
        NeonButtonStyle.SECONDARY -> ElectricBlue
        NeonButtonStyle.DANGER -> TextPrimary
    }

    Box(
        modifier = modifier
            .testTag(testTag)
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (enabled) backgroundBrush else Brush.linearGradient(listOf(Color.DarkGray, Color.Gray)))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}
