package com.gecesablon.app.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gecesablon.app.BuildConfig
import com.gecesablon.app.R
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.ui.theme.GeceColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

/**
 * Layar Splash Screen saat aplikasi pertama kali dibuka:
 * - Menampilkan logo GeCe, judul, dan tagline sesuai Figma
 * - Tunda ±1,5 detik
 * - Mengarahkan ke NavGraph sesuai role jika sesi ada, Onboarding jika belum selesai, atau Welcome
 */
@Composable
fun SplashScreen(
    sessionStore: SessionStore,
    onNavigateToCustomer: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToOwner: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToWelcome: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        val role = sessionStore.userRole.first()
        val onboardingDone = sessionStore.isOnboardingCompleted.first()
        delay(1500)

        when {
            role == Role.CUSTOMER -> onNavigateToCustomer()
            role == Role.ADMIN -> onNavigateToAdmin()
            role == Role.OWNER -> onNavigateToOwner()
            !onboardingDone -> onNavigateToOnboarding()
            else -> onNavigateToWelcome()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = GeceColors.Background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Wadah Logo GeCe
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(GeceColors.Background)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_gece),
                        contentDescription = stringResource(R.string.splash_brand_title),
                        modifier = Modifier.size(76.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Judul Brand "GeCe"
                Text(
                    text = stringResource(R.string.splash_brand_title),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Pill "SCREEN PRINTING"
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.splash_studio_pill),
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Subtitle Tagline
                Text(
                    text = stringResource(R.string.splash_tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(260.dp)
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Garis Aksen & Status Loading
                Box(
                    modifier = Modifier
                        .width(190.dp)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(GeceColors.Accent)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(GeceColors.Accent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.splash_loading),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Footer versi di bagian paling bawah
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = stringResource(R.string.splash_footer, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
