package com.example.o_detect

import android.content.pm.PackageManager
import android.os.Bundle
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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.renderscript.Sampler
import android.text.format.DateUtils
import android.util.Log
import android.view.*
import androidx.annotation.NonNull
import androidx.core.content.FileProvider
import androidx.core.widget.ContentLoadingProgressBar
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.title_uploadimage.*
import kotlinx.android.synthetic.main.title_uploadimage.view.*
import kotlinx.android.synthetic.main.title_uploadimage.view.imageView
import org.w3c.dom.Text
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Exception
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
    private var greenhouseID = 1

    companion object{
        private const val PHOTO_FROM_GALLERY = 1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view : View = inflater.inflate(R.layout.title_uploadimage,null)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        greenHouseList.visibility = View.VISIBLE

        openAlbum = activity!!.findViewById(R.id.uploadImageButton)
        hintText = activity!!.findViewById(R.id.hintImageProgressText)
        progressBar = activity!!.findViewById(R.id.uploadImageProgress)
        storageFirebase = FirebaseStorage.getInstance()

        openAlbum.setOnClickListener{
            onGallery()
        }

        insertImageButton.setOnClickListener{
            onGallery()
        }

        //取得greenhouse編號(從database取得溫室數量)
        val spinner = activity!!.findViewById<androidx.appcompat.widget.AppCompatSpinner>(R.id.greenHouseList)
        var spinnerList = arrayListOf("溫室1")

        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser!!.uid
        val path = "MataData/$userId/houseSum"
        val databaseRef = FirebaseDatabase.getInstance().reference
        databaseRef.child(path).addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.value.toString().toInt() >= 2) {
                    for (i in 2..p0.value.toString().toInt()) {
                        spinnerList.add("溫室$i")
                    }
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
            }

        }

    }

    object DateUtils {
        fun formatDate(date: Date, pattern: String): String = SimpleDateFormat(pattern).format(date)
    }

    private fun onGallery(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent,PHOTO_FROM_GALLERY)
    }

    //從相簿回來
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //設定上傳進度(progress)
        greenHouseList.visibility = View.INVISIBLE
        hintText.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        hintText.text = resources.getString(R.string.hint1)

        when(requestCode){
            PHOTO_FROM_GALLERY -> {
                when(resultCode){
                    Activity.RESULT_OK ->{

                        openAlbum.visibility = View.INVISIBLE
                        imageView.visibility = View.INVISIBLE

                        val url = data!!.data
                        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()

                        //設定圖片名稱(當前時間)
                        val imageName = DateUtils.formatDate(Date(), "yyyyMMdd-HH:mm:ss")
                        //設定上傳Ref
                        val userUploadImageRef = storageFirebase.reference
                            .child("$userId/G$greenhouseID/originImage/$imageName.jpg")
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
                            var path = "Greenhouse/$userId/G$greenhouseID/$imageName"
                            if(task.isSuccessful){
                                val databaseRef = FirebaseDatabase.getInstance().reference

                              /*  databaseRef.addListenerForSingleValueEvent( object:ValueEventListener{

                                    override fun onCancelled(p0: DatabaseError) {}

                                    override fun onDataChange(p0: DataSnapshot) {
                                        //var i = listOf(p0.value).size
                                        checkImageURL.add(p0.value.toString())
                                        for(i in 0 until checkImageURL.size)
                                            Log.d("TEST-data",checkImageURL[i])
                                    }
                                })*/
                                databaseRef.child("$path/originURL").setValue(task.result.toString()) //task.result取得downloadURL
                                databaseRef.child("$path/detectURL").setValue("Null")
                                databaseRef.child("$path/result").setValue("Null")
                                //傳送socket
                                hintText.text = resources.getString(R.string.hint3)
                                PacketAsyncTask().execute(PacketModel(task.result.toString(),path))
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

    class PacketAsyncTask : AsyncTask<PacketModel, Int, Int>() {

        override fun doInBackground(vararg p0: PacketModel?): Int {
            val host = "120.113.173.82"
            val port = "6688"

            try
            {
                //開thread處理client傳送socket
                val clientThread = Thread {
                    val data = PacketModel(p0[0]!!.URL,p0[0]!!.path)
                    val client = ClientSocket(host, port.toInt())
                    client.update(data)
                }
                clientThread.start()
            }
            catch(e: Exception) {
                e.printStackTrace()
            }
            return 0
        }

        //客戶端
        class  ClientSocket (host: String, port: Int ) :Socket(host,port){

            var reader = BufferedReader(InputStreamReader(inputStream))
            var writer = PrintWriter(outputStream, true)
            var status = 0

            fun update(data : PacketModel) {

                val clientRead = ReadHandler(reader)
                clientRead.build()

                val timer = Timer()
                timer.schedule(object : TimerTask(){
                    override fun run() {
                        status = clientRead.getSatus()
                        if(status == 1){
                            timer.cancel()
                            timer.purge()
                            Log.i("[Socket]","Closed")
                            close()
                        }else{
                            val msg = data.URL
                            //向服務器寫入
                            writer.println(msg)
                            Log.i("[Socket]","Connected")
                        }
                    }

                },0,1000)
            }
        }

        //開啟一個線程從服務器讀取數據
        class ReadHandler(var reader: BufferedReader) : Thread() {

            private var status = 0

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
                        status = 1
                        break
                    }
                }
            }

            fun getSatus():Int{
                return status
            }
        }
    }


}