package fr.em.orgaecls

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class SplashScreenActivity : AppCompatActivity() {
    private val version = 0.1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashscreen)
    }

    override fun onStart() {
        super.onStart()
        val auth = Firebase.auth
        val currentUser = auth.currentUser

        if (currentUser != null) {
            checkAppVersion { goToMainActivity() }
        } else {
            goToAuthActivity()
        }
    }

    private fun goToMainActivity() {
        val runnable = Runnable {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        Handler(Looper.getMainLooper()).postDelayed(runnable, 1000)
    }

    private fun goToAuthActivity() {
        val runnable = Runnable {
            val intent = Intent(this, AuthenticationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        Handler(Looper.getMainLooper()).postDelayed(runnable, 1000)
    }

    private fun checkAppVersion(callback: () -> Unit) {
        val ref =
            FirebaseDatabase.getInstance("https://orgaecles-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("version")

        ref.get().addOnSuccessListener {
            val updatedVersion = it.getValue(Double::class.java)
            if (version == updatedVersion) {
                callback()
            } else {
                Toast.makeText(this, "L'application n'est pas à jour", Toast.LENGTH_SHORT).show()
            }
        }
    }
}