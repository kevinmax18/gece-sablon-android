package com.gecesablon.app.ui.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gecesablon.app.R
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.ui.components.AppButton
import com.gecesablon.app.ui.components.AppButtonVariant
import com.gecesablon.app.ui.theme.GeceColors
import kotlinx.coroutines.launch

private data class OnboardingPageData(
    val tagRes: Int,
    val hasTagCheck: Boolean = false,
    val titleRes: Int,
    val descRes: Int,
    val feat1Icon: ImageVector,
    val feat1TitleRes: Int,
    val feat1TagRes: Int,
    val feat1DescRes: Int,
    val feat2Icon: ImageVector,
    val feat2TitleRes: Int,
    val feat2TagRes: Int,
    val feat2DescRes: Int
)

/**
 * Layar Onboarding 3 slide sesuai Figma:
 * - Menampilkan fitur utama sablon DTF GeCe
 * - Indikator halaman dan tombol Lewati / Lanjut / Mulai Sekarang
 * - Menyimpan penanda onboardingCompleted di DataStore
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    sessionStore: SessionStore,
    onFinishOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })

    val pages = listOf(
        OnboardingPageData(
            tagRes = R.string.onboarding_tag_1,
            titleRes = R.string.onboarding_title_1,
            descRes = R.string.onboarding_desc_1,
            feat1Icon = Icons.Default.AllInclusive,
            feat1TitleRes = R.string.onboarding_feat_title_1_1,
            feat1TagRes = R.string.onboarding_feat_tag_1_1,
            feat1DescRes = R.string.onboarding_feat_desc_1_1,
            feat2Icon = Icons.Default.Print,
            feat2TitleRes = R.string.onboarding_feat_title_1_2,
            feat2TagRes = R.string.onboarding_feat_tag_1_2,
            feat2DescRes = R.string.onboarding_feat_desc_1_2
        ),
        OnboardingPageData(
            tagRes = R.string.onboarding_tag_2,
            hasTagCheck = true,
            titleRes = R.string.onboarding_title_2,
            descRes = R.string.onboarding_desc_2,
            feat1Icon = Icons.Default.Palette,
            feat1TitleRes = R.string.onboarding_feat_title_2_1,
            feat1TagRes = R.string.onboarding_feat_tag_2_1,
            feat1DescRes = R.string.onboarding_feat_desc_2_1,
            feat2Icon = Icons.Default.Checkroom,
            feat2TitleRes = R.string.onboarding_feat_title_2_2,
            feat2TagRes = R.string.onboarding_feat_tag_2_2,
            feat2DescRes = R.string.onboarding_feat_desc_2_2
        ),
        OnboardingPageData(
            tagRes = R.string.onboarding_tag_3,
            titleRes = R.string.onboarding_title_3,
            descRes = R.string.onboarding_desc_3,
            feat1Icon = Icons.Default.Notifications,
            feat1TitleRes = R.string.onboarding_feat_title_3_1,
            feat1TagRes = R.string.onboarding_feat_tag_3_1,
            feat1DescRes = R.string.onboarding_feat_desc_3_1,
            feat2Icon = Icons.Default.FactCheck,
            feat2TitleRes = R.string.onboarding_feat_title_3_2,
            feat2TagRes = R.string.onboarding_feat_tag_3_2,
            feat2DescRes = R.string.onboarding_feat_desc_3_2
        )
    )

    fun completeAndNavigate(action: () -> Unit) {
        coroutineScope.launch {
            sessionStore.setOnboardingCompleted(true)
            action()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = GeceColors.Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header: Studio Badge & Lewati Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge GeCe Studio
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_gece),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.onboarding_studio_badge),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (pagerState.currentPage > 0) {
                                Text(
                                    text = stringResource(R.string.onboarding_studio_sub),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Tombol Lewati
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { completeAndNavigate(onFinishOnboarding) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_skip),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.onboarding_skip),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress bar & Page Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indikator Baris
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (isSelected) 36.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) GeceColors.Accent else MaterialTheme.colorScheme.outline
                                )
                        )
                    }
                }

                // Text Counter "01 / 03"
                Text(
                    text = stringResource(
                        R.string.onboarding_page_counter,
                        pagerState.currentPage + 1,
                        3
                    ),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pager Konten Slide
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIdx ->
                val pageData = pages[pageIdx]
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.Top
                ) {
                    // Tag Kategori
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFE8E8FF))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (pageData.hasTagCheck) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF1E1B4B),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = stringResource(pageData.tagRes),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1E1B4B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Judul Utama Halaman
                    Text(
                        text = stringResource(pageData.titleRes),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Deskripsi Halaman
                    Text(
                        text = stringResource(pageData.descRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Kartu Fitur 1
                    FeatureCard(
                        icon = pageData.feat1Icon,
                        title = stringResource(pageData.feat1TitleRes),
                        tag = stringResource(pageData.feat1TagRes),
                        desc = stringResource(pageData.feat1DescRes)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Kartu Fitur 2
                    FeatureCard(
                        icon = pageData.feat2Icon,
                        title = stringResource(pageData.feat2TitleRes),
                        tag = stringResource(pageData.feat2TagRes),
                        desc = stringResource(pageData.feat2DescRes)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Kartu "Standar Sablon GeCe"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.onboarding_standard_label),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            // Swatch Bulatan Warna
                            Row(horizontalArrangement = Arrangement.spacedBy((-4).dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(GeceColors.PrimaryInk)
                                        .border(1.5.dp, Color.White, CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(GeceColors.Accent)
                                        .border(1.5.dp, Color.White, CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF334155))
                                        .border(1.5.dp, Color.White, CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tombol Aksi Utama (Lanjut / Mulai Sekarang)
            val isLastPage = pagerState.currentPage == 2
            AppButton(
                text = if (isLastPage) {
                    stringResource(R.string.onboarding_btn_start)
                } else {
                    stringResource(R.string.onboarding_btn_next)
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                variant = AppButtonVariant.Primary,
                onClick = {
                    if (isLastPage) {
                        completeAndNavigate(onFinishOnboarding)
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tautan "Sudah punya akun? Masuk"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.onboarding_have_account),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.onboarding_login_link),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.clickable {
                        completeAndNavigate(onNavigateToLogin)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    tag: String,
    desc: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFFFFF))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Wadah Ikon Kuning Aksen
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(GeceColors.Accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GeceColors.OnAccent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
