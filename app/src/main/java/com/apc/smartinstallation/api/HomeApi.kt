package com.apc.smartinstallation.api

import com.apc.smartinstallation.dataClasses.Consumer
import com.apc.smartinstallation.dataClasses.FieldVisitHistoryRes
import com.apc.smartinstallation.dataClasses.GetConsumerListRes
import com.apc.smartinstallation.dataClasses.LoginRequest
import com.apc.smartinstallation.dataClasses.LoginResponse
import com.apc.smartinstallation.dataClasses.MeterReadingExceptionsResItem
import com.apc.smartinstallation.dataClasses.MeterReadingUnitsResItem
import com.apc.smartinstallation.dataClasses.PostCallRecordRes
import com.apc.smartinstallation.dataClasses.ServerCallLogsRes
import com.apc.smartinstallation.dataClasses.SubmitFieldVisitDetailsRes
import com.apc.smartinstallation.dataClasses.UpdateMobileReq
import com.apc.smartinstallation.dataClasses.UpdateMobileRes
import com.apc.smartinstallation.dataClasses.UploadMeterReadingImageRes
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface HomeApi {
    @GET("/meterreplacement/API/get_consumers_list.php")
    suspend fun getConsumers(
        @Query("reader_id") readerId: String,
    ): Response<GetConsumerListRes>

    @GET("/meter-reading-exceptions")
    suspend fun getMrExceptions(

    ): Response<List<MeterReadingExceptionsResItem>>

    @GET("/meter-reading-unit")
    suspend fun getMrUnit(

    ): Response<List<MeterReadingUnitsResItem>>

    @Multipart
    @POST("meter-reading/upload")
    suspend fun uploadMeterImage(
        @Part file: MultipartBody.Part,
        @Part("agency") agency: RequestBody,
        @Part("reading_date_time") readingDateTime: RequestBody,
        @Part("site_location") siteLocation: RequestBody,
        @Part("ca_no") caNo: RequestBody,
        @Part("image_path") imagePath: RequestBody,
        @Part("meter_no") meterNo: RequestBody,
        @Part("meter_reading") meterReading: RequestBody,
        @Part("lat_long") latLong: RequestBody,
        @Part("address") address: RequestBody,
        @Part("unit") unit: RequestBody,
        @Part("meter_reader") meterReader: RequestBody,
        @Part("consumer") consumer: RequestBody,
        @Part("mru") mru: RequestBody,
        @Part("exception") exception: RequestBody,
        @Part("location_type") locationType: RequestBody,
        @Part("location") location: RequestBody,
        @Part("load") load: RequestBody

    ): Response<UploadMeterReadingImageRes>


}