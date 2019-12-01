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
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.internal.Sleeper
import kotlinx.android.synthetic.main.farm_overview.*
import java.lang.Exception

class FarmFragment:Fragment() {

    private lateinit var combineChart : CombinedChart
    private lateinit var pieChart : PieChart
    private var houseNum = 1
    private var pHealthSum = 0.0
    private var pUnHealthSum = 0.0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view : View = inflater.inflate(R.layout.farm_overview,container,false)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //ImageAsyncTask().execute("https://firebasestorage.googleapis.com/v0/b/civic-kayak-240607.appspot.com/o/HukCZC5CuTVGlC3yGXC8Pqzscmk1%2F20190821-02%3A20%3A02.jpg?alt=media&token=ac052a2d-9393-4997-bbef-247e62dd138f")

        //---設定CombineChart---
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

        //---設定PieChart---
        pieChart = farmPieChart
        pieChart.setNoDataText("無統計資料")
        pieChart.description.isEnabled = false
        //關閉label
        pieChart.legend.isEnabled = false
        //設定為可旋轉
        pieChart.isRotationEnabled = true
        //設定文字大小
        pieChart.setEntryLabelTextSize(10f)


        initData()
    }

    class houseData(var health:Float,var unhealth:Float,var housePlantSum:Float)

    private fun initData(){

        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val path = "MetaData/$userId"
        val databaseRef = FirebaseDatabase.getInstance().reference
        val statistic : MutableList<houseData> = mutableListOf()


        databaseRef.child(path).addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {

                context?.let {
                    val newHouseNum = it.getSharedPreferences("houseData", Context.MODE_PRIVATE).getInt("houseNum", 1)
                    if (houseNum != newHouseNum) {
                        houseNum = newHouseNum

                        databaseRef.child("$path/G$houseNum/health").setValue(0)
                        databaseRef.child("$path/G$houseNum/unhealth").setValue(0)
                        databaseRef.child("$path/G$houseNum/housePlantSum").setValue(0)
                    }
                }

                //寫入新資料要重讀一次
                databaseRef.child(path).addListenerForSingleValueEvent( object: ValueEventListener {

                    override fun onCancelled(p0: DatabaseError) {}

                    override fun onDataChange(p1: DataSnapshot) {

                        statistic.clear()
                        pHealthSum = 0.0
                        pUnHealthSum = 0.0


                        p1.children.forEach { it ->
                            //Log.i("data-key",it.key.toString())

                            Log.i("-----------4-----------", it.value.toString())
                            if (it.key.toString()[0] == 'G') {
                                val info = houseData(0F, 0F, 0F)
                                it.children.forEach {
                                    when (it.key) {
                                        "health" -> {
                                            info.health = it.value.toString().toFloat()
                                            pHealthSum += info.health
                                        }
                                        "unhealth" -> {
                                            info.unhealth = it.value.toString().toFloat()
                                            pUnHealthSum += info.unhealth
                                        }
                                        "housePlantSum" -> {
                                            info.housePlantSum = it.value.toString().toFloat()
                                        }
                                    }
                                }
                                statistic.add(info)
                            }
                        }

                       Log.i("健康數量:",pHealthSum.toString())
                       Log.i("不健康數量",pUnHealthSum.toString())
                       Log.i("溫室數量:",houseNum.toString())

                       /*if(statistic.size < houseNum){
                           statistic.add(houseData(0f,0f,0f))
                       }*/
                       Log.i("統計表數量:",statistic.size.toString())

                       showCombineChart(statistic)
                       showPieChart()

                        val pSum = pHealthSum + pUnHealthSum

                        try {
                            if (pSum == 0.0) {
                                farmPieText.setText(context!!.resources.getString(R.string.farm_no_plant))
                            } else {
                                when (pHealthSum / pSum) {
                                    in 0.9..1.0 -> {
                                        farmPieText.setText(context!!.resources.getString(R.string.farm_perfect))
                                    }
                                    in 0.7..0.9 -> {
                                        farmPieText.setText(context!!.resources.getString(R.string.farm_good))
                                    }
                                    else -> {
                                        farmPieText.setText(context!!.resources.getString(R.string.farm_bad))
                                    }
                                }
                            }
                        }catch(e:Exception){

                        }

                    }
                })
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
        context?.let { dataSet.color = ContextCompat.getColor(it,color) }
        context?.let { dataSet.valueTextColor = ContextCompat.getColor(it,R.color.colorHint) }
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
        context?.let { dataSet.color = ContextCompat.getColor(it,R.color.colorBar) }
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

    private fun showPieChart(){
        val pieStaticSum : MutableList<PieEntry> = mutableListOf()
        pieStaticSum .add(PieEntry(pHealthSum.toFloat(),"健康"))
        pieStaticSum .add(PieEntry(pUnHealthSum.toFloat(),"不健康"))

        val color : MutableList<Int> = mutableListOf()
        context?.let { color.add(ContextCompat.getColor(it,R.color.health)) }
        context?.let { color.add(ContextCompat.getColor(it,R.color.unhealth)) }

        val dataSet = PieDataSet(pieStaticSum,"溫室狀況")
        dataSet.colors = color

        val pieData = PieData(dataSet)
        pieData.setDrawValues(true)

        context?.let { pieData.setValueTextColor(ContextCompat.getColor(it,R.color.colorText)) }
        pieData.setValueTextSize(14f)
        pieData.setValueFormatter(PercentFormatter())

        //設定圓餅圖旁的提醒文字
        /*val pSum = pHealthSum + pUnHealthSum

        if(pSum == 0.0){
            Log.i("TESTTT","WHAT")
            farmPieText.text = context!!.resources.getString(R.string.farm_no_plant)
        }else {
            when (pHealthSum / pSum) {
                in 0.9..1.0 -> {
                    farmPieText.text = context!!.resources.getString(R.string.farm_perfect)
                }
                in 0.7..0.9 -> {
                    farmPieText.text = context!!.resources.getString(R.string.farm_good)
                }
                else -> {
                    farmPieText.text = context!!.resources.getString(R.string.farm_bad)
                }
            }
        }
*/

        /*dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        pieData.setValueTextColor(ContextCompat.getColor(activity!!,R.color.colorHint))
        dataSet.valueLinePart1OffsetPercentage = 10f
        dataSet.valueLinePart1Length = 0.43f
        dataSet.valueLinePart2Length = 0.1f
        dataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE*/

        pieChart.data = pieData
        pieChart.invalidate()

    }

    private fun showCombineChart(statistic:MutableList<houseData>){

        Log.i("顯示資訊，統計圖數量",statistic.size.toString())

        val allData = CombinedData()
        //Log.i("size",statistic.size.toString())
        //綁定柱狀圖
        val housePlantSum : MutableList<BarEntry> = mutableListOf()
        for(i in 0 until statistic.size){
            housePlantSum.add(BarEntry(i.toFloat(),statistic[i].housePlantSum))
        }
        //設定X軸資訊
        val house : MutableList<String> = mutableListOf()

        /* 此處新增溫室時會有NULL Exception問題，暫時用讀取資料庫解
        val houseNum = activity!!.getSharedPreferences("houseData", Context.MODE_PRIVATE).getInt("houseNum", 1)
        */
        for( i in 1..houseNum){
            house.add("溫室$i")
        }

        Log.i("combine--",houseNum.toString())
        Log.i("combine-housePlant--",housePlantSum.size.toString())

        combineChart.xAxis.valueFormatter = IndexAxisValueFormatter(house)
        allData.setData(getBarData(housePlantSum))

        //綁定折線圖(健康)
        val health : MutableList<Entry> = mutableListOf()
        for(i in 0 until statistic.size){
            health.add(Entry(i.toFloat(),statistic[i].health))
        }
        val healthDataSet = getLineData(health,R.color.health,"健康")

        //綁定折線圖(不健康)
        val unHealth : MutableList<Entry> = mutableListOf()
        for(i in 0 until statistic.size){
            unHealth.add(Entry(i.toFloat(),statistic[i].unhealth))
        }
        val unHealthDataSet = getLineData(unHealth,R.color.unhealth,"不健康")
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
