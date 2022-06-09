package com.example.android.navigation.onboarding.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.testposedetection.R
import com.example.testposedetection.databinding.FragmentSecondScreenBinding


class SecondScreen : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_second_screen, container, false)

        val binding = DataBindingUtil.inflate<FragmentSecondScreenBinding>(inflater, R.layout.fragment_second_screen,container, false)

        val viewPager =  activity?.findViewById<ViewPager2>(R.id.viewPager)

        binding.next2.setOnClickListener {
            viewPager?.currentItem = 2
        }

        return binding.root
    }
}