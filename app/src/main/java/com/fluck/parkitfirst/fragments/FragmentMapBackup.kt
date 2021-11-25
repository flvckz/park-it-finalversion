package com.fluck.parkitfirst.fragments

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.system.Os.remove
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import com.fluck.parkitfirst.MapActivity
import com.fluck.parkitfirst.R
import com.fluck.parkitfirst.entities.LocationInfo
import com.fluck.parkitfirst.entities.MarkersRetrieve
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.lang.Exception
import com.google.android.gms.location.LocationCallback

import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import io.grpc.InternalChannelz.id

class FragmentMapBackup : Fragment(), OnMapReadyCallback {

    lateinit var v: View
    private lateinit var mMap: GoogleMap //mapa de google
    private lateinit var fusedLocationClient: FusedLocationProviderClient //ubicacion
    private val db = FirebaseFirestore.getInstance() //firebase
    private lateinit var shareLocationBtn: Button //boton de VACATE
    private lateinit var delLocationBtn: Button //boton de REFRESH
    lateinit var storageReference: StorageReference //guardado de firestore (linea 69)
    private val loc: MutableList<LocationInfo> = arrayListOf() //LISTA DE LA CLASS PARA LA LAT LONG
    private val map: MutableMap<String, Any> = HashMap() //lista para el mapa
    private lateinit var lastLocation: Location //last location
    private var userId = FirebaseAuth.getInstance().currentUser?.uid //user id
    private var lastMarker: Marker? = null //marker
    private val SPLASH_TIME_OUT : Long = 20 //tiempo para el refresh

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_map_backup, container, false)
        setupLocClient()
        shareLocationBtn = v.findViewById(R.id.btn_find_location)
        delLocationBtn = v.findViewById(R.id.btn_del_marker)
        storageReference = FirebaseStorage.getInstance().getReference("Markers")

        val actionBar = (activity as MapActivity).supportActionBar

        actionBar!!.setDisplayShowTitleEnabled(false)
        actionBar!!.setTitle("")

        return v
    }

    // use it to request location updates and get the latest location


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapBU) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        // Construct a FusedLocationProviderClient.
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap //declara al mapa como mMap
        mMap.clear() //borra todos los markers (en pantalla, no database)
        //mMap.uiSettings.isZoomControlsEnabled = true

        db.collection("Markers")
            .addSnapshotListener{ result: QuerySnapshot?, e: FirebaseFirestoreException? ->
                if (e != null){
                    Log.w(TAG, "Listen failed. ", e)
                    return@addSnapshotListener
                }
                loc.clear() //limpia la mutable list de locationinfo
                loc.addAll(result!!.toObjects(LocationInfo::class.java)) //añade el resultado de la snapshot (si escucho un cambio) al objeto
                for (LocationInfo in loc){

                    //mMap.clear()

                    val geoPosition = LatLng(LocationInfo.latitude, LocationInfo.longitude) //CREADO X NOSOTROS, CONSTANTE QUE GUARDA LAT LONG

                    //if (lastMarker != null)
                    //    lastMarker!!.remove()

                    /*lastMarker = */mMap.addMarker(MarkerOptions().position(geoPosition).title(getDirection(geoPosition)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)))
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(geoPosition)) //geoPosition nuestra posicion
                }

            }
        setUpMap() //funcion de mas abajo (setea las cosas, listeners del boton, etc) LINEA 199 FUNCION COMPLETA
        //FINAL APP 24 NOV
    }

    private fun getDirection(latLng: LatLng) : String { //obtiene calle y numero (address y adressline)
        val geocoder = Geocoder(context) //geocoder prehecha para coordenadas
        val directions: List<Address>? //guarda la lista de direcciones
        val firstDirection: Address //constante para la primera direccion de tipo address (direccion)
        var addressTxt = "" //variable donde se muestra

        try {
            directions = geocoder.getFromLocation(
                latLng.latitude, latLng.longitude, 1
            )
            if (directions != null && directions.isNotEmpty()) {
                firstDirection = directions[0] //guarda la direccion unica

                //direccion con muchas lineas
                if (firstDirection.maxAddressLineIndex > 0) {
                    for (i in 0..firstDirection.maxAddressLineIndex) {
                        addressTxt += firstDirection.getAddressLine(i) + "\n"
                    }
                }
                //Principal y secundario
                else {
                    addressTxt += firstDirection.thoroughfare + ", " + firstDirection.subThoroughfare + "\n"
                }
            }
        } catch (e : Exception) {
            addressTxt = "No address found."
        }
        return addressTxt
    }

    private fun setupLocClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    // prompt the user to grant/deny access

    companion object {
        private const val REQUEST_LOCATION = 1 //request code to identify specific permission request
        private const val TAG = "FragmentMapBackup" // for debugging
    }

    private fun uploadData(data: Map<String, Any>) {
        //userId?.let { //por si se necesita hacer el marker con user id propio (que se sobreescriban) // siempre se sobreescriben xq el id es siemper la uId del user
            db.collection("Markers").document().set(data) //document vacio crea uno nuevo
                .addOnSuccessListener {
                    Toast.makeText(context, "Ubication added to map", Toast.LENGTH_SHORT).show()
                    shareLocationBtn?.isEnabled = true
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Issue on adding ubication", Toast.LENGTH_SHORT).show()
                    shareLocationBtn?.isEnabled = true

                }
        //}
    }

    @SuppressLint("MissingPermission") //no checkea permisos
    private fun getLastLocation() { //obtener nuestra ultima ubi para poner el marker siempre donde estemos
        val location = fusedLocationClient.lastLocation
        location.addOnSuccessListener { location: Location? ->
            if(location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude) //pasa ubi a coordenadas

                map["latitude"] = location.latitude
                map["longitude"] = location.longitude
                uploadData(map)

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f))
            }
        }
    }



    private fun setUpMap() { //200 a 204 checkea permisos
        if (ActivityCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION)
            return
        }
        mMap.isMyLocationEnabled = true //ubi puesta
        mMap.uiSettings.isZoomControlsEnabled = true //zoom puestp
        mMap.uiSettings.isCompassEnabled = true //brujula puesta

        mMap.setOnInfoWindowClickListener { marker: Marker -> // WINDOW CLICK LISTENER -> VENTANA QUE SALE CUANDO APRETAS MARKER (EN NUESTRO CASO LA DE LA ADDRESS "Cachimayo 471")

                    db.collection("Markers")
                        .whereEqualTo("latitude", marker.position.latitude) //whereEqualTo busca en FB // marker.position busca en el marker localmente
                        .whereEqualTo("longitude", marker.position.longitude)
                        .get()
                        .addOnSuccessListener { documents -> //refiere a la firebase
                            for (document in documents) {
                                db.collection("Markers").document(document.id)
                                    .delete()
                                marker.remove() //borrar marker de pantalla
                                lastMarker?.remove() //""
                                mMap.clear() //""
                            }
                        }
                }

        shareLocationBtn.setOnClickListener{ //boton vacate
            getLastLocation() //metodo linea 180
        }

        delLocationBtn.setOnClickListener {
            lastMarker?.remove()

            Handler().postDelayed(

                {
                    val action = FragmentMapBackupDirections.actionFragmentMapBackupToFragmentWait()
                    v.findNavController().navigate(action)
                }, SPLASH_TIME_OUT)
        }
    }
}