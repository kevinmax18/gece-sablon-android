package com.gecesablon.app.ui.customer

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import com.gecesablon.app.R
import com.gecesablon.app.ui.account.AccountViewModel
import com.gecesablon.app.ui.components.AppTopBar
import com.gecesablon.app.ui.theme.GeceColors

/**
 * Layar Profil Pelanggan.
 * Menampilkan nama dari sesi, placeholder "—" untuk kontak (Tahap 8),
 * baris Tentang Aplikasi, dan tombol Keluar dengan dialog konfirmasi.
 *
 * @param viewModel       AccountViewModel bersama.
 * @param onNavigateBack  Kembali ke Beranda.
 * @param onNavigateToAbout Buka layar Tentang Aplikasi.
 * @param onLogout        Dipanggil setelah sesi dihapus; navigasi ke Welcome.
 */
@Composable
fun ProfileScreen(
    viewModel: AccountViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.logoutDone) {
        if (uiState.logoutDone) {
            viewModel.onLogoutNavigated()
            onLogout()
        }
    }

    // Dialog konfirmasi logout
    if (uiState.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissLogoutDialog,
            title = { Text(stringResource(R.string.profile_logout_dialog_title)) },
            text = { Text(stringResource(R.string.profile_logout_dialog_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::logout) {
                    Text(
                        text = stringResource(R.string.profile_logout_dialog_confirm),
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
            AppTopBar(
                title = stringResource(R.string.title_profile),
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Avatar + Nama
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(GeceColors.Accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = GeceColors.OnAccent,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Column {
                        Text(
                            text = uiState.name.ifBlank { stringResource(R.string.profile_name_placeholder) },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(R.string.profile_role_customer),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info kontak (placeholder hingga Tahap 8)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ProfileInfoRow(
                        icon = { Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) },
                        label = stringResource(R.string.profile_label_phone),
                        value = stringResource(R.string.profile_placeholder_contact)
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    ProfileInfoRow(
                        icon = { Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) },
                        label = stringResource(R.string.profile_label_email),
                        value = stringResource(R.string.profile_placeholder_contact)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Menu
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    // Tentang Aplikasi
                    ProfileMenuRow(
                        icon = { Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) },
                        label = stringResource(R.string.menu_about_app),
                        onClick = onNavigateToAbout
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    // Keluar
                    ProfileMenuRow(
                        icon = { Icon(Icons.Default.Logout, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                        label = stringResource(R.string.menu_logout),
                        labelColor = MaterialTheme.colorScheme.error,
                        onClick = viewModel::showLogoutDialog
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        icon()
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun ProfileMenuRow(
    icon: @Composable () -> Unit,
    label: String,
    labelColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onBackground,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            icon()
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = labelColor
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

