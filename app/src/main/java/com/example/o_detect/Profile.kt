package com.example.o_detect

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Profile:Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view : View = inflater.inflate(R.layout.profile,container,false)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val profileUsername = activity!!.findViewById<TextView>(R.id.profileUsername)
        val signOutButton = activity!!.findViewById<com.google.android.material.button.MaterialButton>(R.id.signOutButton)

        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid.toString()
        val path = "User/$userId/username"
        val databaseRef = FirebaseDatabase.getInstance().reference.child(path)


        databaseRef.addListenerForSingleValueEvent( object: ValueEventListener {

              override fun onCancelled(p0: DatabaseError) {}

              override fun onDataChange(p0: DataSnapshot) {
                  profileUsername.text = p0.value.toString()
              }
        })

        signOutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(activity, MainActivity::class.java))
        }
    }
}