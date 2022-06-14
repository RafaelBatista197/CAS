package com.example.android.navigation

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.testposedetection.R
import com.example.testposedetection.kotlin.CameraXLivePreviewActivity

class ExercicesFragment : Fragment() {


    val POSE = "pose"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {



        val view: View = inflater.inflate(R.layout.fragment_exercices, container, false)

        setHasOptionsMenu(true)

        clickButtonArmsLateral(view)
        clickButtonNeck(view)
        clickButtonArmsRaise(view)


        // Inflate the layout for this fragment
       return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.options_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
            return NavigationUI.onNavDestinationSelected(item, requireView().findNavController())
                    || super.onOptionsItemSelected(item)


    }

    fun clickButtonArmsLateral(view: View) {
        val buttonLateral: Button = view.findViewById<Button>(R.id.buttonArmLateral)
        buttonLateral.setOnClickListener { view : View ->

            val intent = Intent(context, CameraXLivePreviewActivity::class.java)
            intent.putExtra(POSE, "arms_lateral_up")
            startActivity(intent)
        }
    }

    fun clickButtonNeck(view: View) {
        val buttonNeck: Button = view.findViewById<Button>(R.id.buttonNeckRotation)
        buttonNeck.setOnClickListener { view : View ->
            val intent = Intent(context, CameraXLivePreviewActivity::class.java)
            intent.putExtra(POSE, "neck_left")
            startActivity(intent)
        }
    }

    fun clickButtonArmsRaise(view: View) {
        val buttonArmsRaise: Button = view.findViewById<Button>(R.id.buttonArmsRaise)
        buttonArmsRaise.setOnClickListener { view : View ->
            val intent = Intent(context, CameraXLivePreviewActivity::class.java)
            intent.putExtra(POSE, "arms_raise_up")
            startActivity(intent)
        }
    }



}
