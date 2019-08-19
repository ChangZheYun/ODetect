package com.example.o_detect

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.title_datalist.*

class DataListFragment :Fragment(){

    private var dataArray : Array<String?> = arrayOfNulls(20)

    class DataAdapter : RecyclerView.Adapter<DataAdapter.ViewHolder>{

        private var context : Context
        private var data : Array<String?>

        constructor(context:Context,data:Array<String?>) : super(){
            this.context = context
            this.data = data
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataAdapter.ViewHolder {
            val cell = LayoutInflater.from(context).inflate(R.layout.dataitem,parent,false)
            val viewHolder = ViewHolder(cell)
            cell.layoutParams.height = 930
            viewHolder.title = cell.findViewById(R.id.dataItemTitle)
            return viewHolder
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun onBindViewHolder(holder: DataAdapter.ViewHolder, position: Int) {
            holder.title.text = data[position]
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

            lateinit var title : TextView
        }
    }

    /*class DataModel {
        var title : String
        constructor(title:String){
            this.title = title
        }
    }*/

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view : View = inflater.inflate(R.layout.title_datalist,null)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initData()
        initView()

        insertDataButton.setOnClickListener{
            Snackbar.make(view!!,"CLICK FLOATING", Snackbar.LENGTH_SHORT).show()
        }

        //設定更新顏色
        refreshRecycle.setColorSchemeColors(ContextCompat.getColor(context!!,R.color.colorPrimary))
        //設定更新事件
        refreshRecycle.setOnRefreshListener {
            Snackbar.make(view!!,"下拉更新", Snackbar.LENGTH_SHORT).show()
            //完成載入後關閉
            refreshRecycle.isRefreshing= false
        }

    }

    //初始化資料
    private fun initData(){
        for(i in 0..19){
            dataArray[i]="項目 $i"
        }
    }

    //設定RecycleView
    private fun initView(){

        val contentRecyclerView = activity!!.findViewById<RecyclerView>(R.id.dataList)
        val layoutManager = LinearLayoutManager(activity)
        val adpater = DataAdapter(activity!!,dataArray)
        contentRecyclerView.layoutManager = layoutManager
        contentRecyclerView.adapter = adpater

        //設定layoutManage
       /* contentRecyclerView.layoutManager = layoutManager*/



    }

}