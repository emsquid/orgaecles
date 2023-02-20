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
import com.google.firebase.auth.UserProfileChangeRequest
import fr.em.orgaecls.AuthenticationActivity
import fr.em.orgaecls.R
import fr.em.orgaecls.databases.UsersRepository
import fr.em.orgaecls.models.UserModel

class RegisterFragment(private val context: AuthenticationActivity) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        val nameInput = view.findViewById<EditText>(R.id.register_page_name)
        val emailInput = view.findViewById<EditText>(R.id.register_page_email)
        val passwordInput = view.findViewById<EditText>(R.id.register_page_password)

        view.findViewById<Button>(R.id.register_page_button).setOnClickListener {
            val name = nameInput.text.toString()
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            tryToRegisterUser(name, email, password)
        }

        view.findViewById<TextView>(R.id.register_page_login).setOnClickListener {
            context.supportFragmentManager.popBackStack()
        }

        return view
    }

    private fun tryToRegisterUser(name: String, email: String, password: String) {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(
                context,
                "Un champ est incomplet",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            context.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = context.auth.currentUser
                        // set user name
                        user!!.updateProfile(
                            UserProfileChangeRequest.Builder()
                                .setDisplayName(name).build()
                        ).addOnCompleteListener { task1 ->
                            if (task1.isSuccessful) {
                                // register user
                                UsersRepository()
                                    .insertUser(UserModel(user.uid, email, name))
                                // splashscreen to get datas
                                context.goToSplashScreen()
                            } else {
                                Toast.makeText(
                                    context,
                                    task1.exception?.message.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
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
