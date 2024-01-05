package com.bignerdranch.photogallery.api

import retrofit2.http.GET
import retrofit2.http.Query

//private const val API_KEY = "adc9fd5d64a8f0cc27302c4568c2dcbf"
interface FlickrApi {
//    @GET("/")
//    suspend fun fetchContents(): String
//
//    @GET(
//        "services/rest/?method=flickr.interestingness.getList" +
//                "&api_key=$API_KEY" +
//                "&format=json" +
//                "&nojsoncallback=1" +
//                "&extras=url_s"
//    )
    @GET("services/rest/?method=flickr.interestingness.getList")
    suspend fun fetchPhotos(): FlickrResponse

    @GET("service/rest?method=flickr.photos.search")
    suspend fun searchPhotos(@Query("text") query: String): FlickrResponse

}