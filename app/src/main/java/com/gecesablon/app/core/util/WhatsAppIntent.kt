package com.gecesablon.app.core.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.gecesablon.app.R

/**
 * WhatsAppIntent menyediakan helper untuk membuka obrolan WhatsApp workshop via Intent
 * dengan URL https://wa.me/<NOMOR_WA_WORKSHOP> dan menangani kondisi WhatsApp tidak terpasang.
 */
object WhatsAppIntent {

    fun openChat(context: Context, phoneNumber: String, message: String = "") {
        val cleanNumber = phoneNumber.replace(Regex("[^0-9]"), "")
        val baseUrl = "https://wa.me/$cleanNumber"
        val fullUrl = if (message.isNotBlank()) {
            "$baseUrl?text=${Uri.encode(message)}"
        } else {
            baseUrl
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                context.getString(R.string.about_whatsapp_not_found),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
