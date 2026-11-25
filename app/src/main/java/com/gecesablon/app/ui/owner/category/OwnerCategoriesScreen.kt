package com.gecesablon.app.ui.owner.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gecesablon.app.R
import com.gecesablon.app.data.model.Category
import com.gecesablon.app.ui.components.EmptyState
import com.gecesablon.app.ui.components.ErrorState
import com.gecesablon.app.ui.components.LoadingState
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

/**
 * Layar Kelola Kategori Produk Owner:
 * - Menampilkan daftar kategori produk
 * - Aksi Tambah Kategori (Dialog)
 * - Aksi Ubah Nama Kategori (Dialog)
 * - Aksi Hapus Kategori dengan konfirmasi yang menyebut nama kategori
 * - State loading, empty, dan error
 * - Didesain aman pada layar 320-360 dp
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerCategoriesScreen(
    viewModel: OwnerCategoriesViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    // Dialog Tambah / Ubah Kategori
    if (uiState.showFormDialog) {
        val isEdit = uiState.editingCategory != null
        AlertDialog(
            onDismissRequest = { if (!uiState.isActionInProgress) viewModel.dismissFormDialog() },
            title = {
                Text(
                    text = stringResource(
                        if (isEdit) R.string.owner_categories_dialog_edit_title else R.string.owner_categories_dialog_add_title
                    ),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.owner_categories_name_label),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = uiState.formNamaInput,
                        onValueChange = viewModel::onFormNamaChanged,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.owner_categories_name_placeholder),
                                color = TextSecondary
                            )
                        },
                        isError = uiState.formNamaError != null,
                        singleLine = true,
                        shape = ShapeControl,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = InkBlack,
                            unfocusedBorderColor = BorderHairline,
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard
                        )
                    )
                    uiState.formNamaError?.let { err ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = err,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::submitCategoryForm,
                    enabled = !uiState.isActionInProgress,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = InkBlack,
                        contentColor = Color.White
                    ),
                    shape = ShapeControl
                ) {
                    if (uiState.isActionInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.owner_categories_dialog_save))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = viewModel::dismissFormDialog,
                    enabled = !uiState.isActionInProgress
                ) {
                    Text(stringResource(R.string.owner_categories_dialog_cancel), color = TextSecondary)
                }
            },
            containerColor = SurfaceCard,
            shape = ShapeCard
        )
    }

    // Dialog Konfirmasi Hapus
    uiState.categoryToDelete?.let { cat ->
        AlertDialog(
            onDismissRequest = { if (!uiState.isActionInProgress) viewModel.dismissDeleteDialog() },
            title = {
                Text(
                    text = stringResource(R.string.owner_categories_dialog_delete_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.owner_categories_dialog_delete_msg, cat.nama),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = viewModel::confirmDeleteCategory,
                    enabled = !uiState.isActionInProgress,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    ),
                    shape = ShapeControl
                ) {
                    if (uiState.isActionInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.owner_categories_dialog_delete_confirm))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = viewModel::dismissDeleteDialog,
                    enabled = !uiState.isActionInProgress
                ) {
                    Text(stringResource(R.string.owner_categories_dialog_cancel), color = TextSecondary)
                }
            },
            containerColor = SurfaceCard,
            shape = ShapeCard
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GeceColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            OwnerHeader(
                title = stringResource(R.string.owner_categories_title),
                subtitle = stringResource(R.string.owner_categories_subtitle),
                roleBadge = stringResource(R.string.owner_categories_badge_role),
                showBack = true,
                onBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Baris Tombol Tambah Kategori
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Kategori (${uiState.categories.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = viewModel::openAddDialog,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = InkBlack,
                        contentColor = Color.White
                    ),
                    shape = ShapeControl,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.owner_categories_btn_add),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    LoadingState(modifier = Modifier.fillMaxSize())
                }
                uiState.errorMessage != null && uiState.categories.isEmpty() -> {
                    ErrorState(
                        modifier = Modifier.fillMaxSize(),
                        title = stringResource(R.string.state_error_title),
                        description = uiState.errorMessage ?: stringResource(R.string.state_error_desc),
                        onRetry = viewModel::retryLoad
                    )
                }
                uiState.categories.isEmpty() -> {
                    EmptyState(
                        title = stringResource(R.string.owner_categories_empty_title),
                        description = stringResource(R.string.owner_categories_empty_desc),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.categories, key = { it.id }) { cat ->
                            CategoryItemCard(
                                category = cat,
                                onEdit = { viewModel.openEditDialog(cat) },
                                onDelete = { viewModel.openDeleteDialog(cat) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Kartu per baris kategori produk untuk Owner.
 */
@Composable
private fun CategoryItemCard(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderHairline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(ShapeControl)
                    .background(GeceColors.Background)
                    .border(1.dp, BorderHairline, ShapeControl),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = InkBlack,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.nama,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "ID: ${category.id}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Ubah Kategori",
                        tint = InkBlack,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Kategori",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
