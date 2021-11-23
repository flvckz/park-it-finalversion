package com.fluck.parkitfirst.fragments

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.fluck.parkitfirst.R

class FragmentWait : Fragment() {

    private lateinit var v: View
    private val SPLASH_TIME_OUT : Long = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_wait, container, false)

        Handler().postDelayed(

            {
                val action = FragmentWaitDirections.actionFragmentWaitToFragmentMapBackup()
                v.findNavController().navigate(action)
            }, SPLASH_TIME_OUT)

        return v
    }

}