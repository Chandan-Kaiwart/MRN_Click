package com.apc.smartinstallation

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.apc.smartinstallation.databinding.ActivityMainBinding
import com.apc.smartinstallation.ui.theme.SmartInstallationTheme
import com.apc.smartinstallation.util.PermissionHelper
import com.apc.smartinstallation.vm.MainViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var perms: Array<String>
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var permissionHelper: PermissionHelper
    private var latitude: String? = ""
    private var longitude: String? = ""
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)  // Forces light mode

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        navController = Navigation.findNavController(binding.root.findViewById(R.id.fragment))
        navController.setGraph(R.navigation.si_nav_graph)

        permissionHelper = PermissionHelper(this)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Setup back press handling
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!navController.popBackStack()) {
                    // If no fragments to pop, minimize the app instead of closing
                    moveTaskToBack(true)
                }
            }
        })

        setupPermissions()
    }

    private fun setupPermissions() {
        perms = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.ACCESS_NETWORK_STATE,
            )
        } else {
            arrayOf(
                android.Manifest.permission.POST_NOTIFICATIONS,
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VIDEO,
                android.Manifest.permission.READ_MEDIA_AUDIO,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.ACCESS_NETWORK_STATE,
            )
        }

        if (!permissionHelper.checkPermissions(perms)) {
            permissionHelper.requestPermissions(perms, 100)
        } else {
            lastLocation
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save navigation state
        navController.saveState()?.let {
            outState.putBundle("nav_state", it)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Restore navigation state
        savedInstanceState.getBundle("nav_state")?.let {
            navController.restoreState(it)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            lastLocation
        }
    }

    @get:SuppressLint("MissingPermission")
    private val lastLocation: Unit
        get() {
            Log.d("LOC>>", "lastLocation: ")
            if (isLocationEnabled) {
                mFusedLocationClient.lastLocation.addOnCompleteListener { task ->
                    val location = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        latitude = location.latitude.toString()
                        longitude = location.longitude.toString()
                        vm.lat.value = latitude
                        vm.long.value = longitude
                        Log.d("LAT>>>", location.latitude.toString())
                        Log.d("LONG>>>", location.longitude.toString())

                        try {
                            val geocoder = Geocoder(this, Locale.getDefault())
                            val addresses = geocoder.getFromLocation(
                                location.latitude,
                                location.longitude,
                                1
                            )
                            addresses?.let {
                                if (it.isNotEmpty()) {
                                    val city = it[0].locality
                                    val state = it[0].adminArea
                                    // vm.address.value = "$city,$state"
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } else {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                val locationRequest = LocationRequest.create().apply {
                    priority = Priority.PRIORITY_HIGH_ACCURACY
                    interval = 10000
                    fastestInterval = 5000
                }
                val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
                val locationSettingsRequest = builder.build()
                val settingsClient: SettingsClient = LocationServices.getSettingsClient(this)
                val task = settingsClient.checkLocationSettings(locationSettingsRequest)

                task.addOnSuccessListener(this,
                    OnSuccessListener<LocationSettingsResponse> {
                        Log.d("LOC>>", "All location settings are satisfied.")
                        lastLocation
                    })

                task.addOnFailureListener(this, OnFailureListener { exception ->
                    if (exception is ResolvableApiException) {
                        try {
                            exception.startResolutionForResult(this, 1001)
                        } catch (sendEx: IntentSender.SendIntentException) {
                            // Ignore the error
                        }
                    }
                })
            }
        }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest().apply {
            priority = Priority.PRIORITY_HIGH_ACCURACY
            interval = 5
            fastestInterval = 0
            numUpdates = 1
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback, Looper.myLooper()
        )
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation = locationResult.lastLocation
            vm.lat.value = mLastLocation?.latitude.toString()
            vm.long.value = mLastLocation?.longitude.toString()
        }
    }

    private val isLocationEnabled: Boolean
        get() {
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }

    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        SmartInstallationTheme {
            Greeting("Android")
        }
    }
}