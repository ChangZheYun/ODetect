package com.example.o_detect

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*


class TitleGridView : BaseAdapter(){

    //Title GridView
    private val signText = arrayOf("Sign Up","Sign In")

    override fun getView(position:Int, convertView: View?, parent: ViewGroup?): View {

        //Inflate (將layout:title_login找出)
        val inflater = parent?.context?.
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.title_login,null)

        //Get GridView and TextView
        val titleGridView = view.findViewById<GridView>(R.id.titleGridView)
        val titleText = view.findViewById<TextView>(R.id.titleGridText)

        //Push data to titleText
        titleText.text = signText[position]

        titleGridView.setOnClickListener{
            Toast.makeText(parent.context,
                "Clicked : ${signText[position]}",Toast.LENGTH_SHORT).show()
        }
        return view
    }

    override fun getItem(p0: Int): Any {
        return signText[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return signText.size
    }

}