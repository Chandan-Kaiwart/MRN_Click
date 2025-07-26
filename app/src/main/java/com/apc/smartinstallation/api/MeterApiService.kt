package com.apc.smartinstallation.api

import com.apc.smartinstallation.dataClasses.CiMeterRequest
import com.apc.smartinstallation.dataClasses.MRNReadingRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface MeterApiService {
    // For CI Portal (Old Meter)
    @POST("insert_mob_data_ci.php")
    suspend fun saveToCiPortal(
        @Body request: CiMeterRequest
    ): Response<ApiResponse>

    // For MI Portal (New Meter)
    @FormUrlEncoded
    @POST("insert_mob_data_mi.php")
    suspend fun saveToMiPortal(
        @Field("acct_id") acctId: String,
        @Field("organization") organization: String,
        @Field("old_meter_image") oldMeterImage: String,
        @Field("old_meter_model") oldMeterModel: String,
        @Field("old_meter_number") oldMeterNumber: String,
        @Field("old_meter_reading") oldMeterReading: String,
        @Field("old_meter_phase") oldMeterPhase: String,
        @Field("new_meter_image") newMeterImage: String,
        @Field("new_meter_model") newMeterModel: String,
        @Field("new_meter_number") newMeterNumber: String,
        @Field("new_meter_reading") newMeterReading: String,
        @Field("new_meter_phase") newMeterPhase: String,
        @Field("new_meter_box_seal") newMeterBoxSeal: String,
        @Field("new_meter_seal") newMeterSeal: String,
        @Field("mrn_pdf") mrnPdf: String
    ): Response<ApiResponse>

    // For MRN Portal (Meter Replacement New)
    @Multipart
    @POST("insert_mob_mrn_data.php")
    suspend fun saveMRNReading(
        @Part("reading") reading: RequestBody,
        @Part("consumer_name") consumerName: RequestBody,
        @Part("consumer_no") consumerNo: RequestBody,
        @Part("consumer_mobile_no") consumerMobile: RequestBody?,
        @Part("box_seal") boxSeal: RequestBody?,
        @Part("body_seal") bodySeal: RequestBody?,
        @Part("terminal_seal") terminalSeal: RequestBody?,
        @Part("old_meter_no") oldMeterNo: RequestBody?,
        @Part("old_meter_reading") oldMeterReading: RequestBody?,
        @Part("old_meter_make") oldMeterMake: RequestBody?,
        @Part("old_phase") oldPhase: RequestBody?,
        @Part("meter_make") meterMake: RequestBody?,
        @Part("phase") phase: RequestBody?,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("account_id") accountId: RequestBody?,
        @Part("manual_reading") manualReading: RequestBody?,
        @Part image: MultipartBody.Part
    ): Response<ApiResponse>
}