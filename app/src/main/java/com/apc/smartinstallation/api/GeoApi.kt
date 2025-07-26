package com.apc.smartinstallation.api

import com.apc.lossreduction.dataClasses.geocode.GeocodeResponse
import com.apc.smartinstallation.dataClasses.directions.GoogleDirectionsApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GeoApi {
    @GET("/maps/api/geocode/json")
    suspend fun geocode(
        @Query("key") apiKey: String,
        @Query("address") address: String
    ): Response<GeocodeResponse>

    @GET("/maps/api/directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("waypoints") waypoints: String,
        @Query("mode") mode: String = "road",
        @Query("key") apiKey: String = "AIzaSyDtj4Bwn_vqj0Dq7B--q51phjr39jYYAKA"
    ): Response<GoogleDirectionsApiResponse>

    /*

    https://maps.googleapis.com/maps/api/directions/json?
    origin=Mundka, Delhi, India&destination=NIT DELHI, Delhi, India
    &waypoints=optimize:true|Rohini,Delhi|Nangloi,Delhi|Peeragarhi,Delhi|Jahangirpuri,Delhi
    &mode=road&key=AIzaSyDtj4Bwn_vqj0Dq7B--q51phjr39jYYAKA
     */


}