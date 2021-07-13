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

class FragmentAuth : Fragment() {

    lateinit var v : View

    lateinit var txtLogIn : TextView
    lateinit var txtEmail : EditText
    lateinit var txtPassword : EditText
    lateinit var btnSignIn : Button
    lateinit var txtGoToSignUp: TextView
    lateinit var txtRecover: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_auth, container, false)
        txtLogIn = v.findViewById(R.id.txtLogIn)
        txtEmail = v.findViewById(R.id.txtEmail)
        txtPassword = v.findViewById(R.id.txtPassword)
        btnSignIn = v.findViewById(R.id.btnSignIn)
        txtGoToSignUp = v.findViewById(R.id.txtGoToSignUp)
        txtRecover = v.findViewById(R.id.txtRecover)
        return v
    }

    override fun onStart() {
        super.onStart()

        signIn()
        goToSignUp()
        goToRecover()
    }

    private fun signIn() {
        btnSignIn.setOnClickListener {
            if (txtEmail.text.toString() != "" && txtPassword.text.toString() != ""){
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(txtEmail.text.toString(),
                        txtPassword.text.toString()).addOnCompleteListener{
                        if(it.isSuccessful){
                            val action = FragmentAuthDirections.actionFragmentAuthToMapActivity()
                            v.findNavController().navigate(action)
                        }
                        else{
                            showDialog()
                        }
                    }
            }
        }
    }

    private fun showDialog() {
        val dialog = AlertDialog.Builder(context)
            .setTitle("Error")
            .setMessage("Intentar nuevamente.")
            .setCancelable(true)
            .create()
        dialog.show()
    }

    private fun goToSignUp() {
        txtGoToSignUp.setOnClickListener {
            val action2 = FragmentAuthDirections.actionFragmentAuthToFragmentAccess()
            v.findNavController().navigate(action2)
        }
    }

    private fun goToRecover() {
        txtRecover.setOnClickListener {
            val action3 = FragmentAuthDirections.actionFragmentAuthToFragmentRecover()
            v.findNavController().navigate(action3)
        }
    }
}