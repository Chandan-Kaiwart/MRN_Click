package com.apc.smartinstallation.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.apc.smartinstallation.R
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.apc.smartinstallation.api.MeterReadingRequest
import com.apc.smartinstallation.dataClasses.MRNReadingRequest
import com.apc.smartinstallation.dataClasses.retrofit.RetrofitClient
import com.apc.smartinstallation.databinding.FragMrnBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import com.apc.smartinstallation.vm.MainViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class MRNFragment : Fragment() {
    private lateinit var binding: FragMrnBinding
    private lateinit var mContext: Context
    private var imagePath: String = ""
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: String = "0.0"
    private var longitude: String = "0.0"

    private val vm: MainViewModel by activityViewModels()

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            bitmap?.let {
                binding.imgMeter.setImageBitmap(it)
                imagePath = saveBitmapToFile(it)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragMrnBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getCurrentLocation()

        binding.imgMeter.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    mContext,
                    android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openCamera()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(android.Manifest.permission.CAMERA),
                    1001
                )
            }
        }

        binding.btnSubmit.setOnClickListener {
            if (validateInputs()) {
                submitData()
            }
            findNavController().navigate(R.id.action_mrnFrag_to_signatureFragment)
        }
    }

    private fun validateInputs(): Boolean {
        return when {
            binding.etNewMeterNo.text.isBlank() -> {
                Toast.makeText(mContext, "Please enter meter number", Toast.LENGTH_SHORT).show()
                false
            }
            binding.etNewMeterReading.text.isBlank() -> {
                Toast.makeText(mContext, "Please enter meter reading", Toast.LENGTH_SHORT).show()
                false
            }
            binding.etConsumerName.text.isBlank() -> {
                Toast.makeText(mContext, "Please enter consumer name", Toast.LENGTH_SHORT).show()
                false
            }
            binding.etConsumerNo.text.isBlank() -> {
                Toast.makeText(mContext, "Please enter consumer number", Toast.LENGTH_SHORT).show()
                false
            }
            imagePath.isEmpty() -> {
                Toast.makeText(mContext, "Please capture meter image", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun submitData() {
        if (!validateInputs()) return

        try {
            val imageFile = File(imagePath)
            val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

            // Create request bodies
            val reading = binding.etNewMeterReading.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val consumerName = binding.etConsumerName.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val consumerNo = binding.etConsumerNo.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val consumerMobile = binding.etConsumerMobile.text?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val boxSeal = binding.etBoxSeal.text?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val bodySeal = binding.etBodySeal.text?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val terminalSeal = binding.etTerminalSeal.text?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val oldMeterNo = binding.etOldMeterNo.text?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val oldMeterReading = binding.etOldMeterReading.text?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val oldMeterMake = binding.etOldMeterMake.text?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val oldPhase = binding.etOldPhase.text?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val meterMake = binding.etNewMeterMake.text?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val phase = binding.etNewPhase.text?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val lat = latitude.toRequestBody("text/plain".toMediaTypeOrNull())
            val lng = longitude.toRequestBody("text/plain".toMediaTypeOrNull())
            val accountId = binding.tvAccountId.text?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val manualReading = binding.etManualReading.text?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

            binding.btnSubmit.isEnabled = false
            binding.btnSubmit.text = "Submitting..."

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.instance.saveMRNReading(
                        reading, consumerName, consumerNo, consumerMobile,
                        boxSeal, bodySeal, terminalSeal, oldMeterNo,
                        oldMeterReading, oldMeterMake, oldPhase, meterMake,
                        phase, lat, lng, accountId, manualReading, imagePart
                    )

                    if (response.isSuccessful) {
                        response.body()?.let { apiResponse ->
                            if (apiResponse.success) {
                                Toast.makeText(mContext, "MRN Data uploaded successfully", Toast.LENGTH_SHORT).show()
                                // Save to ViewModel for next screen
                                val mrnRequest = MRNReadingRequest(
                                    image_name = imageFile.name,
                                    image_path = imagePath,
                                    reading = binding.etNewMeterReading.text.toString(),
                                    consumer_name = binding.etConsumerName.text.toString(),
                                    consumer_no = binding.etConsumerNo.text.toString(),
                                    consumer_mobile_no = binding.etConsumerMobile.text?.toString(),
                                    box_seal = binding.etBoxSeal.text?.toString(),
                                    body_seal = binding.etBodySeal.text?.toString(),
                                    terminal_seal = binding.etTerminalSeal.text?.toString(),
                                    old_meter_no = binding.etOldMeterNo.text?.toString(),
                                    old_meter_reading = binding.etOldMeterReading.text?.toString(),
                                    old_meter_make = binding.etOldMeterMake.text?.toString(),
                                    old_phase = binding.etOldPhase.text?.toString(),
                                    meter_make = binding.etNewMeterMake.text?.toString(),
                                    phase = binding.etNewPhase.text?.toString(),
                                    latitude = latitude,
                                    longitude = longitude,
                                    account_id = binding.tvAccountId.text?.toString(),
                                    manual_reading = binding.etManualReading.text?.toString()
                                )
                                vm.setMrnData(mrnRequest)
                                findNavController().navigate(MRNFragmentDirections.actionMrnFragToSignatureFragment())
                            } else {
                                Toast.makeText(mContext, "Error: ${apiResponse.message}", Toast.LENGTH_LONG).show()
                            }
                        } ?: run {
                            Toast.makeText(mContext, "Empty response from server", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "No error details"
                        Log.e("API_ERROR", "Server error: $errorBody")
                        Toast.makeText(mContext, "Server error: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e("NETWORK_ERROR", "Submit error", e)
                    Toast.makeText(mContext, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    binding.btnSubmit.isEnabled = true
                    binding.btnSubmit.text = "SUBMIT"
                }
            }
        } catch (e: Exception) {
            Toast.makeText(mContext, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                mContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    latitude = it.latitude.toString()
                    longitude = it.longitude.toString()
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1002
            )
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): String {
        val filename = "MRN_${System.currentTimeMillis()}.jpg"
        val file = File(mContext.cacheDir, filename)
        try {
            FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                stream.flush()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file.absolutePath
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1001 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(mContext, "Camera permission required", Toast.LENGTH_SHORT).show()
                }
            }
            1002 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                }
            }
        }
    }
}