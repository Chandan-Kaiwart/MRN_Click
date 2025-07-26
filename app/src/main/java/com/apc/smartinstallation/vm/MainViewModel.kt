package com.apc.smartinstallation.vm

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.location.Geocoder
import android.os.Looper
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apc.lossreduction.dataClasses.geocode.GeocodeResponse
import com.apc.smartinstallation.api.MeterReadingRequest
import com.apc.smartinstallation.dataClasses.Consumer
import com.apc.smartinstallation.dataClasses.GetConsumerListRes
import com.apc.smartinstallation.dataClasses.MRNReadingRequest
import com.apc.smartinstallation.dataClasses.MeterReadingExceptionsResItem
import com.apc.smartinstallation.dataClasses.MeterReadingUnitsResItem
import com.apc.smartinstallation.dataClasses.UploadMeterReadingImageReq
import com.apc.smartinstallation.dataClasses.UploadMeterReadingImageRes
import com.apc.smartinstallation.dataClasses.directions.GoogleDirectionsApiResponse
import com.apc.smartinstallation.dataClasses.login.LoginRes
import com.apc.smartinstallation.dataStore.UserPreferences
import com.apc.smartinstallation.dispatchers.DispatcherTypes
import com.apc.smartinstallation.repository.home.HomeDefRepo
import com.apc.smartinstallation.repository.login.LoginDefRepo
import com.apc.smartinstallation.util.Resource
import com.apc.smartinstallation.dataClasses.ocr.request.OcrRequest
import com.apc.smartinstallation.dataClasses.ocr.response.OcrResponse
import com.apc.smartinstallation.dataClasses.ocr.response.OcrResult

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import javax.inject.Inject
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: HomeDefRepo,
    private val loginRepository: LoginDefRepo,
    private val dispatchers: DispatcherTypes,
    private val userPreferences: UserPreferences,
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    private val application: Application,


) : ViewModel() {

    /* init {
          startLocationUpdates()

      }*/
    // Add this to your existing MainViewModel class
    val mrnData = MutableLiveData<MRNReadingRequest?>()

    // Add this function to set MRN data
    fun setMrnData(data: MRNReadingRequest) {
        mrnData.value = data
    }
    val meterSlipUploaded = MutableLiveData<Boolean>(false)
    val meterSlipImage = MutableLiveData<Bitmap?>()
    val meterSlipPath = MutableLiveData<String?>()
    val lat = MutableLiveData<String>("")
    val sign1 = MutableLiveData<String>("")
    val sign2 = MutableLiveData<String>("")
    val capStage = MutableLiveData<Int>(1)

    // image path
    val oldMeterBitmap = MutableLiveData<Bitmap?>()
    val newMeterBitmap = MutableLiveData<Bitmap?>()
    // Current meter image store karne ke liye
    val currentMeterImage = MutableLiveData<Bitmap?>()

    private val _capturedImage = MutableLiveData<Bitmap?>()
    val capturedImage: LiveData<Bitmap?> get() = _capturedImage

    fun setCapturedImage(bitmap: Bitmap?) {
        _capturedImage.value = bitmap
    }

    // Old meter specific image
    val oldMeterImage = MutableLiveData<Bitmap?>()

    // New meter specific image
    val newMeterImage = MutableLiveData<Bitmap?>()

    // MainViewModel.kt - Add this to your existing ViewModel
    var currentMeterImageForValidation: Bitmap? = null

    val ocrResults = MutableLiveData<MutableList<OcrResult>>(mutableListOf())
    val ocrRes = MutableLiveData<OcrResponse>(OcrResponse(

        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "manual",
        emptyList(),
        "",
        "",
        OcrRequest("","","","","","","","", emptyList()),
        "",
        "",
        "",

        ))
    val curType = MutableLiveData<Int>(0)
    val res = MutableLiveData<UploadMeterReadingImageRes>()
    val inp = MutableLiveData<OcrRequest>()
    val retakes = MutableLiveData<Int>(0)


    val long = MutableLiveData<String>("")

    val address = MutableLiveData<String>("")

    val excep = MutableLiveData<Int>(0)

    val unit = MutableLiveData<Int>(0)
    val unit_taken = MutableLiveData<Int>(0)

    // In MainViewModel.kt - Add these with other LiveData properties
// Basic Information
    val bookNo = MutableLiveData<String>().apply { value = "09" }
    val mrnSlipNo = MutableLiveData<String>().apply { value = "0426" }
    val circleName = MutableLiveData<String>().apply { value = "Circle A" }
    val division = MutableLiveData<String>().apply { value = "Division 1" }
    val subDivision = MutableLiveData<String>().apply { value = "Sub Division 1" }
    val feederName = MutableLiveData<String>().apply { value = "Feeder 123" }
    val dtrNameCode = MutableLiveData<String>().apply { value = "DTR-456" }
    val poleNo = MutableLiveData<String>().apply { value = "P-789" }
    val repName = MutableLiveData<String>().apply { value = "Technician A" }

    // Meter seals
    val oldBoxSeal = MutableLiveData<String>().apply { value = "No" }
    val oldBodySeal = MutableLiveData<String>().apply { value = "No" }
    val oldTerminalSeal = MutableLiveData<String>().apply { value = "No" }
    val newBoxSeal = MutableLiveData<String>().apply { value = "Yes" }
    val newBodySeal = MutableLiveData<String>().apply { value = "Yes" }
    val newTerminalSeal = MutableLiveData<String>().apply { value = "Yes" }

    // Meter status
    val oldMeterStatus = MutableLiveData<String>().apply { value = "Working" }
    val newMeterStatus = MutableLiveData<String>().apply { value = "Installed" }

    // Meter phases
    val oldMeterPhase = MutableLiveData<String>().apply { value = "1" }
    val newMeterPhase = MutableLiveData<String>().apply { value = "1" }

    // Meter MD values
    val oldMeterMD = MutableLiveData<String>().apply { value = "5.6" }
    val newMeterMD = MutableLiveData<String>().apply { value = "0.0" }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation ?: return
            lat.value = location.latitude.toString()
            long.value = location.longitude.toString()
            fetchAddress(location.latitude, location.longitude)
        }
    }
    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun fetchAddress(latitude: Double, longitude: Double) {
        try {
            val geocoder = Geocoder(application, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            address.value = addresses?.firstOrNull()?.getAddressLine(0) ?: "Unknown Location"
        } catch (e: Exception) {
            e.printStackTrace()
            address.value = "Failed to fetch address"
        }
    }

    var consumer = MutableLiveData<Consumer>()


    private val _userState = MutableStateFlow<String?>("-1")

    val userState: StateFlow<String?> = _userState

    init {
        // Load user ID on ViewModel initialization
        viewModelScope.launch {
            _userState.value = userPreferences.user.first()
        }
    }

    fun saveUser(user: LoginRes) {
        viewModelScope.launch {
            val jsonStr=Gson().toJson(user)
            userPreferences.saveUser(user)
            _userState.value = jsonStr
        }
    }

    fun clearUser() {
        viewModelScope.launch {
            userPreferences.clearSession()
            _userState.value = null
        }
    }
    sealed class UploadMeterReadingImageEvent {
        object Empty : UploadMeterReadingImageEvent()
        object Loading : UploadMeterReadingImageEvent()
        data class Success(val result: UploadMeterReadingImageRes?) : UploadMeterReadingImageEvent()
        data class Failure(val errorText: String) : UploadMeterReadingImageEvent()
    }

    private val _uploadMeterReadingImageState = MutableStateFlow<UploadMeterReadingImageEvent>(
        UploadMeterReadingImageEvent.Empty
    )
    val uploadMeterReadingImageState: StateFlow<UploadMeterReadingImageEvent> = _uploadMeterReadingImageState


    fun bitmapToFile(context: Context, bitmap: Bitmap): File {
        val file = File(context.cacheDir, "upload_image_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
        return file
    }
    fun uploadMeterReadingImage(file: File, meterReadingRequest: UploadMeterReadingImageReq) {
        viewModelScope.launch(dispatchers.io) {
            _uploadMeterReadingImageState.value =
                UploadMeterReadingImageEvent.Loading // Set loading state
            when (val result = repository.uploadMeterReadingImage(file, meterReadingRequest)) {
                is Resource.Success -> {
                    _uploadMeterReadingImageState.value =
                        UploadMeterReadingImageEvent.Success(result.data)

                }
                is Resource.Error -> {
                    _uploadMeterReadingImageState.value =
                        UploadMeterReadingImageEvent.Failure("Submission Failed: ${result.message}")
                }


            }
        }
    }

    fun uploadMeterReadingImage(context:Context,bmp: Bitmap, meterReadingRequest: UploadMeterReadingImageReq) {
        viewModelScope.launch(dispatchers.io) {
            val file = bitmapToFile(context, bmp)
            _uploadMeterReadingImageState.value =
                UploadMeterReadingImageEvent.Loading // Set loading state
            when (val result = repository.uploadMeterReadingImage(file, meterReadingRequest)) {
                is Resource.Success -> {
                    _uploadMeterReadingImageState.value =
                        UploadMeterReadingImageEvent.Success(result.data)

                }
                is Resource.Error -> {
                    _uploadMeterReadingImageState.value =
                        UploadMeterReadingImageEvent.Failure("Submission Failed: ${result.message}")
                }
            }
        }
    }

    fun reInitUploadMeterReadingImage() {
        _uploadMeterReadingImageState.value = UploadMeterReadingImageEvent.Empty
    }

    // Sealed class to represent different states of the login process
    sealed class LoginEvent {
        object Empty : LoginEvent()
        object Loading : LoginEvent()
        data class Success(val resultText: LoginRes?) : LoginEvent()
        data class Failure(val errorText: String) : LoginEvent()
    }

    // Mutable StateFlow to hold the current state of login
    private val _loginState = MutableStateFlow<LoginEvent>(LoginEvent.Empty)

    // Exposed immutable StateFlow for composable to observe
    val loginState: StateFlow<LoginEvent> = _loginState

    // Function to handle user login
    fun loginUser(username: String, password: String) {
        viewModelScope.launch(dispatchers.io) {
            _loginState.value = LoginEvent.Loading
            when (val result = loginRepository.loginUser(username, password)) {
                is Resource.Success -> {
                    if (!result.data!!.error) {
                        _loginState.value = LoginEvent.Success(result.data)
                    } else {
                        _loginState.value =
                            LoginEvent.Failure(result.data.message ?: "Unknown Error")
                    }
                }

                is Resource.Error -> {
                    _loginState.value = LoginEvent.Failure(result.message ?: "Network Error")
                }
            }
        }
    }


    // Reinitialize login state
    fun reInitLogin() {
        _loginState.value = LoginEvent.Empty
    }




    // Sealed class to represent different states of the login process
    sealed class GetAssignedConsumersEvent {
        object Empty : GetAssignedConsumersEvent()
        object Loading : GetAssignedConsumersEvent()
        data class Success(val resultText: GetConsumerListRes?) : GetAssignedConsumersEvent()
        data class Failure(val errorText: String) : GetAssignedConsumersEvent()
    }

    // Mutable StateFlow to hold the current state of login
    private val _consListState =
        MutableStateFlow<GetAssignedConsumersEvent>(GetAssignedConsumersEvent.Empty)

    // Exposed immutable StateFlow for composable to observe
    val consListState: StateFlow<GetAssignedConsumersEvent> = _consListState

    // Function to handle user login
    fun getAssignedConsumers(mrId: String) {
        viewModelScope.launch(dispatchers.io) {
            _consListState.value = GetAssignedConsumersEvent.Loading // Set loading state
            when (val result = repository.getAssignedConsumers(mrId)) {
                is Resource.Success -> {

                    try {
                        if (result.data!!.data.isNotEmpty()) {


                            _consListState.value = GetAssignedConsumersEvent.Success(result.data)

                        } else {
                            _consListState.value = GetAssignedConsumersEvent.Failure("No Data Found !")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                is Resource.Error -> {
                    _consListState.value =
                        GetAssignedConsumersEvent.Failure(result.message ?: "Network Error")
                }
            }
        }
    }

    // Reinitialize login state
    fun reInitAssignedConsList() {
        _consListState.value = GetAssignedConsumersEvent.Empty
    }


    //////


    sealed class CalculateDistancesEvent {
        object Empty : CalculateDistancesEvent()
        object Loading : CalculateDistancesEvent()
        data class Success(val updatedCons: List<Consumer>) : CalculateDistancesEvent()
        data class Failure(val errorText: String) : CalculateDistancesEvent()
    }

    private val _distanceState =
        MutableStateFlow<CalculateDistancesEvent>(CalculateDistancesEvent.Empty)
    val distanceState: StateFlow<CalculateDistancesEvent> = _distanceState



    fun reInitDistanceState() {
        _distanceState.value = CalculateDistancesEvent.Empty
    }

    private fun haversineGreatCircleDistance(
        latitudeFrom: Double,
        longitudeFrom: Double,
        latitudeTo: Double,
        longitudeTo: Double,
        earthRadius: Double = 6371000.0
    ): Double {
        val latFrom = Math.toRadians(latitudeFrom)
        val lonFrom = Math.toRadians(longitudeFrom)
        val latTo = Math.toRadians(latitudeTo)
        val lonTo = Math.toRadians(longitudeTo)

        val latDelta = latTo - latFrom
        val lonDelta = lonTo - lonFrom

        val a = sin(latDelta / 2).pow(2) +
                cos(latFrom) * cos(latTo) * sin(lonDelta / 2).pow(2)

        val angle = 2 * asin(sqrt(a))
        return angle * earthRadius
    }

    // GeoCode

    sealed class GeocodeEvent {
        object Empty : GeocodeEvent()
        object Loading : GeocodeEvent()
        data class Success(val resultText: GeocodeResponse?) : GeocodeEvent()
        data class Failure(val errorText: String) : GeocodeEvent()

    }

    private val _geocodeState = MutableStateFlow<GeocodeEvent>(GeocodeEvent.Empty)
    val geocodeState: StateFlow<GeocodeEvent> = _geocodeState
    fun geocode(key: String, address: String) {
        viewModelScope.launch(dispatchers.io) {
            _geocodeState.value = GeocodeEvent.Loading // Set loading state
            when (val result = repository.geocode(key, address)) {
                is Resource.Success -> {
                    if (result.data!!.status == "OK" && result.data.results.isNotEmpty()) {
                        _geocodeState.value = GeocodeEvent.Success(result.data)
                    } else {
                        _geocodeState.value = GeocodeEvent.Failure(result.data.status)


                    }
                }

                is Resource.Error -> {
                    _geocodeState.value = GeocodeEvent.Failure("Geocode Failed")

                }
            }
        }

    }

    fun reInitGeocode() {
        _geocodeState.value = GeocodeEvent.Empty
    }

    ///

    // Define the DirectionsEvent sealed class
    sealed class DirectionsEvent {
        object Empty : DirectionsEvent()
        object Loading : DirectionsEvent()
        data class Success(val resultText: GoogleDirectionsApiResponse?) : DirectionsEvent()
        data class Failure(val errorText: String) : DirectionsEvent()
    }

    // StateFlow to manage the current state of directions
    private val _directionsState = MutableStateFlow<DirectionsEvent>(DirectionsEvent.Empty)
    val directionsState: StateFlow<DirectionsEvent> = _directionsState

    // Fetch directions from the repository
    fun getDirections(origin: String, destination: String, waypoints: String) {
        viewModelScope.launch(dispatchers.io) {
            _directionsState.value = DirectionsEvent.Loading // Set loading state
            when (val result = repository.getDirections(origin, destination, waypoints)) {
                is Resource.Success -> {
                    if (result.data?.status == "OK" && result.data.routes.isNotEmpty()) {
                        _directionsState.value = DirectionsEvent.Success(result.data)
                    } else {
                        _directionsState.value = DirectionsEvent.Failure("Directions not found")
                    }
                }
                is Resource.Error -> {
                    _directionsState.value = DirectionsEvent.Failure("Failed to fetch directions")
                }
            }
        }
    }

    // Re-initialize the state
    fun reInitDirections() {
        _directionsState.value = DirectionsEvent.Empty
    }

    //
    // Define events for Meter Reading operations
    sealed class MeterReadingEvent {
        object Empty : MeterReadingEvent()
        object Loading : MeterReadingEvent()
        data class DeleteSuccess(val success: Boolean) : MeterReadingEvent()
        data class SaveSuccess(val success: Boolean) : MeterReadingEvent()
        data class GetSuccess(val readings: List<UploadMeterReadingImageReq>) : MeterReadingEvent()
        data class Failure(val errorText: String) : MeterReadingEvent()
    }
    // StateFlow for saving meter reading state
    private val _deleteMeterReadingState = MutableStateFlow<MeterReadingEvent>(MeterReadingEvent.Empty)
    val deleteMeterReadingState: StateFlow<MeterReadingEvent> = _deleteMeterReadingState


    // StateFlow for saving meter reading state
    private val _saveMeterReadingState = MutableStateFlow<MeterReadingEvent>(MeterReadingEvent.Empty)
    val saveMeterReadingState: StateFlow<MeterReadingEvent> = _saveMeterReadingState

    // StateFlow for fetching meter readings state
    private val _meterReadingListState = MutableStateFlow<MeterReadingEvent>(MeterReadingEvent.Empty)
    val meterReadingListState: StateFlow<MeterReadingEvent> = _meterReadingListState

    /**
     * Save Meter Reading Data
     */


    /**
     * Reset states
     */
    fun reInitSaveState() {
        _saveMeterReadingState.value = MeterReadingEvent.Empty
    }

    fun reInitGetState() {
        _meterReadingListState.value = MeterReadingEvent.Empty
    }



    // Define Event Sealed Class
    sealed class MeterReadingExceptionsEvent {
        object Empty : MeterReadingExceptionsEvent()
        object Loading : MeterReadingExceptionsEvent()
        data class GetSuccess(val exceptions: List<MeterReadingExceptionsResItem>) : MeterReadingExceptionsEvent()
        data class Failure(val errorText: String) : MeterReadingExceptionsEvent()
    }

    // StateFlow to manage UI state
    private val _meterReadingExceptionsState = MutableStateFlow<MeterReadingExceptionsEvent>(
        MeterReadingExceptionsEvent.Empty
    )
    val meterReadingExceptionsState: StateFlow<MeterReadingExceptionsEvent> = _meterReadingExceptionsState

    // Function to get meter reading exceptions
    fun getMeterReadingExceptions() {
        viewModelScope.launch(Dispatchers.IO) {
            _meterReadingExceptionsState.value = MeterReadingExceptionsEvent.Loading
            when (val result = repository.getMeterReadingExceptions()) {
                is Resource.Success -> {
                    if (result.data!!.isNotEmpty()) {
                        _meterReadingExceptionsState.value =
                            MeterReadingExceptionsEvent.GetSuccess(result.data)
                    } else {
                        _meterReadingExceptionsState.value =
                            MeterReadingExceptionsEvent.Failure("No exceptions found")
                    }
                }
                is Resource.Error -> {
                    _meterReadingExceptionsState.value =
                        MeterReadingExceptionsEvent.Failure(result.message ?: "An error occurred")
                }
            }
        }
    }

    // Reset state
    fun resetMeterReadingExceptions() {
        _meterReadingExceptionsState.value = MeterReadingExceptionsEvent.Empty
    }



    // Define Event Sealed Class
    sealed class MeterReadingUnitsEvent {
        object Empty : MeterReadingUnitsEvent()
        object Loading : MeterReadingUnitsEvent()
        data class GetSuccess(val units: List<MeterReadingUnitsResItem>) : MeterReadingUnitsEvent()
        data class Failure(val errorText: String) : MeterReadingUnitsEvent()
    }

    // StateFlow to manage UI state
    private val _meterReadingUnitsState = MutableStateFlow<MeterReadingUnitsEvent>(
        MeterReadingUnitsEvent.Empty
    )
    val meterReadingUnitsState: StateFlow<MeterReadingUnitsEvent> = _meterReadingUnitsState

    // Function to get meter reading units
    fun getMeterReadingUnits() {
        viewModelScope.launch(Dispatchers.IO) {
            _meterReadingUnitsState.value = MeterReadingUnitsEvent.Loading
            when (val result = repository.getMeterReadingUnits()) {
                is Resource.Success -> {
                    if (result.data!!.isNotEmpty()) {
                        _meterReadingUnitsState.value =
                            MeterReadingUnitsEvent.GetSuccess(result.data)
                    } else {
                        _meterReadingUnitsState.value =
                            MeterReadingUnitsEvent.Failure("No units found")
                    }
                }
                is Resource.Error -> {
                    _meterReadingUnitsState.value =
                        MeterReadingUnitsEvent.Failure(result.message ?: "An error occurred")
                }
            }
        }
    }

    // Reset state
    fun resetMeterReadingUnits() {
        _meterReadingUnitsState.value = MeterReadingUnitsEvent.Empty
    }

}