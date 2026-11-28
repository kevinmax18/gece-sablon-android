package com.gecesablon.app.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gecesablon.app.R
import com.gecesablon.app.data.model.LanguageMode
import com.gecesablon.app.data.model.ThemeMode
import com.gecesablon.app.ui.components.AppTopBar
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.ShapeCard

/**
 * Layar Pengaturan bersama untuk semua peran (Pelanggan, Admin, Owner).
 * Mengelola preferensi bahasa aplikasi dan tema tampilan.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GeceColors.Background,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.settings_title),
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
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Bagian Bahasa
            SettingsSection(
                icon = Icons.Default.Language,
                title = stringResource(R.string.settings_language_section),
                description = stringResource(R.string.settings_language_desc)
            ) {
                SettingsRadioRow(
                    label = stringResource(R.string.settings_option_system),
                    selected = uiState.languageMode == LanguageMode.SISTEM,
                    onClick = { viewModel.setLanguageMode(LanguageMode.SISTEM) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = GeceColors.Border
                )
                SettingsRadioRow(
                    label = stringResource(R.string.settings_language_id),
                    selected = uiState.languageMode == LanguageMode.INDONESIA,
                    onClick = { viewModel.setLanguageMode(LanguageMode.INDONESIA) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = GeceColors.Border
                )
                SettingsRadioRow(
                    label = stringResource(R.string.settings_language_en),
                    selected = uiState.languageMode == LanguageMode.ENGLISH,
                    onClick = { viewModel.setLanguageMode(LanguageMode.ENGLISH) }
                )
            }

            // Bagian Tema Tampilan
            SettingsSection(
                icon = Icons.Default.Palette,
                title = stringResource(R.string.settings_theme_section),
                description = stringResource(R.string.settings_theme_desc)
            ) {
                SettingsRadioRow(
                    label = stringResource(R.string.settings_option_system),
                    selected = uiState.themeMode == ThemeMode.SISTEM,
                    onClick = { viewModel.setThemeMode(ThemeMode.SISTEM) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = GeceColors.Border
                )
                SettingsRadioRow(
                    label = stringResource(R.string.settings_theme_light),
                    selected = uiState.themeMode == ThemeMode.TERANG,
                    onClick = { viewModel.setThemeMode(ThemeMode.TERANG) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = GeceColors.Border
                )
                SettingsRadioRow(
                    label = stringResource(R.string.settings_theme_dark),
                    selected = uiState.themeMode == ThemeMode.GELAP,
                    onClick = { viewModel.setThemeMode(ThemeMode.GELAP) }
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    icon: ImageVector,
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GeceColors.TextMain,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = GeceColors.TextMain,
                modifier = Modifier.weight(1f),
                softWrap = true
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = GeceColors.TextMuted,
            modifier = Modifier.padding(horizontal = 4.dp),
            softWrap = true
        )
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = ShapeCard,
            colors = CardDefaults.cardColors(containerColor = GeceColors.Surface),
            border = BorderStroke(1.dp, GeceColors.Border)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsRadioRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = GeceColors.TextMain,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            softWrap = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = GeceColors.PrimaryInk,
                unselectedColor = GeceColors.TextMuted
            )
        )
    }
}
