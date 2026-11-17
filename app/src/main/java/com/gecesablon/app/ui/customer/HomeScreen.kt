package com.gecesablon.app.ui.customer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.model.ProductCategory
import com.gecesablon.app.ui.components.ErrorView
import com.gecesablon.app.ui.components.LoadingView
import com.gecesablon.app.ui.components.ProductCard
import com.gecesablon.app.ui.components.TopBarStandardActions
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.ShapeBadge
import com.gecesablon.app.ui.theme.ShapeCard
import com.gecesablon.app.ui.theme.ShapeControl

/**
 * Layar Beranda Pelanggan sesuai Figma 10-beranda-tamu.png:
 * - TopBar identitas GeCe, tombol kontak WhatsApp, dan Profil.
 * - Banner DTF Premium dengan tombol "Pesan Sekarang" yang membuka Katalog.
 * - Kategori Produk (Kaos Custom, Kaos Oversize, Baju Polo).
 * - Portofolio/Produk Unggulan (Grid 2 Kolom menggunakan ProductCard).
 * - Kartu Konsultasi Gratis WhatsApp.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    viewModel: HomeCustomerViewModel,
    onProfileClick: () -> Unit,
    onProductClick: (productId: String) -> Unit,
    onNavigateToCatalog: (category: ProductCategory?) -> Unit,
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
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GeceColors.Accent)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.logo_gece),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
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
                                    text = stringResource(R.string.title_home),
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Tag & Headline Sambutan
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
                            text = stringResource(R.string.home_badge_specialist),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stringResource(R.string.home_welcome_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = stringResource(R.string.home_welcome_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 2. Banner Utama Sablon DTF Kaos & Polo (Hitam)
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
                            text = stringResource(R.string.home_banner_badge),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                fontSize = 10.sp
                            ),
                            color = GeceColors.Accent
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = stringResource(R.string.home_banner_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = GeceColors.Background
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = stringResource(R.string.home_banner_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { onNavigateToCatalog(null) },
                                shape = ShapeControl,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GeceColors.Accent,
                                    contentColor = GeceColors.PrimaryInk
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.home_banner_btn),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(ShapeBadge)
                                    .background(GeceColors.Accent)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.home_banner_tag),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = GeceColors.PrimaryInk
                                )
                            }
                        }
                    }
                }
            }

            // 3. Kategori Produk
            item {
                Column {
                    Text(
                        text = stringResource(R.string.home_category_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.home_category_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CategoryCard(
                            title = stringResource(R.string.category_kaos_custom),
                            subtitle = stringResource(R.string.home_category_kaos_custom_desc),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToCatalog(ProductCategory.KAOS_CUSTOM) }
                        )
                        CategoryCard(
                            title = stringResource(R.string.category_kaos_oversize),
                            subtitle = stringResource(R.string.home_category_kaos_oversize_desc),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToCatalog(ProductCategory.KAOS_OVERSIZE) }
                        )
                        CategoryCard(
                            title = stringResource(R.string.category_baju_polo),
                            subtitle = stringResource(R.string.home_category_baju_polo_desc),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToCatalog(ProductCategory.BAJU_POLO) }
                        )
                    }
                }
            }

            // 4. Portofolio Kami / Produk Unggulan
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.home_portfolio_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(R.string.home_portfolio_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onNavigateToCatalog(null) }
                            .padding(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.home_see_all),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            // Grid Produk Unggulan (Pasangan 2-Kolom)
            when {
                uiState.isLoading -> {
                    item {
                        LoadingView(modifier = Modifier.fillMaxWidth().height(160.dp))
                    }
                }
                uiState.errorMessage != null -> {
                    item {
                        ErrorView(
                            message = uiState.errorMessage ?: stringResource(R.string.state_error_desc),
                            onRetry = { viewModel.loadFeaturedProducts() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                else -> {
                    val chunked = uiState.featuredProducts.chunked(2)
                    items(chunked) { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
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

            // 5. Konsultasi Gratis WhatsApp
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ShapeCard)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, ShapeCard)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(ShapeControl)
                                .background(GeceColors.Accent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = GeceColors.PrimaryInk,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.home_consultation_title),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = stringResource(R.string.home_consultation_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

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
                            containerColor = GeceColors.PrimaryInk,
                            contentColor = GeceColors.Background
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.home_consultation_btn),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Kartu mini kategori pada Beranda Pelanggan.
 */
@Composable
private fun CategoryCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(ShapeControl)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, ShapeControl)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
