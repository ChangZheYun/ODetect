package com.example.o_detect

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat.getMainExecutor
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.title_signin.*
import java.io.*
import java.lang.Exception


class SignInFragment : Fragment() {

    private lateinit var inButton : Button
    private lateinit var inEmail : TextInputEditText
    private lateinit var inPassword : TextInputEditText
    private lateinit var inEmailLayout : TextInputLayout
    private lateinit var inPasswordLayout : TextInputLayout

    private val TAG = "sign in"
    private var privatePassword = ""

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

        //不同API調整指紋辨識模組
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ) {

            //點選"指紋登入"
            fingerSignInButton.setOnClickListener{
                cleanPicture()
                //先判斷有沒有信箱密碼才能使用指紋
                if(checkMemoryIsSecretData()) {
                    biometricVer28()
                }else{
                    Snackbar.make(view!!, "第一次登入請先使用密碼", Snackbar.LENGTH_SHORT).show()
                }
            }

            //畫面直接進入指紋辨識區
            if(checkMemoryIsSecretData()) {
                biometricVer28()
            }
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ){

            //點選"指紋登入"
            fingerSignInButton.setOnClickListener{
                cleanPicture()
                //先判斷有沒有信箱密碼才能使用指紋
                if(checkMemoryIsSecretData()) {
                    biometricVer23()
                }else{
                    Snackbar.make(view!!, "第一次登入請先使用密碼", Snackbar.LENGTH_SHORT).show()
                }
            }

            //畫面直接進入指紋辨識區
            if(checkMemoryIsSecretData()) {
                biometricVer23()
            }

        }

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

            cleanPicture()

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
                authenticate(1)
            }
        }

    }

    private fun cleanPicture(){
        //隱藏鍵盤
        val imm  = inPassword.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken,0)

        //清除焦點
        inEmail.clearFocus()
        inPassword.clearFocus()
    }


    private fun checkMemoryIsSecretData():Boolean{
        val dir = context!!.filesDir
        return try {
            val inputFile = File(dir, "account.txt")
            readFromFile(inputFile)
        }catch (e:Exception){
            false
        }
    }

    private fun readFromFile(inputFile: File):Boolean{
        val inputStream = FileInputStream(inputFile)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val accountData = bufferedReader.readLine()
        inEmail.setText(accountData.split("###")[0])
        privatePassword = accountData.split("###")[1]
        inEmail.text!!.isNotEmpty() &&  privatePassword!=""
            return true
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

    //認證帳號、密碼、是否有按驗證信(整合指紋)`
    //type1:密碼登入，2:指紋登入
    private fun authenticate(type : Int){
        auth = FirebaseAuth.getInstance()
        var password = ""
        when(type){
            1 -> password = inPassword.text.toString()
            2 -> password = privatePassword
        }
        auth.signInWithEmailAndPassword(inEmail.text.toString(), password)
            .addOnCompleteListener { task ->
                val user = auth.currentUser
                if (task.isSuccessful && user!!.isEmailVerified) {    //user.isEmailVerified要先通過信箱驗證
                    Log.d(TAG, "signInWithEmail:Success")
                    activity!!.getSharedPreferences("userData", Context.MODE_PRIVATE)
                        .edit().putString("email",inEmail.text.toString()).apply()

                    //將信箱密碼寫入記憶體
                    val dir = context!!.filesDir
                    val outputFile = File(dir,"account.txt")
                    val accountData = "${inEmail.text}###${password}"
                    writeToFile(outputFile,accountData)

                    getXYAnimation(view!!)
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

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    @RequiresApi(api = Build.VERSION_CODES.P)
    private fun biometricVer28(){

        Handler().postDelayed({

            biometricPrompt = BiometricPrompt(activity!!,
                getMainExecutor(context),
                object : BiometricPrompt.AuthenticationCallback() {

                    override fun onAuthenticationError(errorCode: Int,
                                                       errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        //Snackbar.make(view!!, "認證錯誤：$errString", Snackbar.LENGTH_SHORT).show()
                        //Toast.makeText(activity, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                    }

                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        //Toast.makeText(activity,"成功登入", Toast.LENGTH_SHORT).show()
                        authenticate(2)
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Snackbar.make(view!!, "認證失敗", Snackbar.LENGTH_SHORT).show()
                        //Toast.makeText(activity, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                })

            promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("指紋驗證")
                .setSubtitle("")
                .setDescription("")
                .setNegativeButtonText("使用密碼")
                .build()

            biometricPrompt.authenticate(promptInfo)

        }, 10)

    }

    private fun biometricVer23(){

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