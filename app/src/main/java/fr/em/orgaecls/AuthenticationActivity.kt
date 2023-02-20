package fr.em.orgaecls

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import fr.em.orgaecls.fragments.LoginFragment

class AuthenticationActivity : AppCompatActivity() {

    val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
    }

    override fun onStart() {
        super.onStart()
        // go to loginFragment
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.auth_layout, LoginFragment(this))
        transaction.commit()
    }

    fun goToSplashScreen() {
        // go to main
        val intent = Intent(this, SplashScreenActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
