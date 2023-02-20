package fr.em.orgaecls.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import fr.em.orgaecls.AuthenticationActivity
import fr.em.orgaecls.R

class ResetPasswordFragment(private val context: AuthenticationActivity) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reset_password, container, false)

        view?.findViewById<ImageView>(R.id.reset_page_back_button)?.setOnClickListener {
            context.supportFragmentManager.popBackStack()
        }

        val emailInput = view?.findViewById<EditText>(R.id.reset_page_email)

        view?.findViewById<Button>(R.id.reset_page_button)?.setOnClickListener {
            val email = emailInput?.text.toString()

            tryToResetPassword(email)
        }

        return view
    }

    private fun tryToResetPassword(email: String) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(
                context,
                "Un champ est incomplet",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            context.auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // login
                        Toast.makeText(context, "Un email vous a été envoyé", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(
                            context,
                            task.exception?.message.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

}
