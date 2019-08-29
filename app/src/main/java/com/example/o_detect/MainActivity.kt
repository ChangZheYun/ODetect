package com.example.o_detect

import android.Manifest
import android.app.Activity
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
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.content_display.*

class MainActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermission()

        auth = FirebaseAuth.getInstance()
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            executeProgram()
        }


      /*  Handler().postDelayed({
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
            //進入/退出動畫
           // overridePendingTransition(R.anim.abc_fade_in,R.anim.abc_fade_out)
        }, 800)*/
    }

    private fun executeProgram(){
        Handler().postDelayed({
            val user = auth.currentUser
            if (user == null) //無登入
            {
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            } else  //已登入
            {
                startActivity(Intent(this@MainActivity, Content::class.java))
            }
            finish()
            //進入/退出動畫
            // overridePendingTransition(R.anim.abc_fade_in,R.anim.abc_fade_out)
        }, 500)
    }

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
        if (permissionList.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(array), 0)
        }else{
            executeProgram()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            0 ->{
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED){
                    //擁有權限才能登入
                    auth.addAuthStateListener(authStateListener)
                }else{
                    Snackbar.make(window.decorView,"請給予程式權限",Snackbar.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }


   /* override fun onStop() {
        auth.removeAuthStateListener(authStateListener)
        super.onStop()
    }*/

   /* override fun onDestroy() {
        auth.signOut()
        super.onDestroy()
    }*/
}
