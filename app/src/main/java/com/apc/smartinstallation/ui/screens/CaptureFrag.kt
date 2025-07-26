package com.apc.smartinstallation.ui.screens

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.apc.smartinstallation.R
import com.apc.smartinstallation.dataClasses.MeterReadingExceptionsRes
import com.apc.smartinstallation.dataClasses.MeterReadingExceptionsResItem
import com.apc.smartinstallation.dataClasses.MeterReadingUnitsRes
import com.apc.smartinstallation.dataClasses.UploadMeterReadingImageRes
import com.apc.smartinstallation.databinding.FragCaptureBinding
import com.apc.smartinstallation.util.ImageUpload.VolleyMultipartRequest
import com.apc.smartinstallation.util.ProgressDia
import com.apc.smartinstallation.vm.MainViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import java.io.File
import java.io.FileOutputStream

class CaptureFrag : Fragment() {
    private lateinit var mContext: Context
    private lateinit var navController: NavController
    private lateinit var binding: FragCaptureBinding
    private lateinit var ocrExps: MeterReadingExceptionsRes
    private lateinit var ocrUnits: MeterReadingUnitsRes
    private lateinit var imageCapture: ImageCapture
    private lateinit var pd: ProgressDia
    private  lateinit var exceptions: List<MeterReadingExceptionsResItem>
    private var isTorchOn = false
    private lateinit var cameraProvider: ProcessCameraProvider
    private var latitude: String? = ""
    private var longitude: String? = ""
    private var location: String? = ""
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    var rcpt: Bitmap? = null
    private val vm: MainViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext=context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        binding=FragCaptureBinding.inflate(inflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController= Navigation.findNavController(view)
        ocrUnits=MeterReadingUnitsRes()
        ocrExps=MeterReadingExceptionsRes()
        binding.drawLL2.setVisibility(View.GONE)
        exceptions= emptyList()
        fetchMeterReadingExceptions(mContext)
        fetchMeterReadingUnits(mContext)
        binding.caNumber.setVisibility(View.GONE)
        val ca=System.currentTimeMillis().toString().substring(7)
        binding.caNumber.setText(ca)
        pd= ProgressDia()
        binding.spinner.setText("Take kWh value")

        binding.btnBack.setOnClickListener {
            navController.navigateUp()
        }
        binding.capBt.setOnClickListener {

            binding.previewView.bitmap?.let { bitmap ->vm.setCapturedImage(bitmap)}
            val capturedBitmap = binding.previewView.bitmap
            val ca = binding.caNumber.text.toString()


            lastLocation
            if(ca.isEmpty())
            {
                Toast.makeText(mContext,"Enter Ca Number !",Toast.LENGTH_LONG).show()
            }
            else {
                try {
                    val geocoder = Geocoder(mContext, Locale.getDefault())
                    val addresses: MutableList<Address>? = geocoder.getFromLocation(
                        latitude!!.toDouble(), longitude!!.toDouble(), 1
                    ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    val address: String =
                        addresses!![0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                    val city: String = addresses[0].getLocality()
                    location=city
                    val state: String = addresses[0].getAdminArea()
                    val country: String = addresses[0].getCountryName()
                    val postalCode: String = addresses[0].getPostalCode()
                    //     Log.d("LOC>>>>addSize", addresses.size.toString())
                    Log.d("LOC>>>>add", address)
                    Log.d("LOC>>>>city", city)
                    Log.d("LOC>>>>state", state)
                    Log.d("LOC>>>>coun", country)
                    Log.d("LOC>>>>postal", postalCode)
                    //  capturePhoto(ca,"1",address)
                    uploadMeterImage(
                        mContext,
                        binding.previewView.bitmap!!
                    )
                }
                catch (err: Exception) {
                    uploadMeterImage(
                        mContext,
                        binding.previewView.bitmap!!
                    )
                    Log.d("LOC>>>>", "1")
                    val s =
                        "https://maps.googleapis.com/maps/api/geocode/json?latlng=$latitude,$longitude&sensor=true&key=AIzaSyCxugGqDXfKCV_iKWMla5D4vaA5BR9dCYQ"
                    val jsonArrayRequest: JsonObjectRequest = object :
                        JsonObjectRequest(
                            Request.Method.GET,
                            s,
                            null,
                            object : Response.Listener<JSONObject?> {
                                override fun onResponse(obj: JSONObject?) {
                                    Log.d("LOC>>>>", "2")

                                    val results = obj?.getJSONArray("results")
                                    val result1 = results?.getJSONObject(0)
                                    val address = result1?.getString("formatted_address")
                                    //
                                    if (address != null) {
                                        //   capturePhoto(ca,"1",address)

                                    }
                                }

                            },
                            object : Response.ErrorListener {
                                override fun onErrorResponse(error: VolleyError?) {
                                    //Log.d("RESP>>>", error.toString())
                                    //    capturePhoto(ca,"1",address)


                                    //  Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_LONG).show();
                                    if (error is NoConnectionError) {
                                    }
                                }
                            }) {}
                    jsonArrayRequest.retryPolicy = DefaultRetryPolicy(
                        DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 48,
                        2,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                    )
                    jsonArrayRequest.setShouldCache(false)
                    Volley.newRequestQueue(mContext).add(jsonArrayRequest)
                }

            }

        }

        //   type = intent.getIntExtra("type", -1)
        //   getAllPermissions();
        binding.pb.setVisibility(View.GONE)
        //   binding.caNumber.setText(vm.conData.value?.CONSUMER_NO.toString())


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)
        cameraProviderFuture = ProcessCameraProvider.getInstance(mContext)
        lastLocation
        cameraProviderFuture.addListener(Runnable {
            cameraProvider = cameraProviderFuture.get()

            //   lastLocation
            cameraProviderFuture.addListener(Runnable {
                cameraProvider = cameraProviderFuture.get()
                bindPreview(cameraProvider)
            }, ContextCompat.getMainExecutor(mContext))
            //   bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(mContext))


        binding.previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE

        binding.subBt.setOnClickListener {

            //  rcpt=previewView.bitmap
            rcpt = cropImage(binding.previewView.bitmap!!, requireActivity().window.decorView.rootView, binding.drawLL)
            val ca = binding.caNumber.text.toString()
            //   lastLocation

            if (rcpt == null) {
                Toast.makeText(mContext, "Select image !", Toast.LENGTH_LONG).show()
            } else if (ca.isEmpty()) {
                Toast.makeText(mContext, "Enter  !", Toast.LENGTH_LONG).show()
            } else if (latitude.isNullOrEmpty()) {
                Toast.makeText(
                    mContext, "Couldn't get location,Kindly Retry !", Toast.LENGTH_LONG
                ).show()

            } else if (longitude.isNullOrEmpty()) {
                Toast.makeText(
                    mContext, "Couldn't get location,Kindly Retry !", Toast.LENGTH_LONG
                ).show()
            } else {
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
                val formatted = current.format(formatter)
                try {
                    val geocoder = Geocoder(mContext, Locale.getDefault())
                    val addresses: MutableList<Address>? = geocoder.getFromLocation(
                        latitude!!.toDouble(), longitude!!.toDouble(), 1
                    ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5


                    val address: String =
                        addresses!![0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                    val city: String = addresses[0].getLocality()
                    val state: String = addresses[0].getAdminArea()
                    val country: String = addresses[0].getCountryName()
                    val postalCode: String = addresses[0].getPostalCode()

                } catch (err: Exception) {
                    Log.d("LOC>>>>", "1")
                    val s =
                        "https://maps.googleapis.com/maps/api/geocode/json?latlng=$latitude,$longitude&sensor=true&key=AIzaSyCxugGqDXfKCV_iKWMla5D4vaA5BR9dCYQ"
                    val jsonArrayRequest: JsonObjectRequest = object :
                        JsonObjectRequest(Request.Method.GET,
                            s,
                            null,
                            object : Response.Listener<JSONObject?> {
                                override fun onResponse(obj: JSONObject?) {
                                    Log.d("LOC>>>>", "2")

                                    val results = obj?.getJSONArray("results")
                                    val result1 = results?.getJSONObject(0)
                                    val address = result1?.getString("formatted_address")
                                    //
                                    if (address != null) {
                                        //    rcpt = saveImage(rcpt!!, formatted , address)



                                    }
                                }

                            },
                            object : Response.ErrorListener {
                                override fun onErrorResponse(error: VolleyError?) {
                                    try {
                                        val responseBody = String(error?.networkResponse?.data ?: ByteArray(0), Charsets.UTF_8)
                                        val jsonObject = JSONObject(responseBody)
                                        jsonObject.optString("message", "Unknown error")
                                        Log.d("RES>>",responseBody)
                                    } catch (e: Exception) {
                                        "Error parsing error response: ${e.message}"
                                    }
                                }
                            }) {}
                    jsonArrayRequest.retryPolicy = DefaultRetryPolicy(
                        DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 48,
                        2,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                    )
                    jsonArrayRequest.setShouldCache(false)
                    Volley.newRequestQueue(mContext).add(jsonArrayRequest)
                }
                //    val out = ByteArrayOutputStream()
                //  rcpt!!.compress(Bitmap.CompressFormat.JPEG, 10, out);


            }


        }
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        //
        val preview1: Preview = Preview.Builder().build()
        imageCapture =
            ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setFlashMode(ImageCapture.FLASH_MODE_OFF).build()



        val cameraSelector1: CameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        // val cameraControl = getCameraControl(lensFacing)
        preview1.setSurfaceProvider(binding.previewView.surfaceProvider)

        val camera =
            cameraProvider.bindToLifecycle(
                this, cameraSelector1, preview1, imageCapture
            )

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                camera.cameraControl.setLinearZoom(progress / 100.toFloat())

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        binding.seekBar.afterMeasured {
            val autoFocusPoint = SurfaceOrientedMeteringPointFactory(1f, 1f).createPoint(.5f, .5f)
            try {

                val autoFocusAction = FocusMeteringAction.Builder(
                    autoFocusPoint, FocusMeteringAction.FLAG_AF
                ).apply {
                    //start auto-focusing after 2 seconds
                    Log.d("FOCUS>>>>", "bindPreview: ")
                    setAutoCancelDuration(5, TimeUnit.SECONDS)
                }.build()
                camera.cameraControl.startFocusAndMetering(autoFocusAction)
            } catch (e: CameraInfoUnavailableException) {
                Log.d("ERROR", "cannot access camera", e)
            }
        }


        binding.torchFab.setOnClickListener(View.OnClickListener {
            if (camera.cameraInfo.hasFlashUnit()) {
                if (!isTorchOn) {
                    camera.cameraControl.enableTorch(true)
                    isTorchOn = true
                    binding. torchFab.setImageResource(R.drawable.ic_baseline_flashlight_off_24)
                } else {
                    camera.cameraControl.enableTorch(false)
                    isTorchOn = false
                    binding.torchFab.setImageResource(R.drawable.ic_baseline_flashlight_on_24)

                }

            }
        })
    }


    //Location

    @get:SuppressLint("MissingPermission")
    private val lastLocation: Unit
        private get() {

            if (isLocationEnabled) {


                mFusedLocationClient.lastLocation.addOnCompleteListener { task ->
                    val location = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        latitude = location.latitude.toString()
                        longitude = location.longitude.toString()
                        Log.d("LAT>>>2", location.latitude.toString())
                        Log.d("LONG>>>", location.longitude.toString())

                    }
                }
            } else {
                Toast.makeText(mContext, "Please turn on" + " your location...", Toast.LENGTH_LONG)
                    .show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)

            }
        }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {

        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 5
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback, Looper.myLooper()
        )
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation = locationResult.lastLocation
            //  latitudeTextView.setText("Latitude: " + mLastLocation.getLatitude() + "");
            //longitTextView.setText("Longitude: " + mLastLocation.getLongitude() + "");
        }
    }


    // method to request for permissions

    // method to check
    // if location is enabled
    private val isLocationEnabled: Boolean
        private get() {
            val locationManager = mContext.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        }

    private fun cropImage(bitmap: Bitmap, rootView: View, smallFrame: View): Bitmap {
        val heightOriginal = rootView.height
        val widthOriginal = rootView.width
        val heightFrame = smallFrame.height
        val widthFrame = smallFrame.width
        val leftFrame = smallFrame.left
        val topFrame = smallFrame.top
        val heightReal = bitmap.height
        val widthReal = bitmap.width
        val widthFinal = widthFrame * widthReal / widthOriginal
        val heightFinal = heightFrame * heightReal / heightOriginal
        val leftFinal = leftFrame * widthReal / widthOriginal
        val topFinal = topFrame * heightReal / heightOriginal
        return Bitmap.createBitmap(
            bitmap, leftFinal, topFinal - 100, widthFinal, heightFinal + 100
        )
    }

    private inline fun <T : View> T.afterMeasured(crossinline f: T.() -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (measuredWidth > 0 && measuredHeight > 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    f()
                }
            }
        })

    }



    private fun capturePhoto(ca:String,mn:String,add:String) {
        val timestamp = System.currentTimeMillis()
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timestamp)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        imageCapture.takePicture(ImageCapture.OutputFileOptions.Builder(
            mContext.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        ).build(), getExecutor(), object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                if (Build.VERSION.SDK_INT >= 28) {

                    val source =
                        ImageDecoder.createSource(mContext.contentResolver, outputFileResults.savedUri!!)
                    var bitmap = ImageDecoder.decodeBitmap(source)
                    bitmap = cropImage(bitmap, activity!!.window.decorView.rootView,
                        binding.drawLL
                    )
                    val current = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
                    val formatted = current.format(formatter)
                    //       bitmap = saveImage(bitmap, "$formatted, $latitude, $longitude, 1.53, $ca",add)

                    Toast.makeText(
                        mContext,
                        "Photo has been saved successfully.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(
                    mContext,
                    "Error saving photo: " + exception.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
    private fun getExecutor(): Executor {
        return ContextCompat.getMainExecutor(mContext)
    }

    fun keepOnlyRealNumbers(input: String): String {
        return input.replace(Regex("[^0-9.]"), "")
    }
    fun showAlert( message: String) {
        val builder = AlertDialog.Builder(mContext)
        builder.setTitle("Error !")
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            // Handle positive button click if needed
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }


    fun uploadMeterImage(
        context: Context,
        bp: Bitmap
    ) {
        Log.d("RESP>>1","1")
        val pd:ProgressDia=ProgressDia()
        val unit = 1

        pd.show(childFragmentManager,"df")
        val url = "https://api.png-suvidha.in/api/meter-reading/upload"

        val request = object : VolleyMultipartRequest(
            Method.POST, url,
            Response.Listener { response ->


                pd.dismiss()
                val result = String(response.data)
                vm.res.value= Gson().fromJson(result, UploadMeterReadingImageRes::class.java)
                // Toast.makeText(mContext,result,Toast.LENGTH_LONG).show()
                val exceptionId = vm.res.value?.exception?.toIntOrNull()
                val reading = vm.res.value?.meter_reading?.lowercase()


                vm.ocrResults.value?.get(vm.curType.value!!)?.ocr_reading=vm.res.value?.meter_reading!!
                vm.ocrResults.value?.get(vm.curType.value!!)?.img_path=vm.res.value?.image_path!!.replace("/home/jbvnl/be/jbvnl_backend/src/ocr/images_power/","https://api.png-suvidha.in/api/images/")
                vm.ocrResults.value?.get(vm.curType.value!!)?.ocr_exception_code= vm.res.value?.exception?.toInt()!!
                vm.ocrResults.value?.get(vm.curType.value!!)?.ocr_exception_msg= ocrExps.find { it.id == vm.res.value?.exception?.toInt()!!}?.name.toString()
                vm.ocrResults.value?.get(vm.curType.value!!)?.ocr_mno= vm.res.value?.meter_no.toString()
                vm.ocrResults.value?.get(vm.curType.value!!)?.ocr_meter_make= vm.res.value?.meter_model.toString()
                vm.ocrResults.value?.get(vm.curType.value!!)?.ocr_unit= vm.res.value?.ocr_unit.toString()
                vm.ocrResults.value?.get(vm.curType.value!!)?.ocr_ref_id= vm.res.value?.id!!
                vm.ocrResults.value?.get(vm.curType.value!!)?.lat_long= vm.ocrRes.value?.lat.toString()+","+vm.ocrRes.value?.lng
                vm.ocrResults.value?.get(vm.curType.value!!)?.address= vm.ocrRes.value?.address.toString()


                Log.d("UNIT>>6","Here2")
                Log.d("UNIT>>7",vm.res.value?.exception!!)
                Log.d("UNIT>>8", ocrExps.size.toString())

                if(vm.capStage.value==1)
                {
                    navController.navigateUp()
                }
                else{
                    navController.navigate(CaptureFragDirections.actionCaptureFragToNewMeterCaptureFrag())

                }
                //   onSuccess(result)
            },
            Response.ErrorListener { error ->
                Log.d("RESP>>3",error.toString())
                Toast.makeText(context, "Error uploading image\n ${error.message}", Toast.LENGTH_SHORT).show()
                pd.dismiss()
                navController.navigateUp()

            }) {

            override fun getHeaders(): Map<String, String> {
                return mapOf(
                    "Authorization" to "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6ImpvaG5fZG9lIiwic3ViIjoxMCwiaWF0IjoxNzQ1OTQ5MDM4LCJleHAiOjE3NDY0Njc0Mzh9.WmBe69fjnO8CY0p2QsjsuPj6b5wQ_DEGuRzRQL_D94s",
                    "accept" to "*/*"
                )
            }

            override fun getParams(): Map<String, String> = mapOf(
                "reading_date_time" to getCurrentTimestamp(),
                "site_location" to vm.ocrRes.value?.address!!+" .",
                "ca_no" to vm.consumer.value?.acct_id!!,
                "image_path" to "DT 1",
                "meter_no" to "DT 1",
                "meter_reading" to "DT 1",
                "lat_long" to vm.ocrRes.value?.lat.toString()+","+vm.ocrRes.value?.lng,
                "address" to vm.ocrRes.value?.address!! +" .",
                "unit" to unit.toString(),
                "meter_reader" to vm.consumer.value?.meter_reader_id!!,
                "consumer" to vm.consumer.value?.acct_id!!,
                "mru" to "1",
                "exception" to "1",
                "location_type" to "1",
                "location" to "1",
                "agency" to "1"
            )
            override fun getByteData(): Map<String, DataPart> {
                return mapOf(
                    "file" to DataPart("img", "image/jpeg",bp)
                )
            }
        }
        request.retryPolicy = DefaultRetryPolicy(
            60000, // timeout in milliseconds (e.g., 30 seconds)
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        Volley.newRequestQueue(context).add(request)
    }
    fun fetchMeterReadingExceptions(context: Context) {
        val url = "https://api.png-suvidha.in/api/meter-reading-exceptions"

        val requestQueue = Volley.newRequestQueue(context)

        val stringRequest = object : StringRequest(
            Method.GET, url,
            Response.Listener { response ->
                // Handle success
                Log.e("Exps>>", response)

                ocrExps=Gson().fromJson(response, MeterReadingExceptionsRes::class.java)


            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e("VolleyError", error.toString())
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["accept"] = "*/*"
                headers["Authorization"] = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6ImpvaG5fZG9lIiwic3ViIjoxMCwiaWF0IjoxNzQ2MTI3MjMxLCJleHAiOjE3NDY2NDU2MzF9.Aht12k9e_DvNLBf-kxpCka5SmlxTNvxfxdd_KvLu0aQ"
                return headers
            }
        }
        Response.ErrorListener { error ->
            Log.d("RESP>>3",error.toString())
            if(error is NoConnectionError){
                Toast.makeText(context, "No internet !", Toast.LENGTH_SHORT).show()

            }
            pd.dismiss()
            navController.navigateUp()
        }

        requestQueue.add(stringRequest)
    }

    fun fetchMeterReadingUnits(context: Context) {
        Log.e("Units>>", "response")

        val url = "https://api.png-suvidha.in/api/meter-reading-unit"

        val requestQueue = Volley.newRequestQueue(context)

        val stringRequest = object : StringRequest(
            Method.GET, url,
            Response.Listener { response ->
                // Handle success
                Log.e("Units>>", response)

                ocrUnits=Gson().fromJson(response, MeterReadingUnitsRes::class.java)


            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e("VolleyError", error.toString())
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["accept"] = "*/*"
                headers["Authorization"] = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6ImpvaG5fZG9lIiwic3ViIjoxMCwiaWF0IjoxNzQ2MTI3MjMxLCJleHAiOjE3NDY2NDU2MzF9.Aht12k9e_DvNLBf-kxpCka5SmlxTNvxfxdd_KvLu0aQ"
                return headers
            }
        }

        requestQueue.add(stringRequest)
    }
    fun getCurrentTimestamp(): String {
        val date = Date()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(date)
    }
}