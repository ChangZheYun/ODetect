package com.example.o_detect

import android.app.Activity
import android.app.Activity.RESULT_OK
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.example.o_detect.ui.login.LoginActivity
import android.content.Intent
import com.google.firebase.auth.FirebaseUser
import androidx.annotation.NonNull
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.textfield.*
import com.google.firebase.auth.AuthResult
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


        inButton.setOnClickListener{
          /*  Log.d(TAG1,username.text.toString())
            Log.d(TAG2,password.text.toString())
            Toast.makeText(activity,"HHH",Toast.LENGTH_SHORT).show()*/

            if(inEmail.text!!.isEmpty() && inPassword.text!!.isEmpty()){
                inEmailLayout.isErrorEnabled = true
                inEmailLayout.error = "Error in email."
                inPasswordLayout.error = "Error in password."
            }
            else if(inEmail.text!!.isEmpty()){
                inEmailLayout.isErrorEnabled = true
                inEmailLayout.error = "Error in email."
            }
            else if(inPassword.text!!.isEmpty()){
                inPasswordLayout.error = "Error in password."
            }
            else{
                auth = FirebaseAuth.getInstance()
                auth.signInWithEmailAndPassword(inEmail.text.toString(), inPassword.text.toString())
                    .addOnCompleteListener { task ->
                        val user = auth.currentUser
                        if (task.isSuccessful && user!!.isEmailVerified) {    //user.isEmailVerified要先通過信箱驗證
                            Log.d(TAG, "signInWithEmail:Success")
                            startActivity(Intent(activity,Content::class.java))
                        }else if(!task.isSuccessful){
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(activity, "帳號密碼錯誤",Toast.LENGTH_SHORT).show()
                        }else{
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(activity, "請先通過信箱驗證",Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

    }
}