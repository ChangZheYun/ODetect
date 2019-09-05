package com.example.o_detect

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.widget.AppCompatSpinner
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.farm_overview.*
import kotlinx.android.synthetic.main.profile.*
import kotlinx.android.synthetic.main.title_datalist.*
import java.lang.Exception
import java.lang.Thread.activeCount
import java.lang.Thread.sleep
import java.net.URL
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

class DataListFragment :Fragment(){

    private lateinit var dataArray : ArrayList<DataModel?>
    private lateinit var auth : FirebaseAuth
    private lateinit var userId : String
    private lateinit var databaseRef : FirebaseDatabase
    private lateinit var storageRef : FirebaseStorage
    private lateinit var recycleradpater :DataAdapter
    private var greenhouseID = 1
    private var dataCount = 0
    private var date = UpImageFragment.DateUtils.formatDate(Date(), "yyyyMMdd")


    class DataAdapter : RecyclerView.Adapter<DataAdapter.ViewHolder>{

        private var context : Context
        private var data : ArrayList<DataModel?>

        constructor(context:Context,data:ArrayList<DataModel?>) : super(){
            this.context = context
            this.data = data
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataAdapter.ViewHolder {
            val cell = LayoutInflater.from(context).inflate(R.layout.dataitem,parent,false)
            val viewHolder = ViewHolder(cell)
            cell.layoutParams.height = 350
            viewHolder.image = cell.findViewById(R.id.dataItemImage)
            viewHolder.name = cell.findViewById(R.id.dataItemTitle)
            viewHolder.result = cell.findViewById(R.id.dataItemResult)
            return viewHolder
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun onBindViewHolder(holder: DataAdapter.ViewHolder, position: Int) {
            //背景載入圖片
            if(data[position]!!.imageURL.isNotEmpty()){
                Picasso.with(context)
                       .load(data[position]!!.imageURL)
                       .placeholder(R.drawable.photo_black_24dp)
                       .error(R.drawable.photo_black_24dp)
                       .into(holder.image)
            }
            //ImageAsyncTask().execute(AsyncModel(holder.image,data[position]?.imageURL.toString()) )
            holder.name.text = data[position]?.plantName.toString()
            holder.result.text = data[position]?.result.toString()
            Log.d("test---",holder.result.text.toString())
            /*holder.title.setOnClickListener{
                Log.d("Test--",holder.title.text.toString())
            }*/
        }

        class ViewHolder : RecyclerView.ViewHolder{
            constructor(itemView:View) : super(itemView){
                itemView.setOnClickListener{
                    Snackbar.make(itemView, adapterPosition.toString(), Snackbar.LENGTH_SHORT).show()
                }
            }

            lateinit var image : ImageView
            lateinit var name : TextView
            lateinit var result : TextView
        }
    }

    class DataModel {
        var plantName : String
        var imageURL : String
        var rid : String
        var result : String
        var timestamp : String
        constructor(plantName:String,imageURL:String,rid:String,result:String,timestamp:String){
            this.plantName = plantName
            this.imageURL = imageURL
            this.rid = rid
            this.result = result
            this.timestamp = timestamp
        }
    }

    //AsyncTask傳遞所需參數
    class AsyncModel{
        var image : ImageView
        var URL : String
        constructor(image:ImageView,URL:String){
            this.image = image
            this.URL = URL
        }
    }

    class ImageAsyncTask : AsyncTask<AsyncModel, Int, Bitmap>() {

        var image : ImageView? = null

        override fun onPreExecute() {
            super.onPreExecute()
            //testProgress.visibility = View.VISIBLE
            //testProgress.progress = 0
            //testProgress.show()
        }

        override fun doInBackground(vararg p0: AsyncModel?): Bitmap? {
            image = p0[0]?.image
            var bitmap : Bitmap? = null
            try {
                val url = URL(p0[0]?.URL)
                bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            }catch (e: Exception){
                Log.w("抓不到圖片","QQ")
                e.printStackTrace()
                return null
            }
            return bitmap
        }


        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
         //   testProgress.progress = values[0]!!.toInt()
        }

        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            // testProgress.hide()
            image?.setImageBitmap(result)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view : View = inflater.inflate(R.layout.title_datalist,null)

        //初始化
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser!!.uid
        databaseRef = FirebaseDatabase.getInstance()
        storageRef = FirebaseStorage.getInstance()

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        //初始資料
        //updateData()
        //初始View
        //initView()


       /* insertDataButton.setOnClickListener{
            Snackbar.make(view!!,"CLICK FLOATING", Snackbar.LENGTH_SHORT).show()
        }*/

        //設定更新顏色
        refreshRecycle.setColorSchemeColors(ContextCompat.getColor(context!!,R.color.colorPrimary))
        //設定更新事件
        refreshRecycle.setOnRefreshListener {
            updateData()
            //initView()
            Snackbar.make(view!!,"下拉更新", Snackbar.LENGTH_SHORT).show()
        }

        //取得greenhouse編號(從database取得溫室數量)
        val houseSpinner = activity!!.findViewById<AppCompatSpinner>(R.id.greenHouseDisplayList)
        var houseSpinnerList = arrayListOf("溫室1")
        //讀取溫室數量
        val houseNum = activity!!.getSharedPreferences("houseData",Context.MODE_PRIVATE).getInt("houseNum",1)
        if(houseNum >= 2) {
            for (i in 2..houseNum) {
                houseSpinnerList.add("溫室$i")
            }
        }
        /*var path = "MataData/$userId/houseNum"
        databaseRef.reference.child(path).addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.value.toString().toInt() >= 2) {
                    for (i in 2..p0.value.toString().toInt()) {
                        houseSpinnerList.add("溫室$i")
                    }
                }

                Log.i("溫室數量",p0.value.toString())
            }
        })*/

        var houseSpinnerAdapter = ArrayAdapter(activity!!,R.layout.greenhouse_list,houseSpinnerList)
        houseSpinnerAdapter.setDropDownViewResource(R.layout.greenhouse_list)
        houseSpinner.adapter = houseSpinnerAdapter
        houseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                greenhouseID = p2+1
                updateData()
            }

        }

        //設定檢視日期
        val dateList = activity!!.findViewById<TextView>(R.id.dateList)
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        //預設為當天
        dateList.text = date

        dateList.setOnClickListener {
            DatePickerDialog(activity!!, DatePickerDialog.OnDateSetListener { _, Year, Month, Day ->
                    // Display Selected date in textbox

                cal.set(Calendar.YEAR,Year)
                cal.set(Calendar.MONTH,Month)
                cal.set(Calendar.DATE,Day)

                dateList.text = SimpleDateFormat("yyyyMMdd",Locale.TAIWAN).format(cal.time)
                dateList.textSize = 16f

                //path = "Record/$userId/G$greenhouseID/${dateList.text}"
                date = dateList.text.toString()
                for( i in 0..20){
                    Log.i("日期----",date)}
                updateData()
                //initView()

            }, year, month, day).show()

        }


    }

    private fun updateData() {

        val path = "Record/$userId/G$greenhouseID/$date"
        val dataSearch = databaseRef.reference

        val init = DataModel("","","","","")
        dataArray = arrayListOf(init)
        dataArray.removeAt(0)

        dataSearch.child(path).addListenerForSingleValueEvent( object: ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {

                dataCount = 0
                //有資料時隱藏warning
                if(p0.childrenCount > 0)
                    noDataWarning.visibility = View.INVISIBLE
                //    dataArray.removeAt(dataArray.size-1)

                p0.children.forEach{ it ->
                    //  val value = it.getValue(DataJSON::class.java)!!
                    //Log.d("TAG",it.value.toString())
                    //if(dataCount==0)
                    //    dataArray.removeAt(0)
                    var data = DataModel("1","1","1", "1","1")
                    data.rid = it.key.toString()
                    Log.i("data-imageName:",data.rid)
                    it.children.forEach {
                        when(it.key){
                            "plantName" ->{
                                data.plantName = it.value.toString()
                            }
                            "result" ->{
                                data.result = it.value.toString()
                            }
                            "originURL" ->{
                                data.imageURL = it.value.toString()
                            }
                            "timestamp" ->{
                                data.timestamp = it.value.toString()
                            }
                        }
                    }
                    dataArray.add(data)
                    //Log.d("DDD-dataArray-","${dataArray[i]!!.imageURL} ${dataArray[i]!!.result}")
                    dataCount+=1
                }

                for( i in 0 until dataCount){
                    Log.i("Test-dataArray--","${dataArray[i]?.imageURL} ${dataArray[i]?.result}" )
                }
                initView()
            }
        })
    }

    //設定RecycleView
    private fun initView(){

        Log.i("陣列大小=",dataArray.size.toString())

        for( i in 0 until dataArray.size){
            Log.d("dataArray--","${dataArray[i]?.imageURL} ${dataArray[i]?.result}" )
        }

        val contentRecyclerView = activity!!.findViewById<RecyclerView>(R.id.dataList)
        val layoutManager = LinearLayoutManager(activity)
        recycleradpater = DataAdapter(activity!!,dataArray)

        if(contentRecyclerView != null) {
            contentRecyclerView.layoutManager = layoutManager
            contentRecyclerView.adapter = recycleradpater

            ItemTouchHelper(ItemDragHelperCallback()).attachToRecyclerView(contentRecyclerView)

            //完成載入後關閉
            refreshRecycle.isRefreshing= false
        }

        //設定layoutManage
       /* contentRecyclerView.layoutManager = layoutManager*/



    }

    inner class ItemDragHelperCallback : ItemTouchHelper.Callback(){
        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            //此處返回可以拖動的方向值
            var swipe = 0
            var move = 0
            //此處為 假設recyclerview不為空
            recyclerView.let {
                if (recyclerView.layoutManager is LinearLayoutManager) {
                    //左滑刪除
                    swipe = ItemTouchHelper.LEFT
                }
            }
            return makeMovementFlags(move, swipe)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun isItemViewSwipeEnabled(): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            databaseRef.reference.child( "Record/$userId/G$greenhouseID/$date/${dataArray[position]!!.rid}").removeValue()
            databaseRef.reference.child( "Plant/$userId/G$greenhouseID/${dataArray[position]!!.plantName}/${dataArray[position]!!.timestamp}").removeValue()
            storageRef.reference.child("$userId/G$greenhouseID/$date/originImage/${dataArray[position]!!.rid}.jpg").delete()
            //結果圖還沒刪除
            //刪除選中item
            dataArray.removeAt(position)
            recycleradpater.notifyItemRemoved(position)
            recycleradpater.notifyItemRangeChanged(position,dataArray.size)
        }
    }
  /*  class SpinnerBehavior : CoordinatorLayout.Behavior<androidx.appcompat.widget.AppCompatSpinner>{

        private var upReach: Boolean = false
        private var downReach: Boolean = false
        private var lastPosition = -1

        constructor()

        override fun onInterceptTouchEvent(
            parent: CoordinatorLayout,
            child: AppCompatSpinner,
            ev: MotionEvent
        ): Boolean {
            when(ev.action){
                MotionEvent.ACTION_DOWN ->{
                    downReach = false
                    upReach = false
                }
            }
            return super.onInterceptTouchEvent(parent, child, ev)
        }

        override fun onNestedFling(
            coordinatorLayout: CoordinatorLayout,
            child: AppCompatSpinner,
            target: View,
            velocityX: Float,
            velocityY: Float,
            consumed: Boolean
        ): Boolean {
            super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed)

        }
    }*/

}