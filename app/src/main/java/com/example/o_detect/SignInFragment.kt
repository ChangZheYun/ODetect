package com.example.o_detect

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.example.o_detect.ui.login.LoginActivity
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.text.util.Linkify
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.inputmethod.InputMethodManager
import com.google.firebase.auth.FirebaseUser
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.circularreveal.CircularRevealWidget
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.*
import com.google.firebase.auth.AuthResult
import kotlinx.android.synthetic.main.content_display.*
import kotlinx.android.synthetic.main.title_signin.*
import java.lang.Exception



class SignInFragment : Fragment() {

    private lateinit var inButton : Button
    private lateinit var inEmail : TextInputEditText
    private lateinit var inPassword : TextInputEditText
    private lateinit var inEmailLayout : TextInputLayout
    private lateinit var inPasswordLayout : TextInputLayout
    private val TAG = "sign in"
    private val TAG1 = "username"
    private val TAG2 = "password"

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view : View = inflater.inflate(R.layout.title_signin,container,false)

        /*auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(username.text.toString(),password.text.toString())
            .addOnCompleteListener(this){ task ->
                if(task.isSuccessful){
                    Log.d(TAG,"signInWithEmail:Success")
                    val user = auth.currentUser
                } else{
                   Log.w(TAG,"signInWithEmail:failure",task.exception)
                }
            }*/
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        inButton = activity!!.findViewById(R.id.signInButton)
        inEmail = activity!!.findViewById(R.id.signInEmailText)
        inPassword = activity!!.findViewById(R.id.signInPasswordText)
        inEmailLayout = activity!!.findViewById(R.id.signInEmailTextLayout)
        inPasswordLayout = activity!!.findViewById(R.id.signInPasswordTextLayout)


        //點擊EditText時取消錯誤訊息(以後可加入即時判斷輸入是否符合格式)
        inEmail.addTextChangedListener(object : TextWatcher{
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //先設置false取消掉錯誤訊息，再設置true避免物件位置偏移
                inEmailLayout.isErrorEnabled = false
                inEmailLayout.isErrorEnabled = true
            }
        })
        inPassword.addTextChangedListener(object : TextWatcher{
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                inPasswordLayout.isErrorEnabled = false
                inPasswordLayout.isErrorEnabled = true
            }
        })

        //inPassword點擊enter時自動點選登入按鈕
        inPassword.setOnKeyListener( View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                inButton.callOnClick()
                return@OnKeyListener true
            }
            false
        })

        inButton.setOnClickListener{

            //隱藏鍵盤
            val imm  = inPassword.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view!!.windowToken,0)

            //清除焦點
            inEmail.clearFocus()
            inPassword.clearFocus()

            if(inEmail.text!!.isEmpty() && inPassword.text!!.isEmpty()){
                inEmailLayout.isErrorEnabled = true
                inEmailLayout.error = "Email can't be empty."
                inPasswordLayout.error = "Password can't be empty."
            }
            else if(inEmail.text!!.isEmpty()){
                inEmailLayout.isErrorEnabled = true
                inEmailLayout.error = "Email can't be empty."
            }
            else if(inPassword.text!!.isEmpty()){
                inPasswordLayout.error = "Password can't be empty."
            }
            else{
                auth = FirebaseAuth.getInstance()
                auth.signInWithEmailAndPassword(inEmail.text.toString(), inPassword.text.toString())
                    .addOnCompleteListener { task ->
                        val user = auth.currentUser
                        if (task.isSuccessful && user!!.isEmailVerified) {    //user.isEmailVerified要先通過信箱驗證
                            Log.d(TAG, "signInWithEmail:Success")
                            activity!!.getSharedPreferences("userData", Context.MODE_PRIVATE)
                                .edit().putString("email",inEmail.text.toString()).apply()
                            getXYAnimation(it)
                        }else if(!task.isSuccessful){
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            val warning = Snackbar.make(view!!, "帳號密碼錯誤", Snackbar.LENGTH_SHORT)
                            warning.duration = 5000
                            warning.setAction("清空", View.OnClickListener{
                                inEmail.setText("")
                                inPassword.setText("")
                            }).show()
                        }else{
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            val warning = Snackbar.make(view!!, "請先通過信箱驗證", Snackbar.LENGTH_SHORT)
                            warning.duration = 5000
                            warning.show()
                        }
                    }
            }
        }

    }

    private fun getXYAnimation(it:View){
        val location = IntArray(2)

        //取得fab的x, y座標
        it.getLocationOnScreen(location)
        val revealX = location[0]
        val revealY = location[1]

        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity!!, it, "Transition")

        //傳入fab的x, y座標
        val intent = Intent(activity!!, Content::class.java)
        intent.putExtra(Content.ARG_CIRCULAR_REVEAL_X, revealX)
        intent.putExtra(Content.ARG_CIRCULAR_REVEAL_Y, revealY)

        ActivityCompat.startActivity(activity!!, intent, options.toBundle())
        //startActivity(Intent(activity,Content::class.java))
    }
}