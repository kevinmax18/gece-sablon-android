package com.gecesablon.app.ui.owner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gecesablon.app.R
import com.gecesablon.app.ui.components.EmptyState
import com.gecesablon.app.ui.components.LoadingState
import com.gecesablon.app.ui.owner.admin.OwnerAdminDetailViewModel
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
import com.gecesablon.app.ui.theme.White

/**
 * Layar Detail Akun Admin oleh Owner.
 * Fitur:
 * - Mengubah nama admin
 * - Mengaktifkan / Menonaktifkan akun dengan dialog konfirmasi
 * - Mengatur ulang kata sandi oleh Owner
 */
@Composable
fun OwnerAdminDetailScreen(
    viewModel: OwnerAdminDetailViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.successSnackbarMessage) {
        uiState.successSnackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbarMessage()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    if (uiState.showStatusDialog && uiState.admin != null) {
        val admin = uiState.admin!!
        val isDeactivating = admin.isActive
        AlertDialog(
            onDismissRequest = { viewModel.showStatusDialog(false) },
            title = {
                Text(
                    text = if (isDeactivating) {
                        stringResource(R.string.owner_admin_detail_dialog_deactivate_title)
                    } else {
                        stringResource(R.string.owner_admin_detail_dialog_activate_title)
                    }
                )
            },
            text = {
                Text(
                    text = if (isDeactivating) {
                        stringResource(R.string.owner_admin_detail_dialog_deactivate_msg, admin.name)
                    } else {
                        stringResource(R.string.owner_admin_detail_dialog_activate_msg, admin.name)
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = viewModel::toggleStatus,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDeactivating) GeceColors.StatusCancelledText else AccentYellow,
                        contentColor = if (isDeactivating) White else InkBlack
                    )
                ) {
                    Text(stringResource(R.string.owner_admin_detail_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showStatusDialog(false) }) {
                    Text(stringResource(R.string.owner_admin_detail_dialog_cancel), color = TextPrimary)
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard)
                    .border(width = 0.5.dp, color = BorderHairline)
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.owner_admin_detail_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = GeceColors.Background
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                LoadingState(modifier = Modifier.padding(innerPadding))
            }
            uiState.admin == null -> {
                EmptyState(
                    title = "Akun Tidak Ditemukan",
                    description = uiState.errorMessage ?: "Data staf admin tidak tersedia.",
                    modifier = Modifier.padding(innerPadding)
                )
            }
            else -> {
                val admin = uiState.admin!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    // Header Profil Ringkas
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ShapeCard,
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, BorderHairline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(ShapeControl)
                                    .background(GeceColors.Background),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = TextPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = admin.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(ShapeBadge)
                                            .background(AccentYellow)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.owner_admin_role_badge),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            color = InkBlack
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(ShapeBadge)
                                            .background(
                                                if (admin.isActive) GeceColors.StatusSuccessBg else GeceColors.StatusCancelledBg
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (admin.isActive) {
                                                stringResource(R.string.owner_admin_status_active)
                                            } else {
                                                stringResource(R.string.owner_admin_status_inactive)
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            color = if (admin.isActive) GeceColors.StatusSuccessText else GeceColors.StatusCancelledText
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Bagian INFORMASI AKUN (Ubah Nama)
                    Text(
                        text = stringResource(R.string.owner_admin_detail_section_info),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ShapeCard,
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, BorderHairline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Email (hanya baca)
                            Text(
                                text = stringResource(R.string.owner_admin_form_email_label),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = admin.email,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = TextPrimary
                            )

                            if (admin.phone.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = stringResource(R.string.owner_admin_form_phone_label),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = admin.phone,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = TextPrimary
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = BorderHairline
                            )

                            // Edit Nama
                            Text(
                                text = stringResource(R.string.owner_admin_form_name_label),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = uiState.editedName,
                                onValueChange = viewModel::onNameChange,
                                modifier = Modifier.fillMaxWidth(),
                                shape = ShapeControl,
                                singleLine = true,
                                isError = uiState.nameError != null,
                                supportingText = uiState.nameError?.let { { Text(it) } },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = InkBlack,
                                    unfocusedBorderColor = BorderHairline,
                                    focusedContainerColor = SurfaceCard,
                                    unfocusedContainerColor = SurfaceCard
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = viewModel::saveName,
                                enabled = !uiState.isSavingName && uiState.editedName.isNotBlank() && uiState.editedName != admin.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = ShapeControl,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentYellow,
                                    contentColor = InkBlack
                                )
                            ) {
                                if (uiState.isSavingName) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = InkBlack,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = stringResource(R.string.owner_admin_detail_btn_save_name),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Bagian PENGATURAN KEAMANAN (Atur Ulang Sandi & Status)
                    Text(
                        text = stringResource(R.string.owner_admin_detail_section_security),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ShapeCard,
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, BorderHairline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Atur Ulang Kata Sandi
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LockReset,
                                    contentDescription = null,
                                    tint = TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.owner_admin_detail_reset_password_title),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.owner_admin_detail_reset_password_desc),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = uiState.newPasswordInput,
                                onValueChange = viewModel::onNewPasswordChange,
                                placeholder = { Text("Kata sandi baru (min. 8 karakter)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = ShapeControl,
                                singleLine = true,
                                visualTransformation = if (uiState.isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    IconButton(onClick = viewModel::toggleNewPasswordVisibility) {
                                        Icon(
                                            imageVector = if (uiState.isNewPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null
                                        )
                                    }
                                },
                                isError = uiState.newPasswordError != null,
                                supportingText = uiState.newPasswordError?.let { { Text(it) } },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = InkBlack,
                                    unfocusedBorderColor = BorderHairline,
                                    focusedContainerColor = SurfaceCard,
                                    unfocusedContainerColor = SurfaceCard
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = viewModel::resetPassword,
                                enabled = !uiState.isResettingPassword && uiState.newPasswordInput.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = ShapeControl,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = InkBlack,
                                    contentColor = AccentYellow
                                )
                            ) {
                                if (uiState.isResettingPassword) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = AccentYellow,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = stringResource(R.string.owner_admin_detail_reset_password_btn),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                color = BorderHairline
                            )

                            // Tombol Aktifkan / Nonaktifkan Akun
                            if (admin.isActive) {
                                OutlinedButton(
                                    onClick = { viewModel.showStatusDialog(true) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp),
                                    shape = ShapeControl,
                                    border = BorderStroke(1.dp, GeceColors.StatusCancelledText),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = GeceColors.StatusCancelledText
                                    )
                                ) {
                                    Text(
                                        text = stringResource(R.string.owner_admin_detail_btn_deactivate),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.showStatusDialog(true) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp),
                                    shape = ShapeControl,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GeceColors.StatusSuccessBg,
                                        contentColor = GeceColors.StatusSuccessText
                                    )
                                ) {
                                    Text(
                                        text = stringResource(R.string.owner_admin_detail_btn_activate),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
