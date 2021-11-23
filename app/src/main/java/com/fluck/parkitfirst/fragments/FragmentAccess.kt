package com.fluck.parkitfirst.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.findNavController
import com.fluck.parkitfirst.R
import com.fluck.parkitfirst.entities.UserLog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FragmentAccess : Fragment() {

    lateinit var v : View

    lateinit var txtSignUp : TextView
    lateinit var edtEmail : EditText
    lateinit var edtPassword : EditText
    lateinit var btnSignUp : Button
    lateinit var txtGoToLogIn: TextView
    lateinit var spnVehicles: Spinner

    val vehicleList = listOf("Sedan", "SUV", "Hatchback", "Compact", "Pikcup")
    lateinit var mainAdapter: ArrayAdapter<String>

    lateinit var selectedVehicle : String
    private val db = FirebaseFirestore.getInstance()
    private var userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_access, container, false)
        txtSignUp = v.findViewById(R.id.txtSignUp)
        edtEmail = v.findViewById(R.id.edtEmail)
        edtPassword = v.findViewById(R.id.edtPassword)
        btnSignUp = v.findViewById(R.id.btnSignUp)
        txtGoToLogIn = v.findViewById(R.id.txtGoToLogIn)
        spnVehicles = v.findViewById(R.id.spnVehicles)
        setHasOptionsMenu(true)
        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        var adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, vehicleList)
        mainAdapter = adapter
    }

    override fun onStart() {
        super.onStart()

        setSpinner()
        signUp()
        goToLogIn()
    }

    private fun setSpinner() {
        spnVehicles.adapter = mainAdapter
        spnVehicles.onItemSelectedListener = object:
            AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedVehicle = vehicleList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                showDialog()
            }
        }
    }

    private fun signUp(){
        btnSignUp.setOnClickListener {
            val spnVehic = selectedVehicle
            if (edtEmail.text.toString() != "" && edtPassword.text.toString() != "" && spnVehic.isNotBlank()){
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(edtEmail.text.toString(),
                        edtPassword.text.toString()).addOnCompleteListener{
                        val newDocUserLog = db.collection("UserLog").document()
                        val itemUserLog = UserLog(newDocUserLog.id, edtEmail.text.toString(), edtPassword.text.toString(), spnVehic)
                        db.collection("UserLog").document(newDocUserLog.id).set(itemUserLog)
                        db.collection("UserLog").document(newDocUserLog.id).set(hashMapOf("usuario" to userId), SetOptions.merge())
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