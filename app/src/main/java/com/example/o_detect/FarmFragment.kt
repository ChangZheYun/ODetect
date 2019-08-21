package com.example.o_detect

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.farm_overview.*
import kotlinx.android.synthetic.main.title_uploadimage.*
import java.lang.Exception
import java.lang.Thread.sleep
import java.net.URL
import kotlin.coroutines.coroutineContext

class FarmFragment:Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view : View = inflater.inflate(R.layout.farm_overview,container,false)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        ImageAsyncTask().execute("https://firebasestorage.googleapis.com/v0/b/civic-kayak-240607.appspot.com/o/HukCZC5CuTVGlC3yGXC8Pqzscmk1%2F20190821-02%3A20%3A02.jpg?alt=media&token=ac052a2d-9393-4997-bbef-247e62dd138f")
    }

    //餵入網址(String)，取得進度(Int)，得到圖片(Bitmap)
    inner class ImageAsyncTask : AsyncTask<String, Int, Bitmap>() {

        // override fun onPreExecute() { super.onPreExecute() }
        // override fun onProgressUpdate(vararg values: Int?) { super.onProgressUpdate(*values) }
        // override fun onPostExecute(result: Bitmap?) { super.onPostExecute(result) }

        override fun onPreExecute() {
            super.onPreExecute()
            //testProgress.visibility = View.VISIBLE
            testProgress.progress = 0
            testProgress.show()
        }

        override fun doInBackground(vararg p0: String?): Bitmap? {
            var progress = 10
            var bitmap : Bitmap ?=null
            try {
                val url = URL(p0[0])
                bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            }catch (e:Exception){
                e.printStackTrace()
                return null
            }
            progress+=30
            publishProgress(progress)
            return bitmap
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            testProgress.progress = values[0]!!.toInt()
        }

        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
           // testProgress.hide()
            testImage.setImageBitmap(result)
        }

    }


}
