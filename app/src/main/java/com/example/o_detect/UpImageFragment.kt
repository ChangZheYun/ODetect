package com.example.o_detect

import android.content.pm.PackageManager
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.renderscript.Sampler
import android.text.format.DateUtils
import android.util.Log
import android.view.*
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.widget.ContentLoadingProgressBar
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.internal.Sleeper
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.content_display.*
import kotlinx.android.synthetic.main.home.*
import kotlinx.android.synthetic.main.title_uploadimage.*
import kotlinx.android.synthetic.main.title_uploadimage.view.*
import kotlinx.android.synthetic.main.title_uploadimage.view.imageView
import org.w3c.dom.Text
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Exception
import java.lang.Thread.interrupted
import java.lang.Thread.sleep
import java.net.Socket
import java.net.URL
import java.sql.Time
import java.text.DateFormat
import java.text.DateFormat.getDateInstance
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread
import kotlin.concurrent.timer
import kotlin.concurrent.timerTask
import kotlin.system.exitProcess

class UpImageFragment : Fragment() {

    private lateinit var openAlbum : Button
    private lateinit var hintText : TextView
    private lateinit var progressBar : ContentLoadingProgressBar
    private lateinit var storageFirebase :FirebaseStorage
    private lateinit var plantName : TextInputEditText
    private lateinit var setImageHandler : Handler
    private lateinit var auth : FirebaseAuth
    private lateinit var userId : String
    private var greenhouseID = 1
    private var packetStatus = 0
    private var date = DateUtils.formatDate(Date(), "yyyyMMdd")
    private var key = ""
    private var housePlantSum = 0
    private var healthNum = 0
    private var unHealthNum = 0

    companion object{
        private const val PHOTO_FROM_GALLERY = 1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view : View = inflater.inflate(R.layout.title_uploadimage,null)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        openAlbum = activity!!.findViewById(R.id.uploadImageButton)
        hintText = activity!!.findViewById(R.id.hintImageProgressText)
        progressBar = activity!!.findViewById(R.id.uploadImageProgress)
        storageFirebase = FirebaseStorage.getInstance()
        plantName = activity!!.findViewById(R.id.plantNameText)

        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser!!.uid

        openAlbum.setOnClickListener{
            plantName = activity!!.findViewById(R.id.plantNameText)
            if(plantName.text!!.isNotEmpty()) {
                onGallery()
            }else{
                Snackbar.make(floatingCoordinate,"請輸入蘭花名稱",Snackbar.LENGTH_SHORT).show()
            }
        }

        insertImageButton.setOnClickListener{
            uploadImageData()
        }

        //取得greenhouse編號(從database取得溫室數量)
        val spinner = activity!!.findViewById<AppCompatSpinner>(R.id.greenHouseList)
        val spinnerList = arrayListOf("溫室1")

        var path = "MetaData/$userId/houseNum"
        val databaseRef = FirebaseDatabase.getInstance().reference
        val preference = activity!!.getSharedPreferences("houseData",Context.MODE_PRIVATE)
        val houseNum = preference.getInt("houseNum",0)
        if(houseNum == 0) {
            databaseRef.child(path).addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {
                    //將溫室資料存在local(使用apply效能較好)
                    preference.edit().putInt("houseNum", p0.value.toString().toInt()).apply()
                    if (p0.value.toString().toInt() >= 2) {
                        for (i in 2..p0.value.toString().toInt()) {
                            spinnerList.add("溫室$i")
                        }
                    }

                    Log.i("溫室數量", p0.value.toString())
                }
            })
        }else{
            if (houseNum >= 2) {
                for (i in 2..houseNum) {
                    spinnerList.add("溫室$i")
                }
            }
        }

        val spinnerAdapter = ArrayAdapter(activity!!,R.layout.greenhouse_list,spinnerList)
        spinnerAdapter.setDropDownViewResource(R.layout.greenhouse_list)
        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                greenhouseID = p2+1
            }

        }

        setImageHandler = object : Handler(){
            override fun handleMessage(msg: Message?) {
                super.handleMessage(msg)
                if(packetStatus == 1){
                    hintText.text = resources.getString(R.string.hint4)
                }else if(packetStatus == 2){
                    hintText.text = resources.getString(R.string.hint5)
                    hintText.visibility = View.INVISIBLE
                    progressBar.visibility = View.INVISIBLE
                    //載入圖片
                    path = "Record/$userId/G$greenhouseID/$date/$key"
                    databaseRef.child("$path/detectURL").addListenerForSingleValueEvent(object : ValueEventListener {

                        override fun onCancelled(p0: DatabaseError) {}

                        override fun onDataChange(p0: DataSnapshot) {
                            imageView.visibility = View.VISIBLE
                            Picasso.with(context)
                                .load(p0.value.toString())
                                .placeholder(R.drawable.photo_black_24dp)
                                .error(R.drawable.photo_black_24dp)
                                .into(imageView)
                        }
                    })
                    databaseRef.child("$path/result").addListenerForSingleValueEvent(object : ValueEventListener {

                        override fun onCancelled(p0: DatabaseError) {}

                        override fun onDataChange(p0: DataSnapshot) {
                            imageResult.visibility = View.VISIBLE
                            imageHouse.visibility = View.VISIBLE
                            imagePlantName.visibility = View.VISIBLE
                            imageDate.visibility = View.VISIBLE
                            path = "MetaData/$userId/G$greenhouseID"
                            val result = p0.value.toString()
                            if(result == "health"){
                                healthNum += 1
                                imageResult.text = resources.getString(R.string.result_health)
                                imageResult.setTextColor(ContextCompat.getColor(context!!,R.color.health))
                                databaseRef.child("$path/health").setValue(healthNum.toString())
                                Log.i("寫入了嗎???",healthNum.toString())
                            }else{
                                unHealthNum += 1
                                imageResult.text = resources.getString(R.string.result_unhealth)
                                imageResult.setTextColor(ContextCompat.getColor(context!!,R.color.unhealth))
                                databaseRef.child("$path/unhealth").setValue(unHealthNum.toString())
                            }
                        }
                    })
                }
            }
        }

    }

    object DateUtils {
        fun formatDate(date: Date, pattern: String): String = SimpleDateFormat(pattern).format(date)
    }

    private fun onGallery(){

        //先初始化資料
        housePlantSum = 0
        healthNum = 0
        unHealthNum = 0

        //獲取當前資訊
        val mPath = "MetaData/$userId/G$greenhouseID"
        val databaseRef = FirebaseDatabase.getInstance().reference
        databaseRef.child(mPath).addListenerForSingleValueEvent( object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach{
                    when(it.key.toString()){
                        "health" ->{
                            healthNum = it.value.toString().toInt()
                            Log.i("健康數量初始化--",healthNum.toString())
                        }
                        "unhealth" ->{
                            unHealthNum = it.value.toString().toInt()
                        }
                        "housePlantSum" ->{
                            housePlantSum = it.value.toString().toInt()
                        }
                    }
                    Log.i("資訊初始化--",it.value.toString())
                }

            }
        })

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent,PHOTO_FROM_GALLERY)
    }

    //設定"上傳圖片"的floatingButton
    private fun uploadImageData(){

        //將之前結果關閉
        imageView.visibility = View.INVISIBLE
        imageResult.visibility = View.INVISIBLE
        imageHouse.visibility = View.INVISIBLE
        imagePlantName.visibility = View.INVISIBLE
        imageDate.visibility = View.INVISIBLE

        val dialog = AlertDialog.Builder(activity,R.style.dialogSoftKeyboardHidden)
        val inflater = activity!!.layoutInflater
        val dataUploadXML = inflater.inflate(R.layout.data_upload,null)


        val spinnerList = arrayListOf("溫室1")
        val spinner = dataUploadXML.findViewById<AppCompatSpinner>(R.id.floatingGreenHouseList)
        plantName = dataUploadXML.findViewById(R.id.floatingPlantNameText)

        val path = "MetaData/$userId/houseNum"
        val databaseRef = FirebaseDatabase.getInstance().reference
        val preference = activity!!.getSharedPreferences("houseData",Context.MODE_PRIVATE)
        val houseNum = preference.getInt("houseNum",0)
        if(houseNum == 0) {
            databaseRef.child(path).addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {
                    //將溫室資料存在local(使用apply效能較好)
                    preference.edit().putInt("houseNum", p0.value.toString().toInt()).apply()
                    if (p0.value.toString().toInt() >= 2) {
                        for (i in 2..p0.value.toString().toInt()) {
                            spinnerList.add("溫室$i")
                        }
                    }
                }
            })
        }else{
            if (houseNum >= 2) {
                for (i in 2..houseNum) {
                    spinnerList.add("溫室$i")
                }
            }
        }

        val spinnerAdapter = ArrayAdapter(activity!!,R.layout.greenhouse_list,spinnerList)
        spinnerAdapter.setDropDownViewResource(R.layout.greenhouse_list)
        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                greenhouseID = p2+1
            }

        }


        //設定Dialog
        dialog.setView(dataUploadXML)
            .setCancelable(false)
            .setTitle("上傳圖片")
            .setPositiveButton(R.string.confirm){ _ , _ ->

                if(plantName.text!!.isEmpty()) {
                    Snackbar.make(floatingCoordinate, "請輸入蘭花名稱", Snackbar.LENGTH_SHORT).show()
                }else{
                    onGallery()
                }

            }
            .setNegativeButton(R.string.cancel,null)
            .create()
            .show()
    }


    //從相簿回來
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            PHOTO_FROM_GALLERY -> {
                when(resultCode){
                    Activity.RESULT_OK ->{

                        //設定上傳進度(progress)
                        greenHouseList.visibility = View.INVISIBLE
                        plantNameTextLayout.visibility = View.INVISIBLE
                        hintText.visibility = View.VISIBLE
                        progressBar.visibility = View.VISIBLE
                        hintText.text = resources.getString(R.string.hint1)

                        openAlbum.visibility = View.INVISIBLE
                        imageView.visibility = View.INVISIBLE


                        val url = data!!.data
                        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()

                        //設定日期及時間
                        date = DateUtils.formatDate(Date(), "yyyyMMdd")
                        val timestamp = DateUtils.formatDate(Date(), "yyyyMMdd-HH:mm:ss")
                        //設定病歷key(全域變數)
                        val databaseRef = FirebaseDatabase.getInstance().reference
                        key = databaseRef.push().key!!
                        //設定上傳Ref
                        val userUploadImageRef = storageFirebase.reference
                            .child("$userId/G$greenhouseID/$date/originImage/$key.jpg")
                        val userUploadImage = userUploadImageRef.putFile(url!!)
                        //取得DownloadURL
                        userUploadImage.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>>{ task->
                            if (!task.isSuccessful) {
                                task.exception?.let {
                                    throw it
                                }
                            }
                            return@Continuation userUploadImageRef.downloadUrl
                        }).addOnCompleteListener { task ->
                            if(task.isSuccessful){

                                //判斷是否存在，對資訊做更新
                                var path = "Plant/$userId/G$greenhouseID/${plantName.text.toString()}"
                                databaseRef.child(path).addListenerForSingleValueEvent( object : ValueEventListener{

                                    override fun onCancelled(p0: DatabaseError) {}

                                    override fun onDataChange(p0: DataSnapshot) {

                                        //子節點不存在sum-1，若存在則依照最後一筆結果判斷health或unHealth-1
                                        if (p0.childrenCount.toInt() == 0) {
                                            Log.i("是否存在子節點", "不存在")
                                            housePlantSum += 1
                                        } else {
                                            Log.i("是否存在子節點", "存在")
                                            var rKey = p0.children.last().key
                                            val rid = p0.children.last().value.toString()
                                            rKey = rKey!!.split('-')[0]
                                            Log.i("rKey", rKey.toString())
                                            Log.i("rid", rid)

                                            val rPath = "Record/$userId/G$greenhouseID/$rKey/$rid/result"
                                            databaseRef.child(rPath)
                                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                                    override fun onCancelled(p0: DatabaseError) {}
                                                    override fun onDataChange(p0: DataSnapshot) {
                                                        when (p0.value.toString()) {
                                                            "health" -> {
                                                                healthNum -= 1
                                                                Log.i("健康數量--", healthNum.toString())
                                                            }
                                                            "unhealth" -> {
                                                                unHealthNum -= 1
                                                                Log.i("不健康數量--", unHealthNum.toString())
                                                            }
                                                        }
                                                    }
                                                })
                                        }

                                        Log.i("健康數量---",healthNum.toString())

                                        //同步病歷rid給Plant
                                        path = "Plant/$userId/G$greenhouseID/${plantName.text.toString()}/$timestamp"
                                        databaseRef.child(path).setValue(key)
                                        Log.i("病歷寫入--", key)

                                        //同步housePlantSum
                                        path = "MetaData/$userId/G$greenhouseID/housePlantSum"
                                        databaseRef.child(path).setValue(housePlantSum.toString())
                                        Log.i("寫入植物總數--", housePlantSum.toString())

                                    }

                                })


                                //將病歷寫入資料庫
                                path = "Record/$userId/G$greenhouseID/$date"
                                databaseRef.child("$path/$key/plantName").setValue(plantName.text.toString())
                                databaseRef.child("$path/$key/originURL").setValue(task.result.toString()) //task.result取得downloadURL
                                databaseRef.child("$path/$key/detectURL").setValue("Null")
                                databaseRef.child("$path/$key/result").setValue("Null")
                                databaseRef.child("$path/$key/timestamp").setValue(timestamp)


                                imageHouse.text = String.format(resources.getString(R.string.result_house),greenhouseID)
                                imagePlantName.text = String.format(resources.getString(R.string.result_plant_name),plantName.text.toString())
                                imageDate.text = String.format(resources.getString(R.string.result_date),timestamp)

                                //清除名稱
                                plantNameText.setText("")

                                //傳送socket
                                Log.i("傳送socket測試","測試測試")
                                packetStatus = 0
                                hintText.text = resources.getString(R.string.hint3)
                                PacketAsyncTask().execute(PacketModel(task.result.toString(),path))
                                //PacketAsyncTask().cancel(true)
                            }

                        }
                        //取得上傳進度
                        userUploadImage.addOnProgressListener { task ->
                            val progress = (100.0 * task.bytesTransferred) / task.totalByteCount
                            progressBar.progress = progress.toInt()
                           // Log.d("UPLOAD--","Upload is $progress% done")
                            if(progress>=100){
                                hintText.text = resources.getString(R.string.hint2)
                                //顯示圖片
                                //progressBar.visibility = View.INVISIBLE
                                //imageView.visibility = View.VISIBLE
                                //imageView.setImageURI(url)
                            }
                        }
                    }
                    Activity.RESULT_CANCELED ->{
                        Log.d("uploadCheckImage","userNoUpload")
                    }
                }
            }
        }
    }

    //AsyncTask傳遞所需參數
    class PacketModel{
        var URL : String
        var path : String
        constructor(URL:String,path:String){
            this.URL = URL
            this.path = path
        }
    }

    inner class PacketAsyncTask : AsyncTask<PacketModel, Int, Int>() {

        override fun doInBackground(vararg p0: PacketModel?): Int {
            val host = "120.113.173.82"
            val port = "6688"

            try {
                //開thread處理client傳送socket
                /*val clientThread = Thread {
                    val data = PacketModel(p0[0]!!.URL,p0[0]!!.path)
                    val client = ClientSocket(host, port.toInt())
                    client.update(data)
                }
                clientThread.start()*/
                val data = PacketModel(p0[0]!!.URL, p0[0]!!.path)
                val client = ClientSocket(host, port.toInt())
                client.update(data)
            }catch (e: InterruptedException){
                Log.i("Socket斷線","已斷線")
                PacketAsyncTask().cancel(true)
            }catch (e: Exception) {
                e.printStackTrace()
            }

            return 0
        }

        //客戶端
        inner class  ClientSocket (host: String, port: Int ) :Socket(host,port){

            var reader = BufferedReader(InputStreamReader(inputStream))
            var writer = PrintWriter(outputStream, true)

            fun update(data : PacketModel) {

                val clientRead = ReadHandler(reader)
                clientRead.build()

                val timer = Timer()
                timer.schedule(object : TimerTask(){
                    override fun run() {
                        if(packetStatus == 1){
                            setImageHandler.sendEmptyMessage(0)
                            Log.i("[Socket]","Server get request")
                        }else if(packetStatus == 2){
                            setImageHandler.sendEmptyMessage(0)
                            timer.cancel()
                            timer.purge()
                            Log.i("[Socket]","Closed")
                            clientRead.interrupt()
                            reader.close()
                            writer.close()
                            PacketAsyncTask().cancel(true)
                        }else{
                            val msg = data.URL
                            //向服務器寫入
                            writer.println(msg)
                            Log.i("[Socket]","Connected")
                        }
                    }

                },0,5000)
            }

        }

        //開啟一個線程從服務器讀取數據
        inner class ReadHandler(var reader: BufferedReader) : Thread() {

            fun build(): ReadHandler {
                start()
                return this
            }

            override fun run() {
                while (true) {
                    //從服務器讀取一行
                    val line = reader.read().toChar()
                    //向控制台寫入
                    //print(line)
                    if(line == '1') {
                        packetStatus = 1
                    }else if(line == '2'){
                        packetStatus = 2
                        break
                    }
                }
            }

        }
    }


}