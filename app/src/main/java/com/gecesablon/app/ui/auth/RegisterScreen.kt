package com.gecesablon.app.ui.auth

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gecesablon.app.R
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.ui.components.AppButton
import com.gecesablon.app.ui.components.AppButtonVariant
import com.gecesablon.app.ui.components.AppTextField
import com.gecesablon.app.ui.components.AppTopBar
import com.gecesablon.app.ui.theme.GeceColors
import androidx.compose.ui.text.withStyle

/**
 * Layar Register sesuai Figma 06-register-screen.png.
 * Validasi dilakukan di RegisterViewModel. Role tidak dipilih; selalu CUSTOMER.
 *
 * @param viewModel             ViewModel yang diinjeksikan dari NavHost.
 * @param onNavigateBack        Kembali (ke Login).
 * @param onNavigateToLogin     Buka Login dari tautan "Sudah punya akun? Masuk".
 * @param onRegisterSuccess     Dipanggil dengan Session dan redirect setelah register berhasil.
 */
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: (redirect: String?) -> Unit,
    onRegisterSuccess: (session: Session, redirect: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    var showTermsDialog by remember { mutableStateOf(false) }

    // Navigasi setelah register berhasil
    LaunchedEffect(uiState.registerSuccess) {
        val session = uiState.registerSuccess ?: return@LaunchedEffect
        viewModel.onNavigated()
        onRegisterSuccess(session, uiState.redirect)
    }

    // Dialog Syarat & Ketentuan / Kebijakan Privasi (sementara)
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text(stringResource(R.string.register_terms_dialog_title)) },
            text = { Text(stringResource(R.string.register_terms_dialog_body)) },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text(stringResource(R.string.register_terms_dialog_close))
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GeceColors.Background,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.register_topbar_title),
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Logo GeCe
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_gece),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.register_title),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.register_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ---- Field Nama Lengkap ----
            FormLabel(stringResource(R.string.register_label_name))
            AppTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                placeholder = stringResource(R.string.register_placeholder_name),
                leadingIcon = {
                    Icon(Icons.Default.Person, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp))
                },
                isError = uiState.nameError != null,
                errorMessage = uiState.nameError,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---- Field No. HP/WhatsApp ----
            FormLabel(stringResource(R.string.register_label_phone))
            AppTextField(
                value = uiState.phone,
                onValueChange = viewModel::onPhoneChange,
                placeholder = stringResource(R.string.register_placeholder_phone),
                leadingIcon = {
                    Icon(Icons.Default.Phone, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp))
                },
                isError = uiState.phoneError != null,
                errorMessage = uiState.phoneError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---- Field Email ----
            FormLabel(stringResource(R.string.register_label_email))
            AppTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                placeholder = stringResource(R.string.register_placeholder_email),
                leadingIcon = {
                    Icon(Icons.Default.Email, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp))
                },
                isError = uiState.emailError != null,
                errorMessage = uiState.emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---- Field Password ----
            FormLabel(stringResource(R.string.register_label_password))
            AppTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = stringResource(R.string.register_placeholder_password),
                isPassword = true,
                leadingIcon = {
                    Icon(Icons.Default.Lock, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp))
                },
                isError = uiState.passwordError != null,
                errorMessage = uiState.passwordError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---- Field Konfirmasi Password ----
            FormLabel(stringResource(R.string.register_label_confirm_password))
            AppTextField(
                value = uiState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                placeholder = stringResource(R.string.register_placeholder_confirm_password),
                isPassword = true,
                leadingIcon = {
                    Icon(Icons.Default.Refresh, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp))
                },
                isError = uiState.confirmPasswordError != null,
                errorMessage = uiState.confirmPasswordError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    viewModel.register()
                }),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ---- Checkbox Syarat & Ketentuan ----
            Column {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = uiState.agreedToTerms,
                        onCheckedChange = viewModel::onAgreedToTermsChange,
                        enabled = !uiState.isLoading,
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            checkmarkColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = buildAnnotatedString {
                            append(stringResource(R.string.register_terms_prefix))
                            append(" ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
                                append(stringResource(R.string.register_terms_link))
                            }
                            append(" ")
                            append(stringResource(R.string.register_terms_and))
                            append(" ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
                                append(stringResource(R.string.register_privacy_link))
                            }
                            append(" ")
                            append(stringResource(R.string.register_terms_suffix))
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.clickable { showTermsDialog = true }
                    )
                }
                if (uiState.termsError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.termsError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 28.dp)
                    )
                }
            }

            // Error global (dari repository, misal duplikat email)
            if (!uiState.globalError.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.globalError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tombol Daftar
            AppButton(
                text = stringResource(R.string.register_btn_submit),
                onClick = { viewModel.register() },
                variant = AppButtonVariant.Primary,
                enabled = !uiState.isLoading,
                trailingIcon = if (uiState.isLoading) {
                    {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                } else {
                    {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tautan "Sudah punya akun? Masuk"
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onNavigateToLogin(viewModel.currentRedirect()) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.register_have_account))
                        append(" ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)) {
                            append(stringResource(R.string.register_login_link))
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/** Label form standar dengan huruf kapital kecil, dipakai konsisten di seluruh layar register. */
@Composable
private fun FormLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
    )
}

