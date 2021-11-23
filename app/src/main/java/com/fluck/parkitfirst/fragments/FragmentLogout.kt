package com.fluck.parkitfirst.fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.fluck.parkitfirst.R
import com.google.firebase.auth.FirebaseAuth


class FragmentLogout : Fragment() {



    lateinit var v : View

    private val SPLASH_TIME_OUT : Long = 3000

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_logout, container, false)

        onResume()

        Handler().postDelayed(

            {
                auth.signOut()

                val action = FragmentLogoutDirections.actionFragmentLogoutToAuthActivity()
                v.findNavController().navigate(action)
            }, SPLASH_TIME_OUT)

        return v
    }

    override fun onResume() {
        super.onResume()
        val supportActionBar: ActionBar? = (requireActivity() as AppCompatActivity).supportActionBar
        if (supportActionBar != null) supportActionBar.hide()
    }
}