package fr.em.orgaecls.builders

import android.app.AlertDialog
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import fr.em.orgaecls.MainActivity
import fr.em.orgaecls.R


fun buildConfirmDialog(
    context: AppCompatActivity,
    positiveCallback: () -> Unit,
) {
    val builder = AlertDialog.Builder(context)
    builder.setMessage("Êtes-vous sûr ?")
        .setCancelable(false)
        .setPositiveButton("Oui") { _, _ ->
            positiveCallback()
        }
        .setNegativeButton("Non") { dialog, _ ->
            dialog.dismiss()
        }

    val dialog = builder.create()
    val typeface = ResourcesCompat.getFont(context, R.font.kanit)

    dialog.show()
    dialog.findViewById<TextView>(android.R.id.message).typeface = typeface
    dialog.findViewById<Button>(android.R.id.button1).typeface = typeface
    dialog.findViewById<Button>(android.R.id.button2).typeface = typeface
}