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
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class FragmentAccess : Fragment() {

    lateinit var v : View

    lateinit var txtSignUp : TextView
    lateinit var txtEmail : EditText
    lateinit var txtPassword : EditText
    lateinit var btnSignUp : Button
    lateinit var txtGoToLogIn: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_access, container, false)
        txtSignUp = v.findViewById(R.id.txtSignUp)
        txtEmail = v.findViewById(R.id.txtEmail)
        txtPassword = v.findViewById(R.id.txtPassword)
        btnSignUp = v.findViewById(R.id.btnSignUp)
        txtGoToLogIn = v.findViewById(R.id.txtGoToLogIn)
        return v
    }

    override fun onStart() {
        super.onStart()

        signUp()
        goToLogIn()
    }

    private fun signUp(){
        btnSignUp.setOnClickListener {
            if (txtEmail.text.toString() != "" && txtPassword.text.toString() != ""){
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(txtEmail.text.toString(),
                        txtPassword.text.toString()).addOnCompleteListener{
                        if(it.isSuccessful){
                            Snackbar.make(v, "User created.", Snackbar.LENGTH_SHORT).show()
                            val action = FragmentAccessDirections.actionFragmentAccessToFragmentAuth()
                            v.findNavController().navigate(action)
                        }
                        else{
                            showDialog()
                        }

                    }
            }
        }
    }

    private fun showDialog(){
        val dialog = AlertDialog.Builder(context)
            .setTitle("Error")
            .setMessage("Intentar nuevamente.")
            .setCancelable(true)
            .create()
        dialog.show()
    }

    private fun goToLogIn() {
        txtGoToLogIn.setOnClickListener {
            val action2 = FragmentAccessDirections.actionFragmentAccessToFragmentAuth()
            v.findNavController().navigate(action2)
        }
    }
}