package me.xleiten.coolgpsapp

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

// Используется для показа всмплывающих сообщений (снизу которые)
class PopupManager(private val context: Context) {

    fun toast(message: String, length: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this.context, message, length).show()
    }

    fun toast(messageId: Int, length: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this.context, messageId, length).show()
    }

    fun dialog(
        message: CharSequence,
        titleId: Int = R.string.dialog_default_title,
        config: AlertDialog.Builder.() -> Unit = { }
    ): AlertDialog {
        val dialogBuilder = AlertDialog.Builder(this.context)
        dialogBuilder.setTitle(titleId)
        dialogBuilder.setMessage(message)
        dialogBuilder.setPositiveButton(
            R.string.dialog_ok_button_text,
            null
        )
        dialogBuilder.setCancelable(true)
        config(dialogBuilder)
        val dialog = dialogBuilder.create()
        dialog.show()
        return dialog
    }

    fun dialog(
        messageId: Int,
        titleId: Int = R.string.dialog_default_title,
        config: AlertDialog.Builder.() -> Unit = { }
    ): AlertDialog {
        return this.dialog(this.context.getText(messageId), titleId, config)
    }
}