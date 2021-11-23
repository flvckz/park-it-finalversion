package com.fluck.parkitfirst.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.navigation.findNavController
import com.fluck.parkitfirst.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_recover.*

class FragmentRecover : Fragment() {

    lateinit var v: View

    lateinit var txtRecover: TextView
    lateinit var edtOldPassword: EditText
    lateinit var btnRecover: Button
    lateinit var txtSupport: TextView
    lateinit var txtGoToLogInForgot: TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_recover, container, false)
        txtRecover = v.findViewById(R.id.txtRecoverAcc)
        edtOldPassword = v.findViewById(R.id.edtEmailForgot)
        btnRecover = v.findViewById(R.id.btnRecover)
        txtSupport = v.findViewById(R.id.txtSupport)
        txtGoToLogInForgot = v.findViewById(R.id.txtGoToLogInForgot)
        auth = FirebaseAuth.getInstance()
        return v
    }

    override fun onStart() {
        super.onStart()

        forgotPassword()
    }

    private fun forgotPassword(){
        btnRecover.setOnClickListener {
            if (edtEmailForgot.text.toString() != ""){
                FirebaseAuth.getInstance().sendPasswordResetEmail(edtEmailForgot.text.toString())
                    .addOnCompleteListener{task ->
                        if(task.isSuccessful){
                            showDialog()
                            goToLogIn()
                        }
                    }
            }
        }

    }

    private fun goToLogIn() {
        txtGoToLogInForgot.setOnClickListener {
            val action = FragmentRecoverDirections.actionFragmentRecoverToFragmentAuth()
            v.findNavController().navigate(action)
        }
    }

    private fun showDialog() {
        val dialog = AlertDialog.Builder(context)
            .setTitle("Cambio de contraseña")
            .setMessage("Se le ha enviado un mail.")
            .setCancelable(true)
            .create()
        dialog.show()
    }
}