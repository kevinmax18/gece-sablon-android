package com.gecesablon.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gecesablon.app.R
import com.gecesablon.app.ui.components.AppButton
import com.gecesablon.app.ui.components.AppButtonVariant
import com.gecesablon.app.ui.components.AppTextField
import com.gecesablon.app.ui.components.AppTopBar
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.ShapeCard
import com.gecesablon.app.ui.theme.SurfaceCard
import com.gecesablon.app.ui.theme.TextPrimary
import com.gecesablon.app.ui.theme.TextSecondary
import kotlinx.coroutines.flow.collectLatest

/**
 * Layar formulir Ubah Kata Sandi (Change Password).
 * Digunakan oleh semua peran (Pelanggan, Admin, Owner).
 */
@Composable
fun ChangePasswordScreen(
    viewModel: ChangePasswordViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ChangePasswordEvent.Success -> {
                    snackbarHostState.showSnackbar(event.message)
                    onNavigateBack()
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GeceColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = stringResource(R.string.change_password_title),
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = stringResource(R.string.change_password_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.change_password_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Kolom Kata Sandi Lama
            AppTextField(
                value = uiState.oldPassword,
                onValueChange = viewModel::onOldPasswordChange,
                label = stringResource(R.string.change_password_old_label),
                placeholder = stringResource(R.string.change_password_old_placeholder),
                isPassword = true,
                isError = uiState.oldPasswordError != null,
                errorMessage = uiState.oldPasswordError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Kolom Kata Sandi Baru
            AppTextField(
                value = uiState.newPassword,
                onValueChange = viewModel::onNewPasswordChange,
                label = stringResource(R.string.change_password_new_label),
                placeholder = stringResource(R.string.change_password_new_placeholder),
                isPassword = true,
                isError = uiState.newPasswordError != null,
                errorMessage = uiState.newPasswordError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Kolom Konfirmasi Kata Sandi Baru
            AppTextField(
                value = uiState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = stringResource(R.string.change_password_confirm_label),
                placeholder = stringResource(R.string.change_password_confirm_placeholder),
                isPassword = true,
                isError = uiState.confirmPasswordError != null,
                errorMessage = uiState.confirmPasswordError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.submit()
                    }
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Kotak bantuan aturan kata sandi
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, shape = ShapeCard)
                    .padding(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.change_password_hint_rules),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = TextSecondary
                )
            }

            if (uiState.generalError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.generalError.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AppButton(
                text = stringResource(R.string.change_password_btn_submit),
                onClick = {
                    focusManager.clearFocus()
                    viewModel.submit()
                },
                enabled = !uiState.isLoading,
                variant = AppButtonVariant.Primary
            )
        }
    }
}
