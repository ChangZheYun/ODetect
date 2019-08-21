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
import android.net.Uri
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
import java.io.File
import java.text.DateFormat
import java.text.DateFormat.getDateInstance
import java.text.SimpleDateFormat
import java.util.*

class UpImageFragment : Fragment() {

    private lateinit var openAlbum : Button
    private lateinit var progressBar : ContentLoadingProgressBar
    private lateinit var storageFirebase :FirebaseStorage

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
        progressBar = activity!!.findViewById(R.id.uploadImageProgress)

        storageFirebase = FirebaseStorage.getInstance()

        openAlbum.setOnClickListener{
            onGallery()
        }

        insertImageButton.setOnClickListener{
            onGallery()
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
                        val userUploadImageRef = storageFirebase.reference.child("$userId/$imageName.jpg")
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
                                var path = "User/$userId/checkImageURL/$imageName/URL"
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
                                databaseRef.child(path).setValue(task.result.toString()) //task.result取得downloadURL
                                path = "User/$userId/checkImageURL/$imageName/result"
                                databaseRef.child(path).setValue("Health")
                                Toast.makeText(activity,"照片上傳成功,URL=${task.result}",Toast.LENGTH_SHORT).show()
                            }

                        }
                        //取得上傳進度
                        userUploadImage.addOnProgressListener { task ->
                            val progress = (100.0 * task.bytesTransferred) / task.totalByteCount
                            progressBar.visibility = View.VISIBLE
                            progressBar.progress = progress.toInt()
                           // Log.d("UPLOAD--","Upload is $progress% done")
                            if(progress>=100){
                                //顯示圖片
                                progressBar.visibility = View.INVISIBLE
                                imageView.visibility = View.VISIBLE
                                imageView.setImageURI(url)
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


}