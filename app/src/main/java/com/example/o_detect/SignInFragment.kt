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
import com.google.firebase.auth.AuthResult
import java.lang.Exception


class SignInFragment : Fragment() {

    private lateinit var inButton : Button
    private lateinit var username : EditText
    private lateinit var password : EditText
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
        username = activity!!.findViewById(R.id.signInUsername)
        password = activity!!.findViewById(R.id.signInPassword)

        inButton.setOnClickListener{
          /*  Log.d(TAG1,username.text.toString())
            Log.d(TAG2,password.text.toString())
            Toast.makeText(activity,"HHH",Toast.LENGTH_SHORT).show()*/

            if(username.text.toString()!="" && password.text.toString()!="") {
                auth = FirebaseAuth.getInstance()
                auth.signInWithEmailAndPassword(username.text.toString(), password.text.toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "signInWithEmail:Success")
                            val user = auth.currentUser
                            startActivity(Intent(activity,Content::class.java))
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                        }
                    }
            }
        }

    }
}