package com.example.android.navigation.onboarding.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.testposedetection.MainActivity
import com.example.testposedetection.R
import com.example.testposedetection.databinding.FragmentThirdScreenBinding


class ThirdScreen : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_third_screen, container, false)

        val binding = DataBindingUtil.inflate<FragmentThirdScreenBinding>(inflater, R.layout.fragment_third_screen,container, false)

        binding.next3.setOnClickListener {
            findNavController().navigate(R.id.action_viewPagerFragment_to_titleFragment)
            //val intent = Intent(context, MainActivity::class.java)
            //startActivity(intent)
            onBoardingFinished()
        }


        return binding.root
    }

    private fun onBoardingFinished(){
        val sharedPref = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("Finished", true)
        editor.apply()
    }
}