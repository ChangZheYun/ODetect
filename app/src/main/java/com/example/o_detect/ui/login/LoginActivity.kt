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
import android.app.PendingIntent.getActivity
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity.*
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.o_detect.SignInFragment
import com.example.o_detect.SignUpFragment
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import kotlin.collections.ArrayList
import kotlinx.android.synthetic.main.title_signin.view.*


class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var viewPager : ViewPager
    private lateinit var tabLayout : TabLayout
    private val TAG = "LoginActivity"
    private lateinit var auth: FirebaseAuth

    // Fragment Pager
    class SignAdapter constructor(fm:FragmentManager): FragmentPagerAdapter(fm) {

        private var fragmentList : MutableList<Fragment> = ArrayList()
      //  private var currentViewPager : View? =null

        init{
            fragmentList.add(SignUpFragment())
            fragmentList.add(SignInFragment())
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when(position){
                0 -> "Sign Up"
                else -> "Sign In"
            }
        }

     /*   override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
            currentViewPager = `object` as View
        }

        fun getPrimaryItem() : View{
            return currentViewPager!!
        }
*/
        /*override fun isViewFromObject(view: View, `object`: Any): Boolean {
              return `object`== view
        }*/

       /* override fun instantiateItem(container: ViewGroup, position: Int): Any {
              val view : Any
              when(position){
                  0 -> view = LayoutInflater.from(container.context).inflate(R.layout.title_signup,container,false)
                  else -> view = LayoutInflater.from(container.context).inflate(R.layout.title_signin,container,false)
              }
              container.addView(view)
              return view
        }*/

    }

    private fun setViewPagerAndTabLayout(){
        val fragmentAdapter = SignAdapter(supportFragmentManager)

        viewPager.adapter = fragmentAdapter
        //viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        //tabLayout.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


       // val img = findViewById<View>(R.id.headerLayout)

        viewPager = findViewById(R.id.signPager)
        tabLayout = findViewById(R.id.signTabs)

        //val titleGridView=findViewById<GridView>(R.id.titleGridView)

/*
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
        am.startNow()*/


        viewPager.bringToFront()
        tabLayout.bringToFront()
        tabLayout.addTab(tabLayout.newTab().setText("Sign Up"))
        tabLayout.addTab(tabLayout.newTab().setText("Sign In"))
        setViewPagerAndTabLayout()
        viewPager.currentItem = 1  //預設sign in


      /*  val signInLayout: View = LayoutInflater.from(this).inflate(R.layout.title_signin,null)
        val inButton = signInLayout.findViewById<Button>(R.id.signInButton)
        inButton.setOnClickListener {
            Log.d(TAG,"ONCLICK")
        }*/

      /*  auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword("hudielan2019@gmail.com","cj62u,6x06")
            .addOnCompleteListener(this){ task ->
                if(task.isSuccessful){
                    Log.d(TAG,"signInWithEmail:Success")
                    //val user = auth.currentUser
                } else{
                    Log.w(TAG,"signInWithEmail:failure",task.exception)
                }
            }*/

        /* viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                if(position==1){
                    val signInLayout: View = LayoutInflater.from(applicationContext).inflate(R.layout.title_signin,null)
                    val inButton = findViewById<Button>(R.id.signInButton)
                    inButton.setOnClickListener {
                        Log.d(TAG,"ONCLICK")
                     //   Toast.makeText(applicationContext,"GAGAGAGA",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })*/

       /* val ttt: View = LayoutInflater.from(this).inflate(R.layout.title_signin,null)
        val inButton = ttt.findViewById<Button>(R.id.signInButton)
        inButton.setOnClickListener {
            Log.d(TAG,"ONCLICK")
        }*/

        //  val username = signInLayout.findViewById<EditText>(R.id.signInUsername)
        // val signUpLayout = inflater.inflate(R.layout.title_signup,null)


       /* tabLayout.addOnTabSelectedListener( object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tabLayout: TabLayout.Tab){
                when(tabLayout.position){
                    0 -> login.text = "註冊"
                    else -> login.text = "登入"
                }
            }

            override fun onTabReselected(p0: TabLayout.Tab?) {

            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {

            }
        })*/

        /*val username = signInLayout.findViewById<EditText>(R.id.signInUsername)
        val password = signInLayout.findViewById<EditText>(R.id.signInPassword)
        val loading = findViewById<ProgressBar>(R.id.loading)*/



       /*  signInButton.setOnClickListener {
            Toast.makeText(this,"BUTTON TEST",Toast.LENGTH_SHORT).show()
           // loginViewModel.login(username.text.toString(), password.text.toString())
        }*/

      /*  loginViewModel = ViewModelProviders.of(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            signInButton.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
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

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            signInButton.setOnClickListener {
                loading.visibility = View.VISIBLE
                loginViewModel.login(username.text.toString(), password.text.toString())
            }
        }*/
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
