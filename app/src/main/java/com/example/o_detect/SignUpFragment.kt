package com.example.o_detect

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.google.android.material.textfield.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*


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

        upButton.setOnClickListener{


            if(upEmail.text!!.isEmpty()){
                upEmailLayout.error = "Error in email."  //另一個效果可用:upEmail.error
            }
            if(upUsername.text!!.isEmpty()){
                upUsernameLayout.error = "Error in username."
            }
            if(upPassword.text!!.isEmpty()){
                upPasswordLayout.error = "Error in password."
            }
            if(upEmail.text!!.isNotEmpty() &&  upUsername.text!!.isNotEmpty() && upPassword.text!!.isNotEmpty()){
                auth = FirebaseAuth.getInstance()
                auth.createUserWithEmailAndPassword(upEmail.text.toString(),upPassword.text.toString())
                    .addOnCompleteListener{task ->
                        if (task.isSuccessful) {

                            //成功註冊
                            Log.d(TAG, "createUserWithEmail:success")

                            //發送驗證信
                            val user = auth.currentUser
                            user?.sendEmailVerification()
                                ?.addOnCompleteListener{task ->
                                    if (task.isSuccessful) {
                                        Log.d(TAG, "Email sent.")
                                    }
                                    else{
                                        Log.d(TAG, "Email failure.")
                                    }
                                }
                            Toast.makeText(activity, "Authentication Success.",Toast.LENGTH_SHORT).show()

                            //將註冊資料寫入firebase
                            val database = FirebaseDatabase.getInstance()
                            val user_id : String = user?.uid.toString()
                            var path : String = "User/$user_id"
                            var userRef = database.getReference(path)
                            userRef.setValue(user_id)
                            path = "User/$user_id/email"
                            userRef = database.getReference(path)
                            userRef.setValue(user?.email.toString())
                            path = "User/$user_id/username"
                            userRef = database.getReference(path)
                            userRef.setValue(upUsername.text.toString())

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.exception)
                            Toast.makeText(activity, "Authentication failed.",Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }
}