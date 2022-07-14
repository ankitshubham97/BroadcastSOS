package com.example.broadcastsos.services.twitter.rest.models

import com.google.gson.annotations.SerializedName


data class GetTweetsResponseModel (

    @SerializedName("data" ) var data : ArrayList<GetTweetsResponseModelData> = arrayListOf(),
    @SerializedName("meta" ) var meta : GetTweetsResponseModelMeta?           = GetTweetsResponseModelMeta()

)

data class Mentions (

    @SerializedName("start"    ) var start    : Int?    = null,
    @SerializedName("end"      ) var end      : Int?    = null,
    @SerializedName("username" ) var username : String? = null

)
data class Entities (

    @SerializedName("mentions" ) var mentions : ArrayList<Mentions> = arrayListOf()

)
data class ReferencedTweets (

    @SerializedName("type" ) var type : String? = null,
    @SerializedName("id"   ) var id   : String? = null

)
data class GetTweetsResponseModelData (

    @SerializedName("author_id"          ) var authorId          : String?                     = null,
    @SerializedName("created_at"         ) var createdAt         : String?                     = null,
    @SerializedName("entities"           ) var entities          : Entities?                   = Entities(),
    @SerializedName("id"                 ) var id                : String?                     = null,
    @SerializedName("lang"               ) var lang              : String?                     = null,
    @SerializedName("possibly_sensitive" ) var possiblySensitive : Boolean?                    = null,
    @SerializedName("referenced_tweets"  ) var referencedTweets  : ArrayList<ReferencedTweets> = arrayListOf(),
    @SerializedName("source"             ) var source            : String?                     = null,
    @SerializedName("text"               ) var text              : String?                     = null

)

data class GetTweetsResponseModelMeta (

    @SerializedName("newest_id"    ) var newestId    : String? = null,
    @SerializedName("oldest_id"    ) var oldestId    : String? = null,
    @SerializedName("result_count" ) var resultCount : Int?    = null,
    @SerializedName("next_token"   ) var nextToken   : String? = null

)