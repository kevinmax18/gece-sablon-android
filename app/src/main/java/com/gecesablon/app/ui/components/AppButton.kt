package com.gecesablon.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.GeceSablonTheme
import com.gecesablon.app.ui.theme.ShapeControl
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding

enum class AppButtonVariant {
    Primary,
    Accent,
    Secondary,
    Text
}

/**
 * Komponen tombol kustom GeCe Screen Printing sesuai DESIGN.md:
 * - Fixed height 52dp across standard actionable states
 * - Radius 12dp
 * - Warna dan font mengikuti MaterialTheme & GeceColors
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.Primary,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    fullWidth: Boolean = true
) {
    val widthModifier = if (fullWidth) modifier.fillMaxWidth() else modifier
    val baseModifier = widthModifier.height(52.dp)

    when (variant) {
        AppButtonVariant.Primary -> {
            Button(
                onClick = onClick,
                modifier = baseModifier,
                enabled = enabled,
                shape = ShapeControl,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                ButtonContent(text = text, leadingIcon = leadingIcon, trailingIcon = trailingIcon)
            }
        }

        AppButtonVariant.Accent -> {
            Button(
                onClick = onClick,
                modifier = baseModifier,
                enabled = enabled,
                shape = ShapeControl,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GeceColors.Accent,
                    contentColor = GeceColors.OnAccent,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                ButtonContent(text = text, leadingIcon = leadingIcon, trailingIcon = trailingIcon)
            }
        }

        AppButtonVariant.Secondary -> {
            Button(
                onClick = onClick,
                modifier = baseModifier,
                enabled = enabled,
                shape = ShapeControl,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                ButtonContent(text = text, leadingIcon = leadingIcon, trailingIcon = trailingIcon)
            }
        }

        AppButtonVariant.Text -> {
            TextButton(
                onClick = onClick,
                modifier = baseModifier,
                enabled = enabled,
                shape = ShapeControl,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                ButtonContent(text = text, leadingIcon = leadingIcon, trailingIcon = trailingIcon)
            }
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    leadingIcon: (@Composable () -> Unit)?,
    trailingIcon: (@Composable () -> Unit)?
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
        if (trailingIcon != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingIcon()
        }
    }
}

@Preview(name = "AppButton - Semua Varian", showBackground = true)
@Composable
private fun AppButtonPreview() {
    GeceSablonTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppButton(
                text = "Masuk",
                onClick = {},
                trailingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null
                    )
                }
            )
            AppButton(
                text = "Pesan Sekarang",
                onClick = {},
                variant = AppButtonVariant.Accent,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null
                    )
                }
            )
            AppButton(
                text = "Jelajahi sebagai Tamu",
                onClick = {},
                variant = AppButtonVariant.Secondary
            )
            AppButton(
                text = "Lupa Kata Sandi?",
                onClick = {},
                variant = AppButtonVariant.Text
            )
        }
    }
}
