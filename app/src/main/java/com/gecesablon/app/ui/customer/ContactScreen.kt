package com.gecesablon.app.ui.customer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.gecesablon.app.core.util.WhatsAppIntent
import com.gecesablon.app.ui.components.AppTopBar
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.ShapeBadge
import com.gecesablon.app.ui.theme.ShapeCard
import com.gecesablon.app.ui.theme.ShapeControl

/**
 * Layar Kontak Workshop Pelanggan:
 * Menampilkan nama workshop, alamat dan jam buka sementara dari strings.xml,
 * serta tombol aksi membuka WhatsApp via WhatsAppIntent.
 */
@Composable
fun ContactScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GeceColors.Background,
        topBar = {
            AppTopBar(title = stringResource(R.string.title_contact))
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Kartu Profil Workshop
            Card(
                shape = ShapeCard,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(GeceColors.Accent)
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.logo_gece),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Box(
                                modifier = Modifier
                                    .clip(ShapeBadge)
                                    .background(GeceColors.PrimaryInk)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.contact_studio_pill),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = GeceColors.Accent
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.contact_workshop_name),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(20.dp))

                    // 1. Alamat Workshop
                    ContactRow(
                        icon = Icons.Default.LocationOn,
                        label = stringResource(R.string.contact_workshop_address_label),
                        value = stringResource(R.string.contact_workshop_address)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Jam Operasional
                    ContactRow(
                        icon = Icons.Default.AccessTime,
                        label = stringResource(R.string.contact_workshop_hours_label),
                        value = stringResource(R.string.contact_workshop_hours)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Nomor WhatsApp
                    ContactRow(
                        icon = Icons.Default.Phone,
                        label = stringResource(R.string.about_workshop_wa_label),
                        value = BuildConfig.NOMOR_WA_WORKSHOP
                    )
                }
            }

            // Tombol WhatsApp Utama
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
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = GeceColors.Accent
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.contact_whatsapp_btn),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun ContactRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(ShapeControl)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
