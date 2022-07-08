package com.example.myapplication

import retrofit2.Response
import retrofit2.http.GET

interface  WordService {
    /*
    {
  "code":0,
  "msg":"ok",
  "data":[
    {
      "id":1,
      "word":"天道酬勤"
    },
    {
      "id":2,
      "word":"moli"
    },{
        "id":3,
        "word": "陌离君"
    }
  ]
}
     */
    @GET("git/raw/master/WebUri.json")
    suspend fun getWords(): Response<Words>
}