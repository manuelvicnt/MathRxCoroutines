package com.manuelvicnt.mathrxcoroutines.number

import android.accounts.NetworkErrorException
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.GET
import retrofit2.http.Path

class NumbersApiService(private val numbersApi: NumbersApiInterface) {

    fun getNumberFunFact(number: Long): Single<String> {
        return numbersApi.getFunFact(number)
                .map {
                    if (it.isSuccessful) {
                        it.body()?.string() ?: throw NetworkErrorException("Error happened")
                    } else {
                        throw NetworkErrorException("Error happened")
                    }
                }
    }
}

object NumbersApiHelper {

    private val retrofit = Retrofit.Builder()
            .baseUrl("http://numbersapi.com")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

    val numbersApi: NumbersApiInterface
        get() = retrofit.create(NumbersApiInterface::class.java)
}

interface NumbersApiInterface {

    @GET("/{number}")
    fun getFunFact(@Path("number") number: Long): Single<Response<ResponseBody>>

}