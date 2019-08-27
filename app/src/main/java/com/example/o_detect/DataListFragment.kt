package com.example.o_detect

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.farm_overview.*
import kotlinx.android.synthetic.main.profile.*
import kotlinx.android.synthetic.main.title_datalist.*
import java.lang.Exception
import java.lang.Thread.sleep
import java.net.URL
import java.util.*
import kotlin.math.absoluteValue

class DataListFragment :Fragment(){

    private lateinit var dataArray : Array<DataModel?>
    private lateinit var auth : FirebaseAuth
    private lateinit var userId : String
    private lateinit var databaseRef : FirebaseDatabase
    private lateinit var recycleradpater :DataAdapter
    private var greenhouseID = 1
    private var dataCount = 0


    class DataAdapter : RecyclerView.Adapter<DataAdapter.ViewHolder>{

        private var context : Context
        private var data : Array<DataModel?>

        constructor(context:Context,data:Array<DataModel?>) : super(){
            this.context = context
            this.data = data
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataAdapter.ViewHolder {
            val cell = LayoutInflater.from(context).inflate(R.layout.dataitem,parent,false)
            val viewHolder = ViewHolder(cell)
            cell.layoutParams.height = 900
            viewHolder.image = cell.findViewById(R.id.dataItemImage)
            viewHolder.id = cell.findViewById(R.id.dataItemTitle)
            viewHolder.result = cell.findViewById(R.id.dataItemResult)
            return viewHolder
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun onBindViewHolder(holder: DataAdapter.ViewHolder, position: Int) {
            //背景載入圖片
            ImageAsyncTask().execute(AsyncModel(holder.image,data[position]?.imageURL.toString()) )
            holder.id.text = data[position]?.id.toString()
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
            lateinit var id : TextView
            lateinit var result : TextView
        }
    }

    class DataModel {
        var imageURL : String
        var id : String
        var result : String
        constructor(imageURL:String,id:String,result:String){
            this.imageURL = imageURL
            this.id = id
            this.result = result
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

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        //初始資料
        getDataCount()
        //初始View
        //initView()


       /* insertDataButton.setOnClickListener{
            Snackbar.make(view!!,"CLICK FLOATING", Snackbar.LENGTH_SHORT).show()
        }*/

        //設定更新顏色
        refreshRecycle.setColorSchemeColors(ContextCompat.getColor(context!!,R.color.colorPrimary))
        //設定更新事件
        refreshRecycle.setOnRefreshListener {
            getDataCount()
            initView()
            Snackbar.make(view!!,"下拉更新", Snackbar.LENGTH_SHORT).show()
        }

        //取得greenhouse編號(從database取得溫室數量)
        val spinner = activity!!.findViewById<androidx.appcompat.widget.AppCompatSpinner>(R.id.greenHouseDisplayList)
        var spinnerList = arrayListOf("溫室1")

        val path = "User/$userId/greenhouseNumber"
        databaseRef.reference.child(path).addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                for( i in 2 .. p0.value.toString().toInt()){
                    spinnerList.add("溫室$i")
                }

                Log.i("溫室數量",p0.value.toString())
            }
        })

        val spinnerAdapter = ArrayAdapter(activity,R.layout.greenhouse_list,spinnerList)
        spinnerAdapter.setDropDownViewResource(R.layout.greenhouse_list)
        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                greenhouseID = p2+1
                getDataCount()
                initView()
                //updateData()
            }

        }

    }

    //初始化資料
    private fun getDataCount(){

        //設定路徑
        val path = "Greenhouse/$userId/G$greenhouseID"
        val dataSearch = databaseRef.reference

        if(dataCount==0){
            dataArray = arrayOfNulls(1)
            dataArray[0]=DataModel("Null","Null","無資料，馬上新增一筆吧!")
            initView()
        }

        //取得資料大小
        dataSearch.child(path).addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                dataCount = p0.childrenCount.toInt()
                Log.i("Count--",dataCount.toString())
                dataArray = arrayOfNulls(dataCount)
                if(dataCount==0){
                    dataArray = arrayOfNulls(1)
                    dataArray[0]=DataModel("Null","Null","無資料，馬上新增一筆吧!")
                }
                updateData()
                //initView()
            }
        })


    }

    private fun updateData() {

        val path = "Greenhouse/$userId/G$greenhouseID"
        val dataSearch = databaseRef.reference

        dataSearch.child(path).addValueEventListener( object: ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {

                var i = 0
                p0.children.forEach{ it ->
                    //  val value = it.getValue(DataJSON::class.java)!!
                    //Log.d("TAG",it.value.toString())
                    var data = DataModel("1","1", "1")
                    it.children.forEach {
                        if(it.key=="result") {
                            data.result = it.value.toString()
                        }else if(it.key=="originURL"){
                            data.imageURL = it.value.toString()
                        }
                    }
                    Log.i("i=",i.toString())
                    data.id = (i+1).toString()
                    dataArray[i] = data
                    Log.d("DDD-dataArray-","${dataArray[i]!!.imageURL} ${dataArray[i]!!.result}")
                    i++
                }

                recycleradpater.notifyDataSetChanged()

                /*   for (ds:DataSnapshot in p0.children) {
                       val value = ds.getValue(String::class.java)!!
                       Log.d("TAG",ds.key)
                   }
                   Log.d("TEST--",p0.value.toString())*/

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

        contentRecyclerView.layoutManager = layoutManager
        contentRecyclerView.adapter = recycleradpater



        //完成載入後關閉
        refreshRecycle.isRefreshing= false

        //設定layoutManage
       /* contentRecyclerView.layoutManager = layoutManager*/



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