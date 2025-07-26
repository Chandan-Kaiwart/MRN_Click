package com.apc.smartinstallation.repository.home

import com.apc.lossreduction.dataClasses.geocode.GeocodeResponse
import com.apc.smartinstallation.dataClasses.Consumer
import com.apc.smartinstallation.dataClasses.GetConsumerListRes
import com.apc.smartinstallation.dataClasses.MeterReadingExceptionsResItem
import com.apc.smartinstallation.dataClasses.MeterReadingUnitsResItem
import com.apc.smartinstallation.dataClasses.UploadMeterReadingImageReq
import com.apc.smartinstallation.dataClasses.UploadMeterReadingImageRes
import com.apc.smartinstallation.dataClasses.directions.GoogleDirectionsApiResponse
import com.apc.smartinstallation.util.Resource
import java.io.File


interface HomeMainRepo {


    suspend fun getAssignedConsumers(mrId: String): Resource<GetConsumerListRes>






    suspend fun geocode(key: String, address: String): Resource<GeocodeResponse>

    suspend fun getDirections(origin: String,
                              destination: String,
                              waypoints: String):
            Resource<GoogleDirectionsApiResponse>

    suspend fun uploadMeterReadingImage(
        file: File,
        data: UploadMeterReadingImageReq
    ): Resource<UploadMeterReadingImageRes>


    suspend fun getMeterReadingExceptions(
    ): Resource<List<MeterReadingExceptionsResItem>>

    suspend fun getMeterReadingUnits(
    ): Resource<List<MeterReadingUnitsResItem>>




}