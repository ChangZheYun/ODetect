package com.example.o_detect

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.content_display.*
import kotlinx.android.synthetic.main.title_datalist.*
import kotlinx.android.synthetic.main.title_uploadimage.*

class Content : AppCompatActivity(){

 //   private lateinit var contentViewPager : ViewPager
 //   private lateinit var contentTabLayout : TabLayout
    private lateinit var contentToolbar : androidx.appcompat.widget.Toolbar

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

        supportFragmentManager.beginTransaction().replace(R.id.contentView,Home()).commit()

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