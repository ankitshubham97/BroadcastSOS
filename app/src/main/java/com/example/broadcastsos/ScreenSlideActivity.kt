package com.example.broadcastsos

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * The number of pages (wizard steps) to show in this demo.
 */

class ScreenSlideActivity : FragmentActivity() {

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_slide)

        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = findViewById(R.id.pager)
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)


        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = ScreenSlidePagerAdapter(this, getListOfPagerContents())
        viewPager.adapter = pagerAdapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            Log.i("TabLayoutMediator", "tab: $tab, position: $position")
        }.attach()
    }
    fun getListOfPagerContents(): List<Array<String>> {
        val ar1 = arrayOf(getString(R.string.intro_title_1), getString(R.string.intro_sub_title_1),"R", "Skip" )
        val ar2 = arrayOf(getString(R.string.intro_title_2), getString(R.string.intro_sub_title_2) ,"G", "Skip")
        val ar3 = arrayOf(getString(R.string.intro_title_3), getString(R.string.intro_sub_title_3) ,"B", "Done")
        return listOf(ar1,ar2,ar3)
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity, val pagerContents: List<Array<String>>) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = pagerContents.size

        override fun createFragment(position: Int): Fragment {
            val fragment = ScreenSlidePageFragment()
            fragment.arguments = Bundle().apply {
                putStringArray("INTRO_STRING_OBJECT", pagerContents[position])
                putInt("DONE_BUTTON_VISIBILITY", if (position == pagerContents.size - 1) View.VISIBLE else View.GONE)
            }

            return fragment
        }
    }
}