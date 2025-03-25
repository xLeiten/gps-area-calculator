

import android.content.Context
import androidx.appcompat.app.AlertDialog
import test.app.areacalculator.R

class DialogBuilder(private val context: Context) {

    fun dialog(
        message: CharSequence,
        titleId: Int = R.string.dialog_title,
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
        titleId: Int = R.string.dialog_title,
        config: AlertDialog.Builder.() -> Unit = { }
    ): AlertDialog {
        return this.dialog(this.context.getText(messageId), titleId, config)
    }
}