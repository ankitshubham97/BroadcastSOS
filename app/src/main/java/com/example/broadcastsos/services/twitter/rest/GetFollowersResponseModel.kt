package com.example.broadcastsos.services.twitter.rest


import com.google.gson.annotations.SerializedName


data class GetFollowersResponseModel (

    @SerializedName("data" ) var data : ArrayList<Data> = arrayListOf(),
    @SerializedName("meta" ) var meta : Meta?           = Meta()

)

data class Data (

    @SerializedName("id"       ) var id       : String,
    @SerializedName("name"     ) var name     : String,
    @SerializedName("username" ) var username : String

)

data class Meta (

    @SerializedName("result_count" ) var resultCount : Int? = null

)