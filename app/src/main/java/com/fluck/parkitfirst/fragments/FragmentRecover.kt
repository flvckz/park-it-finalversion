package com.fluck.parkitfirst.fragments

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_recover.*

class FragmentRecover : Fragment() {

    lateinit var v: View

    lateinit var txtRecover: TextView
    lateinit var txtOldPassword: EditText
    lateinit var txtNewPassword: EditText
    lateinit var txtPasswordConfirm: EditText
    lateinit var btnRecover: Button
    lateinit var txtSupport: TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_recover, container, false)
        txtRecover = v.findViewById(R.id.txtRecoverAcc)
        txtOldPassword = v.findViewById(R.id.txtOldPassword)
        txtNewPassword = v.findViewById(R.id.txtNewPassword)
        txtPasswordConfirm = v.findViewById(R.id.txtPasswordConfirm)
        btnRecover = v.findViewById(R.id.btnRecover)
        txtSupport = v.findViewById(R.id.txtSupport)
        auth = FirebaseAuth.getInstance()
        return v
    }

    override fun onStart() {
        super.onStart()

        updateUserPassword()
    }

    private fun updateUserPassword() {

        btnRecover.setOnClickListener {
            if (txtOldPassword.text.isNotEmpty() && txtNewPassword.text.isNotEmpty() && txtPasswordConfirm.text.isNotEmpty()) {

                if(txtNewPassword.text.toString().equals(txtPasswordConfirm.text.toString())) {

                    val user = auth.currentUser
                    if(user != null && user.email != null) {

                        val credential = EmailAuthProvider
                            .getCredential(user.email!!, txtOldPassword.text.toString())

// Prompt the user to re-provide their sign-in credentials
                        user.reauthenticate(credential)
                            .addOnCompleteListener {
                                if(it.isSuccessful) {
                                    Snackbar.make(v, "Re-Authentication success.", Snackbar.LENGTH_SHORT).show()

                                    user!!.updatePassword(txtNewPassword.text.toString())
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Snackbar.make(v, "Password changed.", Snackbar.LENGTH_SHORT).show()
                                                auth.signOut()
                                                val action1 = FragmentRecoverDirections.actionFragmentRecoverToFragmentAuth()
                                                v.findNavController().navigate(action1)
                                            }
                                        }
                                }
                                else {
                                    Snackbar.make(v, "Re-Authentication failed.", Snackbar.LENGTH_SHORT).show()
                                }
                            }
                    }
                    else {
                        val action = FragmentRecoverDirections.actionFragmentRecoverToFragmentAuth()
                        v.findNavController().navigate(action)
                    }

                }
                else {
                    Snackbar.make(v, "Password mistmatch.", Snackbar.LENGTH_SHORT).show()
                }

            } else {
                Snackbar.make(v, "Please enter all the fields.", Snackbar.LENGTH_SHORT).show()
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
}