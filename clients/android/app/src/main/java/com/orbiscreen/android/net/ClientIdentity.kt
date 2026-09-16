// Orbiscreen - ClientIdentity.kt (GPL-3.0-or-later)
// https://github.com/shadow-x78/orbiscreen

package com.orbiscreen.android.net

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.WindowManager

data class ClientIdentity(
    val name: String,
    val key: String,
    val width: Int,
    val height: Int,
) {
    companion object {
        fun from(context: Context): ClientIdentity {
            val resolverName = Settings.Global.getString(resolver(context), Settings.Global.DEVICE_NAME)
            val name = resolverName
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: Build.MODEL.ifBlank { "Android" }
            val (w, h) = nativePixels(context)
            return ClientIdentity(name = name, key = deviceKey(context), width = w, height = h)
        }

        private fun deviceKey(context: Context): String {
            val raw = Settings.Secure.getString(resolver(context), Settings.Secure.ANDROID_ID)
                ?.filter { it.isLetterOrDigit() }
                ?.lowercase()
                .orEmpty()
            return raw.takeLast(8).ifBlank { "android" }
        }

        private fun resolver(context: Context) = context.applicationContext.contentResolver

        private fun nativePixels(context: Context): Pair<Int, Int> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val wm = context.getSystemService(WindowManager::class.java)
                val bounds = wm.currentWindowMetrics.bounds
                val w = bounds.width()
                val h = bounds.height()
                if (w > 0 && h > 0) return w to h
            }
            val dm = context.resources.displayMetrics
            return dm.widthPixels to dm.heightPixels
        }
    }
}
