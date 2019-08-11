package com.example.o_detect

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import com.example.o_detect.ui.login.LoginActivity
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            Handler().postDelayed({
              /*  val user = firebaseAuth.currentUser
                if (user == null) //無登入
                {*/
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
           /*     }
                else  //已登入
                {
                    startActivity(Intent(this@MainActivity, Content::class.java))
                }*/
                finish()
                //進入/退出動畫
                // overridePendingTransition(R.anim.abc_fade_in,R.anim.abc_fade_out)
            }, 800)
        }

      /*  Handler().postDelayed({
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
            //進入/退出動畫
           // overridePendingTransition(R.anim.abc_fade_in,R.anim.abc_fade_out)
        }, 800)*/
    }

   /* protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            REQUEST_LOGIN ->{
                if (resultCode != RESULT_OK){
                    finish()
                }
            }
        }
    }*/

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        auth.removeAuthStateListener(authStateListener)
        super.onStop()
    }
}
