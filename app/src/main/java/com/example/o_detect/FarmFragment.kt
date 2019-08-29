package com.example.o_detect

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
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
        //ImageAsyncTask().execute("https://firebasestorage.googleapis.com/v0/b/civic-kayak-240607.appspot.com/o/HukCZC5CuTVGlC3yGXC8Pqzscmk1%2F20190821-02%3A20%3A02.jpg?alt=media&token=ac052a2d-9393-4997-bbef-247e62dd138f")

        val linechart = farmLineChart
        val entries : MutableList<Entry> = mutableListOf()
        //設定資料
        entries.add(Entry(0F,4F))
        entries.add(Entry(1f,1f))
        entries.add(Entry(2f,2f))
        entries.add(Entry(3f,4f))
        entries.add(Entry(5f,10f))
        //設定圖表格式
        val dataset = LineDataSet(entries,"Customized values")
        //點選資料橫豎顏色
        dataset.highLightColor = ContextCompat.getColor(activity!!,R.color.colorButtonNormal)
        dataset.color = ContextCompat.getColor(activity!!,R.color.colorButtonNormal)
        dataset.valueTextColor = ContextCompat.getColor(activity!!,R.color.colorHint)

        val xAxis = linechart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        val months = arrayOf("Jan", "Feb", "Mar", "Apr","May","June")
        /*val formatter = ValueFormatter{
        }*/

        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(months)


        val yAxisLeft = linechart.axisLeft
        yAxisLeft.setGranularity(1f)

        //取消description
        linechart.description.isEnabled = false
        //不顯示格線
        linechart.xAxis.setDrawGridLines(false)
        linechart.axisLeft.setDrawGridLines(false)
        //不顯示右側
        linechart.axisRight.isEnabled = false
        //設定無資料文字
        linechart.setNoDataText("無溫室資料")
        //設定字體大小
        xAxis.textSize = 16f
        yAxisLeft.textSize = 16f
        dataset.valueTextSize = 12f

        // Setting Data
        val data = LineData(dataset)
        linechart.setData(data)
        linechart.animateX(2500)
        //refresh
        linechart.invalidate()

    }

    /*測試用程式
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

    }*/


}
