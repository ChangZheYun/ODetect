package com.example.o_detect

import android.R.attr.data
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileOutputStream


class SignUpFragment:Fragment() {

    private lateinit var upButton : Button
    private lateinit var upEmail : TextInputEditText
    private lateinit var upUsername : TextInputEditText
    private lateinit var upPassword : TextInputEditText
    private lateinit var upEmailLayout : TextInputLayout
    private lateinit var upUsernameLayout : TextInputLayout
    private lateinit var upPasswordLayout : TextInputLayout

    private lateinit var auth: FirebaseAuth
    private val TAG = "sign up"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val view : View = inflater.inflate(R.layout.title_signup,null)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        upButton = activity!!.findViewById(R.id.signUpButton)
        upEmail = activity!!.findViewById(R.id.signUpEmailText)
        upUsername = activity!!.findViewById(R.id.signUpUsernameText)
        upPassword = activity!!.findViewById(R.id.signUpPasswordText)
        upEmailLayout = activity!!.findViewById(R.id.signUpEmailTextLayout)
        upUsernameLayout = activity!!.findViewById(R.id.signUpUsernameTextLayout)
        upPasswordLayout = activity!!.findViewById(R.id.signUpPasswordTextLayout)


        //點擊EditText時取消錯誤訊息(以後可加入即時判斷輸入是否符合格式)
        upEmail.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //先設置false取消掉錯誤訊息，再設置true避免物件位置偏移
                upEmailLayout.isErrorEnabled = false
                upEmailLayout.isErrorEnabled = true
            }
        })
        upUsername.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                upUsernameLayout.isErrorEnabled = false
                upUsernameLayout.isErrorEnabled = true
            }
        })
        upPassword.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                upPasswordLayout.isErrorEnabled = false
                upPasswordLayout.isErrorEnabled = true
            }
        })

        //inPassword點擊enter時自動點選登入按鈕
        upPassword.setOnKeyListener( View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                upButton.callOnClick()
                return@OnKeyListener true
            }
            false
        })

        upButton.setOnClickListener{

            //隱藏鍵盤
            val imm  = upPassword.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view!!.windowToken,0)

            //清除焦點
            upEmail.clearFocus()
            upUsername.clearFocus()
            upPassword.clearFocus()

            if(upEmail.text!!.isEmpty()){
                upEmailLayout.error = "Email can't be empty."  //另一個效果可用:upEmail.error
            }
            if(upUsername.text!!.isEmpty()){
                upUsernameLayout.error = "Username can't be empty."
            }
            if(upPassword.text!!.isEmpty()){
                upPasswordLayout.error = "Password can't be empty."
            }
            if(upEmail.text!!.isNotEmpty() &&  upUsername.text!!.isNotEmpty() && upPassword.text!!.isNotEmpty()){
                auth = FirebaseAuth.getInstance()
                auth.createUserWithEmailAndPassword(upEmail.text.toString(),upPassword.text.toString())
                    .addOnCompleteListener{task ->
                        if (task.isSuccessful) {

                            //成功註冊
                            Log.d(TAG, "createUserWithEmail:success")

                            //將信箱密碼寫入記憶體
                            val dir = context!!.filesDir
                            val outputFile = File(dir,"account.txt")
                            val accountData = "${upEmail.text}###${upPassword.text}"
                            writeToFile(outputFile,accountData)

                            //發送驗證信
                            val user = auth.currentUser
                            user?.sendEmailVerification()
                                ?.addOnCompleteListener{task ->
                                    if (task.isSuccessful) {

                                        Log.d(TAG, "Email sent.")

                                        val info = Snackbar.make(view!!, "註冊成功，請收取驗證信", Snackbar.LENGTH_SHORT)
                                        info.duration = 5000
                                        info.show()

                                        //將註冊資料寫入firebase
                                        val databaseRef = FirebaseDatabase.getInstance().reference
                                        val userId : String = user.uid.toString()
                                        var path = "User/$userId"
                                        var userRef = databaseRef.child("$path/email")
                                        userRef.setValue(user.email.toString())
                                        userRef = databaseRef.child("$path/username")
                                        userRef.setValue(upUsername.text.toString())

                                        //寫入local
                                        activity!!.getSharedPreferences("userData", Context.MODE_PRIVATE)
                                            .edit().putString("username",upUsername.text.toString()).apply()

                                        //設定預設資料
                                        path = "MetaData/$userId"
                                        databaseRef.child("$path/healthSum").setValue(0)
                                        databaseRef.child("$path/unhealthSum").setValue(0)
                                        databaseRef.child("$path/plantSum").setValue(0)
                                        databaseRef.child("$path/houseNum").setValue(1)
                                        databaseRef.child("$path/G1/health").setValue(0)
                                        databaseRef.child("$path/G1/unhealth").setValue(0)
                                        databaseRef.child("$path/G1/housePlantSum").setValue(0)
                                    }
                                    else{

                                        Log.d(TAG, "Email failure.")

                                        val info = Snackbar.make(view!!, "註冊失敗，${task.exception}", Snackbar.LENGTH_SHORT)
                                        info.duration = 5000
                                        info.show()

                                    }
                                }



                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.exception)

                            val info = Snackbar.make(view!!, "註冊失敗，${task.exception}", Snackbar.LENGTH_SHORT)
                            info.duration = 5000
                            info.setAction("清空", View.OnClickListener{
                                upEmail.setText("")
                                upUsername.setText("")
                                upPassword.setText("")
                            }).show()
                        }
                    }
            }
        }
    }

    private fun writeToFile(outputFile : File , data : String){
        var osw: FileOutputStream? = null
        try {
            osw = FileOutputStream(outputFile)
            osw.write(data.toByteArray())
            Log.d("TTest",data)
            osw.flush()
        } catch (e: Exception) {
        } finally {
            try {
                osw?.close()
            } catch (e: Exception) {
            }
        }
    }
}