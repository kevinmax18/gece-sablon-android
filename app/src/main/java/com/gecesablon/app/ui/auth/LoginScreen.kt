package com.gecesablon.app.ui.auth

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gecesablon.app.R
import com.gecesablon.app.ui.components.AppTopBar
import com.gecesablon.app.ui.components.EmptyState

/**
 * Layar sementara Login (otentikasi penuh akan diimplementasikan di Tahap 3).
 */
@Composable
fun LoginScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                title = stringResource(R.string.title_login),
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        EmptyState(
            title = stringResource(R.string.title_login),
            description = stringResource(R.string.state_coming_soon_desc),
            modifier = Modifier.padding(innerPadding)
        )
    }
}
