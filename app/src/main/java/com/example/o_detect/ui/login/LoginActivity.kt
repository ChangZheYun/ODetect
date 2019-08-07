package com.example.o_detect.ui.login

import android.app.Activity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo

import com.example.o_detect.R
import android.animation.AnimatorSet
import android.view.animation.TranslateAnimation
import android.view.animation.Animation
import android.widget.*
import android.R.attr.name
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity.*
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout


class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var viewPager : ViewPager
    private lateinit var tabLayout : TabLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val img = findViewById<View>(R.id.headerLayout)

        viewPager = findViewById(R.id.signPager)
        tabLayout = findViewById(R.id.signTabs)

        //val titleGridView=findViewById<GridView>(R.id.titleGridView)


        //動畫路徑設定(x1,x2,y1,y2)
        val am = TranslateAnimation(0f, 0f, 500f, 0f)
        //動畫開始到結束的時間，2秒
        am.duration = 1800
        // 動畫重覆次數 (-1表示一直重覆，0表示不重覆執行，所以只會執行一次)
        am.repeatCount = 0
        // 結束後保留動畫最終樣態
        //am.fillAfter = true
        //將動畫寫入ImageView
        img.setAnimation(am)
        //開始動畫
        am.startNow()


        /*
        //Get an instance
        val adapter = TitleGridView()

        //Set titleGridView attribute
        titleGridView.adapter = adapter
        titleGridView.horizontalSpacing = 15
        titleGridView.stretchMode = GridView.STRETCH_COLUMN_WIDTH //分配剩餘空間給titleText


        titleGridView.setOnItemClickListener{ _ , view , position , _ ->
            Toast.makeText(this,"Click ${signText[position]}",Toast.LENGTH_SHORT).show()
        }*/

        viewPager.bringToFront()
        tabLayout.bringToFront()
        tabLayout.addTab(tabLayout.newTab().setText("Sign Up"))
        tabLayout.addTab(tabLayout.newTab().setText("Sign In"))
        setViewPagerAndTabLayout()
        viewPager.currentItem = 1  //預設sign in


        val username : EditText? = findViewById<EditText>(R.id.signInUsername)
        val password : EditText? = findViewById<EditText>(R.id.signInPassword)
        val login = findViewById<Button>(R.id.signInButton)
        val loading = findViewById<ProgressBar>(R.id.loading)

        loginViewModel = ViewModelProviders.of(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username?.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password?.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        })

        username?.afterTextChanged {
            loginViewModel.loginDataChanged(
                username?.text.toString(),
                password?.text.toString()
            )
        }

        password?.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username?.text.toString(),
                    password?.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            username?.text.toString(),
                            password?.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                loginViewModel.login(username?.text.toString(), password?.text.toString())
            }
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }


    // Fragment Adapter (這裡可能要改)
    class SignAdapter : PagerAdapter() {

        override fun getCount(): Int {
            return 2
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return `object`== view
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when(position){
                0 -> "Sign Up"
                else -> "Sign In"
            }
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view : Any
            when(position){
                0 -> view = LayoutInflater.from(container.context).inflate(R.layout.title_signup,container,false)
                else -> view = LayoutInflater.from(container.context).inflate(R.layout.title_signin,container,false)
            }
            container.addView(view)
            return view
        }

    }

    private fun setViewPagerAndTabLayout(){
        val fragmentAdapter = SignAdapter()

        viewPager.adapter = fragmentAdapter
        //viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        //tabLayout.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))
        tabLayout.setupWithViewPager(viewPager)
    }

}


/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}
