package com.example.broadcastsos.services.twitter.rest.models

import com.google.gson.annotations.SerializedName

data class CreateTweetResponseModel(
    @SerializedName("data" ) var data: CreateTweetResponseModelData = CreateTweetResponseModelData()
)

data class CreateTweetResponseModelData (
    @SerializedName("id"   ) var id   : String? = null,
    @SerializedName("text" ) var text : String? = null
)