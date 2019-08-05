package com.example.o_detect

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import com.example.o_detect.ui.login.LoginActivity
import android.os.Handler

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Handler().postDelayed({
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
            //進入/退出動畫
           // overridePendingTransition(R.anim.abc_fade_in,R.anim.abc_fade_out)
        }, 800)
    }


}
