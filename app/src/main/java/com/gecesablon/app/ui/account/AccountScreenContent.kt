package com.gecesablon.app.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gecesablon.app.BuildConfig
import com.gecesablon.app.R
import com.gecesablon.app.ui.components.OwnerHeader
import com.gecesablon.app.ui.theme.AccentYellow
import com.gecesablon.app.ui.theme.BorderHairline
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.InkBlack
import com.gecesablon.app.ui.theme.ShapeBadge
import com.gecesablon.app.ui.theme.ShapeCard
import com.gecesablon.app.ui.theme.ShapeControl
import com.gecesablon.app.ui.theme.SurfaceCard
import com.gecesablon.app.ui.theme.TextPrimary
import com.gecesablon.app.ui.theme.TextSecondary
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.SupervisorAccount

/**
 * Komposabel bersama layar Akun (Owner & Admin) yang diselaraskan dengan Figma 33 dan 25.
 * Perbedaan role dipenuhi melalui parameter headerTitle, roleBadge, dan accessText.
 */
@Composable
fun AccountScreenContent(
    viewModel: AccountViewModel,
    headerTitle: String,
    headerSubtitle: String,
    roleBadge: String,
    accessText: String,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToManageProducts: (() -> Unit)? = null,
    onNavigateToManageAdmins: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.logoutDone) {
        if (uiState.logoutDone) {
            viewModel.onLogoutNavigated()
            onLogout()
        }
    }

    if (uiState.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissLogoutDialog,
            title = { Text(stringResource(R.string.profile_logout_dialog_title)) },
            text = { Text(stringResource(R.string.profile_logout_dialog_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::logout) {
                    Text(
                        stringResource(R.string.profile_logout_dialog_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissLogoutDialog) {
                    Text(stringResource(R.string.profile_logout_dialog_cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GeceColors.Background,
        topBar = {
            OwnerHeader(
                title = headerTitle,
                subtitle = headerSubtitle,
                roleBadge = roleBadge
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // -------------------------------------------------------------
            // Kartu Profil (Avatar lingkaran hitam + inisial + centang kuning, nama, badge role, kotak hak akses)
            // -------------------------------------------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ShapeCard,
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderHairline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar inisial 2 huruf dengan lencana centang kuning kecil di pojok
                        val initials = getInitials(uiState.name.ifBlank { "User" })
                        Box(modifier = Modifier.size(60.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(InkBlack),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    ),
                                    color = GeceColors.Background
                                )
                            }

                            // Centang kuning kecil
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(AccentYellow)
                                    .border(1.5.dp, SurfaceCard, CircleShape)
                                    .align(Alignment.BottomEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = InkBlack,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.name.ifBlank { stringResource(R.string.profile_name_placeholder) },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Badge Kuning Role
                            Box(
                                modifier = Modifier
                                    .clip(ShapeBadge)
                                    .background(AccentYellow)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = roleBadge,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = InkBlack
                                )
                            }

                            // TODO: tampilkan dari endpoint profil saat integrasi API (email & no HP tidak ada di sesi)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Kotak Hak Akses
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(ShapeControl)
                            .background(GeceColors.Background)
                            .border(1.dp, BorderHairline, ShapeControl)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = InkBlack,
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = accessText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                ),
                                color = TextPrimary,
                                modifier = Modifier.weight(1f),
                                softWrap = true
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // Bagian INFORMASI AKUN (Ubah Kata Sandi)
            // -------------------------------------------------------------
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.account_section_info),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderHairline)
                ) {
                    AccountMenuRow(
                        icon = Icons.Default.LockReset,
                        title = stringResource(R.string.account_menu_change_password),
                        subtitle = stringResource(R.string.account_menu_change_password_sub),
                        onClick = onNavigateToChangePassword
                    )
                }
            }

            // -------------------------------------------------------------
            // Bagian PENGELOLAAN WORKSHOP (Khusus Owner: Kelola Produk)
            // -------------------------------------------------------------
            if (onNavigateToManageProducts != null) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.account_section_management),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ShapeCard,
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderHairline)
                    ) {
                        AccountMenuRow(
                            icon = Icons.Default.Layers,
                            title = stringResource(R.string.account_menu_manage_products),
                            subtitle = stringResource(R.string.account_menu_manage_products_sub),
                            onClick = onNavigateToManageProducts
                        )
                        if (onNavigateToManageAdmins != null) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = BorderHairline
                            )
                            AccountMenuRow(
                                icon = Icons.Default.SupervisorAccount,
                                title = stringResource(R.string.account_menu_manage_admins),
                                subtitle = stringResource(R.string.account_menu_manage_admins_sub),
                                onClick = onNavigateToManageAdmins
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // Bagian BANTUAN & LEGAL (Tentang Aplikasi + Label Versi)
            // -------------------------------------------------------------
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.account_section_help),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderHairline)
                ) {
                    AccountMenuRow(
                        icon = Icons.Default.Info,
                        title = stringResource(R.string.account_menu_about),
                        subtitle = "GeCe Screen Printing App",
                        trailingTag = "v${BuildConfig.VERSION_NAME}",
                        onClick = onNavigateToAbout
                    )
                }
            }

            // -------------------------------------------------------------
            // Tombol Keluar dari Akun (Latar merah muda, teks merah)
            // -------------------------------------------------------------
            Button(
                onClick = viewModel::showLogoutDialog,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = ShapeControl,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GeceColors.StatusCancelledBg,
                    contentColor = GeceColors.StatusCancelledText
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.account_btn_logout),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

/**
 * Baris item menu di layar akun.
 */
@Composable
private fun AccountMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    trailingTag: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(GeceColors.Background),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (trailingTag != null) {
            Box(
                modifier = Modifier
                    .clip(ShapeBadge)
                    .background(GeceColors.Background)
                    .border(1.dp, BorderHairline, ShapeBadge)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = trailingTag,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * Helper pembuat inisial 2 huruf dari nama pengguna.
 */
private fun getInitials(name: String): String {
    val tokens = name.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
    return when {
        tokens.isEmpty() -> "GC"
        tokens.size == 1 -> tokens[0].take(2).uppercase()
        else -> "${tokens[0].first()}${tokens[1].first()}".uppercase()
    }
}
