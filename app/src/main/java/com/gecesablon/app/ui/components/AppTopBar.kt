package com.gecesablon.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gecesablon.app.R
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.GeceSablonTheme

/**
 * Tombol aksi ikon top bar dengan lingkaran 36 dp dan ikon 18-20 dp sesuai Figma.
 * Menggunakan clickable tanpa batasan minimum touch target 48 dp yang membesarkan top bar.
 */
@Composable
fun TopBarActionButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    iconTint: Color = MaterialTheme.colorScheme.onBackground
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Pasangan aksi top bar standar GeCe (Chat WhatsApp & Profil Pengguna).
 */
@Composable
fun TopBarStandardActions(
    onChatClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TopBarActionButton(
            icon = Icons.Default.Chat,
            contentDescription = stringResource(R.string.content_desc_whatsapp),
            onClick = onChatClick,
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            iconTint = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.width(8.dp))
        TopBarActionButton(
            icon = Icons.Default.Person,
            contentDescription = stringResource(R.string.content_desc_profile),
            onClick = onProfileClick,
            backgroundColor = GeceColors.PrimaryInk,
            iconTint = GeceColors.Background
        )
        Spacer(modifier = Modifier.width(12.dp))
    }
}

/**
 * Komponen top app bar bersama dengan navigasi kembali, judul, subtitle, dan aksi kustom.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    canNavigateBack: Boolean = false,
    onNavigateBack: () -> Unit = {},
    showBottomDivider: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth()) {
        TopAppBar(
            title = {
                Column {
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            navigationIcon = {
                if (canNavigateBack) {
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onNavigateBack
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_back),
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = GeceColors.Background,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                actionIconContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        if (showBottomDivider) {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Preview(name = "AppTopBar - Dengan Tombol Kembali", showBackground = true)
@Composable
private fun AppTopBarWithBackPreview() {
    GeceSablonTheme {
        AppTopBar(
            title = "Detail Pesanan",
            subtitle = "RINGKASAN",
            canNavigateBack = true,
            onNavigateBack = {}
        )
    }
}

@Preview(name = "AppTopBar - Halaman Utama dengan Aksi", showBackground = true)
@Composable
private fun AppTopBarMainPreview() {
    GeceSablonTheme {
        AppTopBar(
            title = "Manajemen Pesanan",
            subtitle = "ADMIN WORKSHOP",
            canNavigateBack = false,
            actions = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = stringResource(R.string.content_desc_profile)
                    )
                }
            }
        )
    }
}
