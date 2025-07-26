package com.apc.smartinstallation.repository.home

import android.util.Log
import com.apc.lossreduction.dataClasses.geocode.GeocodeResponse
import com.apc.smartinstallation.api.GeoApi
import com.apc.smartinstallation.api.HomeApi
import com.apc.smartinstallation.dataClasses.Consumer
import com.apc.smartinstallation.dataClasses.GetConsumerListRes
import com.apc.smartinstallation.dataClasses.MeterReadingExceptionsResItem
import com.apc.smartinstallation.dataClasses.MeterReadingUnitsResItem
import com.apc.smartinstallation.dataClasses.UploadMeterReadingImageReq
import com.apc.smartinstallation.dataClasses.UploadMeterReadingImageRes
import com.apc.smartinstallation.dataClasses.directions.GoogleDirectionsApiResponse
import com.apc.smartinstallation.dispatchers.DispatcherTypes
import com.apc.smartinstallation.util.Resource
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeDefRepo @Inject constructor(
    private val apiService: HomeApi,
    private val geoApiService:GeoApi,
    private val dispatcherProvider: DispatcherTypes
) : HomeMainRepo {
    override suspend fun geocode(key:String,address:String)
            : Resource<GeocodeResponse> = withContext(dispatcherProvider.io) {
        try {
            // Attempt to login with the remote server
            val response = geoApiService.geocode(key,address)
            val result = response.body()
            if (response.isSuccessful && result != null) {
                Log.d("RETRO>>", response.code().toString())
                Resource.Success(result)
            } else {

                // On failure, fallback to local database
                Log.d("RETRO>>", "Error")

                //   val res = consumerDao.getUser(loginReq.username)
                Resource.Error(response.message())

            }
        } catch (e: IOException) {
            // Network issue, use local database
            //  val res = userDao.getUser(loginReq.username)
            Resource.Error(e.message)

        }
    }


    override suspend fun getAssignedConsumers(mrId: String)
    : Resource<GetConsumerListRes> = withContext(dispatcherProvider.io) {
        try {
            // Attempt to login with the remote server
            val response = apiService.getConsumers(mrId)
            val result = response.body()
            if (response.isSuccessful && result != null) {
                Log.d("RETRO>>", response.code().toString())
                Resource.Success(result)
            } else {

                // On failure, fallback to local database
                Log.d("RETRO>>", "Error")

             //   val res = consumerDao.getUser(loginReq.username)
                Resource.Error(response.message())

            }
        } catch (e: IOException) {
            // Network issue, use local database
          //  val res = userDao.getUser(loginReq.username)
            Resource.Error(e.message)

        }
    }


    override suspend fun getMeterReadingExceptions()
            : Resource<List<MeterReadingExceptionsResItem>> = withContext(dispatcherProvider.io) {
        try {
            // Attempt to login with the remote server
            val response = apiService.getMrExceptions()
            val result = response.body()
            if (response.isSuccessful && result != null) {
                Log.d("RETRO>>", response.code().toString())
                Resource.Success(result)
            } else {

                // On failure, fallback to local database
                Log.d("RETRO>>", "Error")

                //   val res = consumerDao.getUser(loginReq.username)
                Resource.Error(response.message())

            }
        } catch (e: IOException) {
            // Network issue, use local database
            //  val res = userDao.getUser(loginReq.username)
            Resource.Error(e.message)

        }
    }


    override suspend fun getMeterReadingUnits()
            : Resource<List<MeterReadingUnitsResItem>> = withContext(dispatcherProvider.io) {
        try {
            // Attempt to login with the remote server
            val response = apiService.getMrUnit()
            val result = response.body()
            if (response.isSuccessful && result != null) {
                Log.d("RETRO>>", response.code().toString())
                Resource.Success(result)
            } else {

                // On failure, fallback to local database
                Log.d("RETRO>>", "Error")

                //   val res = consumerDao.getUser(loginReq.username)
                Resource.Error(response.message())

            }
        } catch (e: IOException) {
            // Network issue, use local database
            //  val res = userDao.getUser(loginReq.username)
            Resource.Error(e.message)

        }
    }


    override suspend fun uploadMeterReadingImage(
        file: File,
        data: UploadMeterReadingImageReq
    ): Resource<UploadMeterReadingImageRes> = withContext(dispatcherProvider.io) {
        try {
            val imagePart = MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )

            val fields = mapOf(
                "agency" to data.agency.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                "reading_date_time" to data.reading_date_time.toRequestBody("text/plain".toMediaTypeOrNull()),
                "site_location" to data.site_location.toRequestBody("text/plain".toMediaTypeOrNull()),
                "ca_no" to data.ca_no.toRequestBody("text/plain".toMediaTypeOrNull()),
                "image_path" to data.image_path.toRequestBody("text/plain".toMediaTypeOrNull()),
                "meter_no" to data.meter_no.toRequestBody("text/plain".toMediaTypeOrNull()),
                "meter_reading" to data.meter_reading.toRequestBody("text/plain".toMediaTypeOrNull()),
                "lat_long" to data.lat_long.toRequestBody("text/plain".toMediaTypeOrNull()),
                "address" to data.address.toRequestBody("text/plain".toMediaTypeOrNull()),
                "unit" to data.unit.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                "meter_reader" to data.meter_no.toRequestBody("text/plain".toMediaTypeOrNull()),
                "consumer" to data.consumer.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                "mru" to data.mru.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                "exception" to data.exception.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                "location_type" to data.location_type.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                "location" to data.location.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                "load" to data.load.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            )

            val response = apiService.uploadMeterImage(
                imagePart,
                fields["agency"]!!,
                fields["reading_date_time"]!!, fields["site_location"]!!, fields["ca_no"]!!,
                fields["image_path"]!!, fields["meter_no"]!!, fields["meter_reading"]!!,
                fields["lat_long"]!!, fields["address"]!!, fields["unit"]!!,
                fields["meter_reader"]!!, fields["consumer"]!!, fields["mru"]!!,
                fields["exception"]!!, fields["location_type"]!!, fields["location"]!!, fields["load"]!!
            )


            val result = response.body()
            if (response.isSuccessful && result != null) {
                Resource.Success(result)
            } else {
                Resource.Error(response.message())
            }
        } catch (e: IOException) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }



    override suspend fun getDirections(
        origin: String,
        destination: String,
        waypoints: String
    ): Resource<GoogleDirectionsApiResponse>  = withContext(dispatcherProvider.io) {
        try {
            // Attempt to login with the remote server
            val response = geoApiService.getDirections(origin, destination, waypoints)
            val result = response.body()
            if (response.isSuccessful && result != null && result.routes.isNotEmpty()) {
                Log.d("RETRO>>", response.code().toString())
                Resource.Success(result)
            } else {

                // On failure, fallback to local database
                Log.d("RETRO>>", "Error")

                Resource.Error(response.message())

            }
        } catch (e: IOException) {
            // Network issue, use local database
            Resource.Error(e.message)

        }
    }



}
