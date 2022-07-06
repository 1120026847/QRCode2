package com.example.myapplication

import com.google.gson.annotations.SerializedName

data class data(
    @SerializedName("id")
    val id: Int,
    @SerializedName("word")
    val word: String
)
data class  Words(
    @SerializedName("msg")
    val msg: String,
    @SerializedName("data")
    val data:List<data>
)