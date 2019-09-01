package com.example.o_detect

import android.app.AlertDialog
import android.content.Context
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
import com.example.o_detect.ui.login.LoginActivity
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
        var path = "User/$userId"
        val databaseRef = FirebaseDatabase.getInstance().reference

        //Username顯示
        val username = activity!!.getSharedPreferences("userData", Context.MODE_PRIVATE).getString("username","")
        if(username!!.isEmpty()) {
            path = "User/$userId/username"
            databaseRef.child(path).addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {
                    profileUsername.text = p0.value.toString()
                    activity!!.getSharedPreferences("userData", Context.MODE_PRIVATE)
                        .edit().putString("username",p0.value.toString()).apply()
                }
            })
        }else{
            profileUsername.text = username
        }

        //Email顯示
        val email = activity!!.getSharedPreferences("userData", Context.MODE_PRIVATE).getString("email","")
        if(email!!.isEmpty()) {
            path = "User/$userId/email"
            databaseRef.child(path).addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {
                    profileEmail.text = p0.value.toString()
                    activity!!.getSharedPreferences("userData", Context.MODE_PRIVATE)
                        .edit().putString("email",p0.value.toString()).apply()
                }
            })
        }else{
            profileEmail.text = email
        }

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
                            path = "User/$userId/username"
                            databaseRef.child(path).setValue(newUsername.text.toString())
                            val preference = activity!!.getSharedPreferences("userData", Context.MODE_PRIVATE)
                            preference.edit().putString("username",newUsername.text.toString()).apply()
                            profileUsername.text = newUsername.text.toString()
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
            activity!!.getSharedPreferences("houseData", Context.MODE_PRIVATE).edit().clear().apply()
            activity!!.getSharedPreferences("userData", Context.MODE_PRIVATE).edit().clear().apply()
            startActivity(Intent(activity, MainActivity::class.java))
        }

        addGreenhouseButton.setOnClickListener{
            val preference = activity!!.getSharedPreferences("houseData", Context.MODE_PRIVATE)
            var houseNum = preference.getInt("houseNum",1)
            houseNum+=1
            preference.edit().putInt("houseNum",houseNum).apply()

            path = "MataData/$userId"
            databaseRef.child("$path/houseNum").setValue(houseNum)
            databaseRef.child("$path/G$houseNum/health").setValue(houseNum)
            databaseRef.child("$path/G$houseNum/unhealth").setValue(houseNum)
            databaseRef.child("$path/G$houseNum/housePlantSum").setValue(houseNum)
            Snackbar.make(view!!, "溫室數量: $houseNum", Snackbar.LENGTH_SHORT).show()
        }
    }
}