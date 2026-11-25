package com.gecesablon.app.ui.owner

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gecesablon.app.R
import com.gecesablon.app.ui.account.AccountScreenContent
import com.gecesablon.app.ui.account.AccountViewModel

/**
 * Layar Akun Owner (Figma 33-akun-profil.png).
 * Menggunakan AccountScreenContent bersama.
 */
@Composable
fun OwnerAccountScreen(
    viewModel: AccountViewModel,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToManageProducts: () -> Unit = {},
    onNavigateToManageCategories: () -> Unit = {},
    onNavigateToManageAdmins: () -> Unit = {}
) {
    AccountScreenContent(
        viewModel = viewModel,
        headerTitle = stringResource(R.string.account_header_gece),
        headerSubtitle = stringResource(R.string.account_header_title),
        roleBadge = stringResource(R.string.account_badge_owner),
        accessText = stringResource(R.string.account_access_owner),
        onNavigateToChangePassword = onNavigateToChangePassword,
        onNavigateToAbout = onNavigateToAbout,
        onLogout = onLogout,
        onNavigateToManageProducts = onNavigateToManageProducts,
        onNavigateToManageCategories = onNavigateToManageCategories,
        onNavigateToManageAdmins = onNavigateToManageAdmins,
        modifier = modifier
    )
}
