package com.example.o_detect

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.CheckBox
import android.widget.TextView




class TitleGridView : BaseAdapter(){

    private lateinit var titleView : View

    //Title GridView (此處以後可用傳遞constructor修改)
    private val signText = arrayOf("Sign Up","Sign In")

    override fun getView(position:Int, convertView: View?, parent: ViewGroup?): View {

        //Inflate (將layout:title_login找出)
        val inflater = parent?.context?.
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if(convertView == null) {

            titleView = inflater.inflate(R.layout.title_login,null)

            //Get GridView and TextView
            //val titleGridView = titleView.findViewById<GridView>(R.id.titleGridView)
            val titleText = titleView.findViewById<TextView>(R.id.titleGridText)

            //Push data to titleText
            titleText.text = signText[position]
        }
        return titleView
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