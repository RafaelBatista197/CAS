package com.example.android.navigation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.android.navigation.onboarding.screens.FirstScreen
import com.example.android.navigation.onboarding.screens.SecondScreen
import com.example.android.navigation.onboarding.screens.ThirdScreen
import com.example.testposedetection.R

class ViewPagerFragment : Fragment() {

    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_view_pager, container, false)
        viewPager = view.findViewById(R.id.viewPager)

        val fragmentList = arrayListOf<Fragment>(
            FirstScreen(),
            SecondScreen(),
            ThirdScreen()
        )

        val adapter = ViewPagerAdapter(
            fragmentList,
            requireActivity().supportFragmentManager,
            lifecycle
        )

        //view.viewPager.adapter = adapter
        viewPager.adapter = adapter

        return view
    }
}