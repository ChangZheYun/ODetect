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
import kotlinx.android.synthetic.main.activity_login.*


class SignUpFragment:Fragment() {

    private lateinit var upButton : Button
    private lateinit var upEmail : TextInputEditText
    private lateinit var upPassword : TextInputEditText
    private lateinit var upEmailLayout : TextInputLayout
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
        upPassword = activity!!.findViewById(R.id.signUpPasswordText)
        upEmailLayout = activity!!.findViewById(R.id.signUpEmailTextLayout)
        upPasswordLayout = activity!!.findViewById(R.id.signUpPasswordTextLayout)

        upButton.setOnClickListener{

            if(upEmail.text!!.isEmpty() && upPassword.text!!.isEmpty()){
                upEmailLayout.isErrorEnabled = true
                upEmailLayout.error = "Error in email."
                upPasswordLayout.error = "Error in password."
            }
            else if(upEmail.text!!.isEmpty()){
                upEmailLayout.isErrorEnabled = true
                upEmailLayout.error = "Error in email."
            }
            else if(upPassword.text!!.isEmpty()){
                upPasswordLayout.error = "Error in password."
            }
            else{
                auth = FirebaseAuth.getInstance()
                auth.createUserWithEmailAndPassword(upEmail.text.toString(),upPassword.text.toString())
                    .addOnCompleteListener{task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success")
                            val user = auth.currentUser
                            Toast.makeText(activity, "Authentication Success.",Toast.LENGTH_SHORT).show()
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