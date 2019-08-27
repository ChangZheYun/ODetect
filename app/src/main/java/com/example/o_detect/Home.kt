package com.example.o_detect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout

class Home : Fragment(){

    private lateinit var contentViewPager : ViewPager
    private lateinit var contentTabLayout : TabLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view : View = inflater.inflate(R.layout.home,container,false)
        return view
    }


    class ContentAdapter constructor(fm: FragmentManager): FragmentPagerAdapter(fm) {

        private var fragmentList : MutableList<Fragment> = ArrayList()

        init{
            fragmentList.add(UpImageFragment())
            fragmentList.add(FarmFragment())
            fragmentList.add(DataListFragment())
        }

        override fun getCount(): Int {
            return 3
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when(position){
                0 -> "上傳圖片"
                1 -> "農場概況"
                else -> "個別管理"
            }
        }
    }

    private fun setViewPagerAndTabLayout(){
        //fragment內使用fragment務必使用childFragmentManger
        val fragmentAdapter = ContentAdapter(childFragmentManager)

        contentViewPager.adapter = fragmentAdapter
        contentTabLayout.setupWithViewPager(contentViewPager)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //設置toolBar

        //設置viewPager+tabLayout
        contentViewPager = activity!!.findViewById(R.id.contentPager)
        contentTabLayout = activity!!.findViewById(R.id.contentTabs)

       /* contentTabLayout.bringToFront()
        contentViewPager.bringToFront()*/

        contentTabLayout.addTab(contentTabLayout.newTab().setText("上傳圖片"))
        contentTabLayout.addTab(contentTabLayout.newTab().setText("農場概況"))
        contentTabLayout.addTab(contentTabLayout.newTab().setText("個別管理"))
        setViewPagerAndTabLayout()
        contentViewPager.currentItem = 0

        /* contentTabLayout.addOnTabSelectedListener( object:TabLayout.OnTabSelectedListener{
             override fun onTabSelected(tab: TabLayout.Tab) {
                 insertImageButton.hide()
                 insertDataButton.show()
             }

             override fun onTabReselected(p0: TabLayout.Tab?) {}
             override fun onTabUnselected(p0: TabLayout.Tab?) {}
         })*/

    }


}