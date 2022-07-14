package com.example.broadcastsos.services.twitter.rest.models

import com.google.gson.annotations.SerializedName

data class GetMeResponseModel (

    @SerializedName("data" ) var data : GetMeResponseModelData? = GetMeResponseModelData()

)

data class GetMeResponseModelData (

    @SerializedName("name"              ) var name            : String? = null,
    @SerializedName("profile_image_url" ) var profileImageUrl : String? = null,
    @SerializedName("username"          ) var username        : String? = null,
    @SerializedName("id"                ) var id              : String? = null

)