package fr.em.orgaecls.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import fr.em.orgaecls.AuthenticationActivity
import fr.em.orgaecls.R

class LoginFragment(private val context: AuthenticationActivity) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val emailInput = view.findViewById<EditText>(R.id.login_page_email)
        val passwordInput = view.findViewById<EditText>(R.id.login_page_password)

        view.findViewById<TextView>(R.id.login_page_forgot_password).setOnClickListener {
            val transaction = context.supportFragmentManager.beginTransaction()
            transaction.replace(R.id.auth_layout, ResetPasswordFragment(context))
            transaction.addToBackStack(null)
            transaction.commit()
        }

        view.findViewById<Button>(R.id.login_page_button).setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            tryToLoginUser(email, password)
        }

        view.findViewById<TextView>(R.id.login_page_register).setOnClickListener {
            val transaction = context.supportFragmentManager.beginTransaction()
            transaction.replace(R.id.auth_layout, RegisterFragment(context))
            transaction.addToBackStack(null)
            transaction.commit()
        }

        return view
    }

    private fun tryToLoginUser(email: String, password: String) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(
                context,
                "Un champ est incomplet",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            context.auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // splashscreen to get datas
                        context.goToSplashScreen()
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
