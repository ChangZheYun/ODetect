package com.example.o_detect

import android.annotation.TargetApi
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.o_detect.ui.login.LoginActivity
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.content_display.*
import kotlinx.android.synthetic.main.title_datalist.*
import kotlinx.android.synthetic.main.title_uploadimage.*
import java.lang.Float.max

open class Content : AppCompatActivity(){

 //   private lateinit var contentViewPager : ViewPager
 //   private lateinit var contentTabLayout : TabLayout
    private lateinit var contentToolbar : androidx.appcompat.widget.Toolbar
    private var revealX: Int = 0
    private var revealY: Int = 0

    companion object {
        const val ARG_CIRCULAR_REVEAL_X = "ARG_CIRCULAR_REVEAL_X"
        const val ARG_CIRCULAR_REVEAL_Y = "ARG_CIRCULAR_REVEAL_Y"
    }

 /*   class ContentAdapter constructor(fm: FragmentManager): FragmentPagerAdapter(fm) {

        private var fragmentList : MutableList<Fragment> = ArrayList()

        init{
            fragmentList.add(UpImageFragment())
            fragmentList.add(DataListFragment())
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when(position){
                0 -> "上傳圖片"
                else -> "資料查看"
            }
        }
    }

    private fun setViewPagerAndTabLayout(){
        val fragmentAdapter = ContentAdapter(supportFragmentManager)

        contentViewPager.adapter = fragmentAdapter
        contentTabLayout.setupWithViewPager(contentViewPager)
    }
*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_display)

        //設置toolBar
        contentToolbar = findViewById(R.id.contentToolBar)
        setSupportActionBar(contentToolbar)

        revealX = intent.getIntExtra(ARG_CIRCULAR_REVEAL_Y, 0)
        revealY = intent.getIntExtra(ARG_CIRCULAR_REVEAL_Y, 0)



        contentDisplay.visibility = View.INVISIBLE

        val viewTreeObserver = contentDisplay.viewTreeObserver
        if (viewTreeObserver.isAlive) {
         viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
             override fun onGlobalLayout() {
                 //當在一個ViewTree 中全局佈局發生改變或某個View的可視狀態發生改變時，呼叫的callback
                 //在這裡開始Reveal animation
                 startReveal(revealX, revealY)
                 contentDisplay.viewTreeObserver.removeOnGlobalLayoutListener(this)
             }
         })
        }

        supportFragmentManager.beginTransaction().replace(R.id.contentView,Home()).commit()

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun startReveal(centerX: Int, centerY: Int) {
        //動畫的開始半徑
        val startRadius = 0.0f

        val metric = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metric)
        val width = metric.widthPixels     // 螢幕寬度（畫素）
        val height = metric.heightPixels   // 螢幕高度（畫素）

        //動畫的結束半徑
        val endRadius = kotlin.math.max(width,height).toFloat()

        val circularReveal = ViewAnimationUtils.createCircularReveal(contentDisplay, centerX, centerY, startRadius, endRadius)
        circularReveal.duration = 800
        circularReveal.interpolator = AccelerateInterpolator()

        contentDisplay.visibility = View.VISIBLE
        circularReveal.start()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }


    //menu點擊事件
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item!!.itemId){
            R.id.action_home ->{
                supportFragmentManager.beginTransaction().replace(R.id.contentView,Home()).commit()
                contentToolbar.title = "O-Detect"
                true
            }
            R.id.action_profile ->{
                supportFragmentManager.beginTransaction().replace(R.id.contentView,Profile()).commit()
                contentToolbar.title = "個人資料"
                //-----待改(點擊時更換圖片)------
                //item.icon.colorFilter = PorterDuffColorFilter(Color.BLACK,PorterDuff.Mode.MULTIPLY)
                true
            }
            else ->{
                super.onOptionsItemSelected(item)
            }
        }
    }

}