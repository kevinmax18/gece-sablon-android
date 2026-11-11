package com.gecesablon.app.ui.staff

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
 * Layar Proses Workshop yang digunakan bersama oleh Admin Workshop dan Owner.
 */
@Composable
fun StaffProcessScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppTopBar(title = stringResource(R.string.title_staff_process))
        }
    ) { innerPadding ->
        EmptyState(
            title = stringResource(R.string.state_coming_soon_title),
            description = stringResource(R.string.state_coming_soon_desc),
            modifier = Modifier.padding(innerPadding)
        )
    }
}
