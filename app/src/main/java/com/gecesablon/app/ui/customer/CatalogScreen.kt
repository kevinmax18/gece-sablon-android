package com.gecesablon.app.ui.customer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gecesablon.app.BuildConfig
import com.gecesablon.app.R
import com.gecesablon.app.core.util.Formatter
import com.gecesablon.app.core.util.WhatsAppIntent
import com.gecesablon.app.data.model.ProductCategory
import com.gecesablon.app.ui.components.EmptyView
import com.gecesablon.app.ui.components.ErrorView
import com.gecesablon.app.ui.components.LoadingView
import com.gecesablon.app.ui.components.ProductCard
import com.gecesablon.app.ui.components.TopBarStandardActions
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.ShapeCard
import com.gecesablon.app.ui.theme.ShapeControl

/**
 * Layar Katalog Produk sesuai Figma 11-katalog-produk.png:
 * - Topbar dengan logo GeCe, judul, dan aksi WhatsApp & Profil
 * - Search bar untuk filter berdasarkan nama/bahan
 * - Chip filter kategori (Semua, Kaos Custom, Kaos Oversize, Baju Polo)
 * - Grid 2 kolom dengan ProductCard
 * - Penanganan state: loading, empty, dan error
 * - Banner bawah untuk konsultasi spek kustom via WhatsApp
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel,
    onProfileClick: () -> Unit,
    onProductClick: (productId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GeceColors.Background,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GeceColors.Accent)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.logo_gece),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.home_header_brand),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = stringResource(R.string.title_catalog),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    },
                    actions = {
                        TopBarStandardActions(
                            onChatClick = {
                                WhatsAppIntent.openChat(
                                    context = context,
                                    phoneNumber = BuildConfig.NOMOR_WA_WORKSHOP,
                                    message = context.getString(R.string.whatsapp_msg_consultation)
                                )
                            },
                            onProfileClick = onProfileClick
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = GeceColors.Background
                    )
                )
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Header Banner Title (Compact)
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(GeceColors.Accent)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = stringResource(R.string.catalog_badge),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.catalog_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = stringResource(R.string.catalog_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 2. Kolom Pencarian
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.catalog_search_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.content_desc_clear_text),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = ShapeControl,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = GeceColors.PrimaryInk,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 3. Chip Filter Kategori (Semua + 3 Kategori)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryChip(
                        label = stringResource(R.string.category_all),
                        isSelected = uiState.selectedCategory == null,
                        onClick = { viewModel.onCategorySelected(null) }
                    )
                    CategoryChip(
                        label = stringResource(ProductCategory.KAOS_CUSTOM.titleRes),
                        isSelected = uiState.selectedCategory == ProductCategory.KAOS_CUSTOM,
                        onClick = { viewModel.onCategorySelected(ProductCategory.KAOS_CUSTOM) }
                    )
                    CategoryChip(
                        label = stringResource(ProductCategory.KAOS_OVERSIZE.titleRes),
                        isSelected = uiState.selectedCategory == ProductCategory.KAOS_OVERSIZE,
                        onClick = { viewModel.onCategorySelected(ProductCategory.KAOS_OVERSIZE) }
                    )
                    CategoryChip(
                        label = stringResource(ProductCategory.BAJU_POLO.titleRes),
                        isSelected = uiState.selectedCategory == ProductCategory.BAJU_POLO,
                        onClick = { viewModel.onCategorySelected(ProductCategory.BAJU_POLO) }
                    )
                }
            }

            // 4. Daftar Produk (Grid 2 Kolom)
            when {
                uiState.isLoading -> {
                    item {
                        LoadingView(modifier = Modifier.fillMaxWidth().height(180.dp))
                    }
                }
                uiState.errorMessage != null -> {
                    item {
                        ErrorView(
                            message = uiState.errorMessage ?: stringResource(R.string.state_error_desc),
                            onRetry = { viewModel.loadProducts() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                uiState.products.isEmpty() -> {
                    item {
                        EmptyView(
                            title = stringResource(R.string.catalog_empty_title),
                            description = stringResource(R.string.catalog_empty_desc),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                        )
                    }
                }
                else -> {
                    val pairs = uiState.products.chunked(2)
                    items(pairs) { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ProductCard(
                                title = pair[0].nama,
                                materialSubtitle = pair[0].bahan,
                                priceFormatted = Formatter.formatRupiah(pair[0].hargaDasar),
                                imageUrl = pair[0].gambarUrl,
                                onClick = { onProductClick(pair[0].id) },
                                modifier = Modifier.weight(1f)
                            )
                            if (pair.size > 1) {
                                ProductCard(
                                    title = pair[1].nama,
                                    materialSubtitle = pair[1].bahan,
                                    priceFormatted = Formatter.formatRupiah(pair[1].hargaDasar),
                                    imageUrl = pair[1].gambarUrl,
                                    onClick = { onProductClick(pair[1].id) },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // 5. Banner Punya Spek Custom Sendiri? (Bespoke Studio)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ShapeCard)
                        .background(GeceColors.PrimaryInk)
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.catalog_custom_spec_badge),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                fontSize = 10.sp
                            ),
                            color = GeceColors.Accent
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = stringResource(R.string.catalog_custom_spec_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = GeceColors.Background
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = stringResource(R.string.catalog_custom_spec_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                WhatsAppIntent.openChat(
                                    context = context,
                                    phoneNumber = BuildConfig.NOMOR_WA_WORKSHOP,
                                    message = context.getString(R.string.whatsapp_msg_consultation)
                                )
                            },
                            shape = ShapeControl,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GeceColors.Accent,
                                contentColor = GeceColors.PrimaryInk
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.catalog_custom_spec_btn),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Text(
                                text = stringResource(R.string.catalog_badge_mockup),
                                style = MaterialTheme.typography.labelSmall,
                                color = GeceColors.Background
                            )
                            Text(
                                text = stringResource(R.string.catalog_badge_guarantee),
                                style = MaterialTheme.typography.labelSmall,
                                color = GeceColors.Background
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Chip kategori custom sesuai desain GeCe.
 */
@Composable
private fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) GeceColors.PrimaryInk else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) GeceColors.Background else MaterialTheme.colorScheme.onBackground
    val borderModifier = if (isSelected) {
        Modifier
    } else {
        Modifier.border(1.dp, MaterialTheme.colorScheme.outline, ShapeControl)
    }

    Box(
        modifier = modifier
            .clip(ShapeControl)
            .background(backgroundColor)
            .then(borderModifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = contentColor
        )
    }
}
