package com.example.o_detect

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.content.pm.PackageManager
import com.example.o_detect.ui.login.LoginActivity
import android.os.Handler
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermission()

        auth = FirebaseAuth.getInstance()
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            Handler().postDelayed({
                val user = firebaseAuth.currentUser
                if (user == null) //無登入
                {
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                }
                else  //已登入
                {
                    startActivity(Intent(this@MainActivity, Content::class.java))
                }
                finish()
                //進入/退出動畫
                // overridePendingTransition(R.anim.abc_fade_in,R.anim.abc_fade_out)
            }, 600)
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



    private fun requestPermission(){

        val permissionList = arrayListOf(Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE)

        var size = permissionList.size
        var i = 0
        while (i < size) {
            if (ActivityCompat.checkSelfPermission(this, permissionList[i]) == PackageManager.PERMISSION_GRANTED) {
                permissionList.removeAt(i)
                i -= 1
                size -= 1
            }
            i += 1
        }
        val array = arrayOfNulls<String>(permissionList.size)
        if (permissionList.isNotEmpty()) ActivityCompat.requestPermissions(this, permissionList.toArray(array), 0)

    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        auth.removeAuthStateListener(authStateListener)
        super.onStop()
    }

   /* override fun onDestroy() {
        auth.signOut()
        super.onDestroy()
    }*/
}
