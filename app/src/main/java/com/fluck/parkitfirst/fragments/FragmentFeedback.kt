package com.fluck.parkitfirst.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.fluck.parkitfirst.R
import com.fluck.parkitfirst.databinding.FragmentFeedbackBinding
import androidx.appcompat.app.AppCompatActivity




class FragmentFeedback : Fragment() {

    private lateinit var binding: FragmentFeedbackBinding
    val Fragment.packageManager get() = activity?.packageManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFeedbackBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        binding.btnSendMail.setOnClickListener {

            val email = binding.edtMailto.text.toString()
            val subject = binding.edtSubject.text.toString()
            val message = binding.edtSendMessage.text.toString()

            val addresses = email.split(",".toRegex()).toTypedArray()

            val intent = Intent(Intent.ACTION_SENDTO).apply {

                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, addresses)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, message)

            }

            if (packageManager?.let { it1 -> intent.resolveActivity(it1) } != null) {

                startActivity(intent)

            } else {
                Toast.makeText(context, "Required app is not installed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}