package com.example.o_detect

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.farm_overview.*
import kotlinx.android.synthetic.main.title_uploadimage.*
import java.lang.Exception
import java.lang.Thread.sleep
import java.net.URL
import kotlin.coroutines.coroutineContext

class FarmFragment:Fragment() {

    private lateinit var combineChart : CombinedChart

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view : View = inflater.inflate(R.layout.farm_overview,container,false)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //ImageAsyncTask().execute("https://firebasestorage.googleapis.com/v0/b/civic-kayak-240607.appspot.com/o/HukCZC5CuTVGlC3yGXC8Pqzscmk1%2F20190821-02%3A20%3A02.jpg?alt=media&token=ac052a2d-9393-4997-bbef-247e62dd138f")

        //圖表設定
        combineChart = farmCombineChart
        //取消description
        combineChart.description.isEnabled = false
        //手勢操作
        combineChart.setTouchEnabled(true)
        combineChart.isDragEnabled = true
        combineChart.setScaleEnabled(true)
        combineChart.setPinchZoom(true)
        //不顯示右側
        combineChart.axisRight.isEnabled = false
        combineChart.setBorderWidth(1f)
        //設定無資料文字
        combineChart.setNoDataText("無統計資料")
        combineChart.setDrawBorders(false)
        //設定Legend
        val legend = combineChart.legend
        legend.textSize = 12f
        legend.formSize = 14f
        legend.xEntrySpace = 16f
        legend.form = Legend.LegendForm.LINE
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        //legend.setDrawInside(false)
        //設定最大可視範圍

        //Y軸
        val yAxisLeft = combineChart.axisLeft
        yAxisLeft.granularity = 1f
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.axisMinimum = 0f //從0開始

        //X軸
        val xAxis = combineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)

        //設定字體大小
        xAxis.textSize = 15f
        yAxisLeft.textSize = 15f

        initData()

    }

    class houseData(var health:Float,var unhealth:Float,var housePlantSum:Float)

    private fun initData(){

        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val path = "MetaData/$userId"
        val databaseRef = FirebaseDatabase.getInstance().reference
        val statistic : MutableList<houseData> = mutableListOf()

        databaseRef.child(path).addValueEventListener( object: ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {

                statistic.clear()

                p0.children.forEach{ it ->
                    //Log.i("data-key",it.key.toString())
                    if(it.key.toString()[0]=='G') {
                        val info = houseData(0F,0F,0F)
                        it.children.forEach {
                            when (it.key) {
                                "health" ->{
                                    info.health = it.value.toString().toFloat()
                                }
                                "unhealth" ->{
                                    info.unhealth = it.value.toString().toFloat()
                                }
                                "housePlantSum" ->{
                                    info.housePlantSum = it.value.toString().toFloat()
                                }
                            }
                        }
                        statistic.add(info)
                    }
                }
                showCombineChart(statistic)
            }
        })
    }

    private fun getLineData(statistic:MutableList<Entry>,color:Int,label:String):LineDataSet{

        //設定圖表格式
        val dataSet = LineDataSet(statistic,label)
        //禁用heightLight
        dataSet.isHighlightEnabled = false
        //dataSet.highLightColor = ContextCompat.getColor(activity!!,R.color.colorButtonNormal)
        //點選資料橫豎顏色
        dataSet.color = color
        dataSet.valueTextColor = ContextCompat.getColor(activity!!,R.color.colorHint)
        //顯示值
        dataSet.valueTextSize = 12f
        dataSet.setDrawValues(true)
        dataSet.mode = LineDataSet.Mode.LINEAR               //設定線條形式
        dataSet.axisDependency = YAxis.AxisDependency.LEFT   //資料依賴左邊座標軸
        dataSet.lineWidth = 2f
        dataSet.valueFormatter = object : ValueFormatter(){  //設定label為整數
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        }

        return dataSet
    }

    private fun getBarData(statistic:MutableList<BarEntry>):BarData{

        //Bar資料
        val dataSet = BarDataSet(statistic,"數量")
        dataSet.valueTextSize = 12f
        dataSet.color = ContextCompat.getColor(activity!!,R.color.colorBar)
        dataSet.axisDependency = YAxis.AxisDependency.LEFT
        dataSet.setDrawValues(false)

        //防止超出邊界
        combineChart.xAxis.axisMinimum = -0.5f
        combineChart.xAxis.axisMaximum = (statistic.size-0.5).toFloat()

        //柱狀圖寬度
        val barData = BarData(dataSet)
        barData.barWidth = 0.7f


        return barData
    }

    private fun showCombineChart(statistic:MutableList<houseData>){

        val allData = CombinedData()
        //Log.i("size",statistic.size.toString())
        //綁定柱狀圖
        val housePlantSum : MutableList<BarEntry> = mutableListOf()
        for(i in 0 until statistic.size){
            housePlantSum.add(BarEntry(i.toFloat(),statistic[i].housePlantSum))
        }
        //設定X軸資訊
        val house : MutableList<String> = mutableListOf()
        val houseNum = activity!!.getSharedPreferences("houseData", Context.MODE_PRIVATE).getInt("houseNum",0)
        for( i in 1..houseNum){
            house.add("溫室$i")
        }
        combineChart.xAxis.valueFormatter = IndexAxisValueFormatter(house)
        allData.setData(getBarData(housePlantSum))

        //綁定折線圖(健康)
        val health : MutableList<Entry> = mutableListOf()
        for(i in 0 until statistic.size){
            health.add(Entry(i.toFloat(),statistic[i].health))
        }
        val healthDataSet = getLineData(health,ContextCompat.getColor(activity!!,R.color.health),"健康")

        //綁定折線圖(不健康)
        val unHealth : MutableList<Entry> = mutableListOf()
        for(i in 0 until statistic.size){
            unHealth.add(Entry(i.toFloat(),statistic[i].unhealth))
        }
        val unHealthDataSet = getLineData(unHealth,ContextCompat.getColor(activity!!,R.color.unhealth),"不健康")
        allData.setData(LineData(healthDataSet,unHealthDataSet))


        combineChart.data = allData
        combineChart.animateX(2500)
        combineChart.invalidate()
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
