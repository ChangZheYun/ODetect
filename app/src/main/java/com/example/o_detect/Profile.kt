package com.example.o_detect

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.profile.*
import kotlinx.android.synthetic.main.update_password.*

class Profile:Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view : View = inflater.inflate(R.layout.profile,container,false)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser!!.uid
        var path = "User/$userId/username"
        val databaseRef = FirebaseDatabase.getInstance().reference

        //持續監聽
        databaseRef.child(path).addValueEventListener( object: ValueEventListener {

              override fun onCancelled(p0: DatabaseError) {}

              override fun onDataChange(p0: DataSnapshot) {
                  profileUsername.text = p0.value.toString()
              }
        })

        //單次監聽
        path="User/$userId/email"
        databaseRef.child(path).addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                profileEmail.text = p0.value.toString()
            }
        })

        updateUsernameButton.setOnClickListener {
            val dialog = AlertDialog.Builder(activity)
            val inflater = activity!!.layoutInflater
            val updateUserNameXML = inflater.inflate(R.layout.update_username,null)

            //設定Dialog
            dialog.setView(updateUserNameXML)
                .setCancelable(false)
                .setTitle("更換名稱")
                .setPositiveButton(R.string.confirm,object :DialogInterface.OnClickListener{

                    override fun onClick(p0: DialogInterface?, p1: Int) {

                        val newUsername = updateUserNameXML.findViewById<TextInputEditText>(R.id.updateUsernameText)
                        Log.d("newUsername=",newUsername.text.toString())

                        if(newUsername.text!!.isEmpty()) {
                            Snackbar.make(view!!, "名稱不得為空", Snackbar.LENGTH_SHORT).show()
                        }else{
                            path="User/$userId/username"
                            databaseRef.child(path).setValue(newUsername.text.toString())
                            Snackbar.make(view!!, "更換名稱成功", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                })
                .setNegativeButton(R.string.cancel,null)
                .create()
                .show()
        }

        updatePasswordButton.setOnClickListener {
            val dialog = AlertDialog.Builder(activity)
            val inflater = activity!!.layoutInflater
            val updatePasswordXML = inflater.inflate(R.layout.update_password,null)

            //設定Dialog
            dialog.setView(updatePasswordXML)
                .setCancelable(false)
                .setTitle("更換密碼")
                .setPositiveButton(R.string.confirm,object :DialogInterface.OnClickListener{

                    override fun onClick(p0: DialogInterface?, p1: Int) {

                        val newPassword = updatePasswordXML.findViewById<TextInputEditText>(R.id.updatePasswordText)
                        Log.d("newPassWord=",newPassword.text.toString())

                        if(newPassword.text!!.isEmpty()) {
                            Snackbar.make(view!!, "密碼不得為空", Snackbar.LENGTH_SHORT).show()
                        }else{
                            auth.currentUser!!.updatePassword(newPassword.text.toString())
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Snackbar.make(view!!, "更換密碼成功", Snackbar.LENGTH_SHORT).show()
                                    } else {
                                        Snackbar.make(view!!, "更換密碼失敗:${task.exception}", Snackbar.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }
                })
                .setNegativeButton(R.string.cancel,null)
                .create()
                .show()
        }

        signOutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(activity, MainActivity::class.java))
        }
    }
}