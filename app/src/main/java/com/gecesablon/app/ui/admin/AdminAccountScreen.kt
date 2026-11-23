package com.gecesablon.app.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gecesablon.app.R
import com.gecesablon.app.ui.account.AccountScreenContent
import com.gecesablon.app.ui.account.AccountViewModel

/**
 * Layar Akun Admin Workshop (Figma 25-akun-profil.png).
 * Menggunakan AccountScreenContent bersama.
 */
@Composable
fun AdminAccountScreen(
    viewModel: AccountViewModel,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    AccountScreenContent(
        viewModel = viewModel,
        headerTitle = stringResource(R.string.account_header_gece),
        headerSubtitle = stringResource(R.string.account_header_title),
        roleBadge = stringResource(R.string.account_badge_admin),
        accessText = stringResource(R.string.account_access_admin),
        onNavigateToChangePassword = onNavigateToChangePassword,
        onNavigateToAbout = onNavigateToAbout,
        onLogout = onLogout,
        modifier = modifier
    )
}
