package com.manuelvicnt.mathrxcoroutines.main

import android.arch.lifecycle.ViewModel
import android.util.Log
import com.manuelvicnt.mathrxcoroutines.fibonacci.FibonacciProducer
import com.manuelvicnt.mathrxcoroutines.number.NumbersApiHelper
import com.manuelvicnt.mathrxcoroutines.number.NumbersApiService
import com.manuelvicnt.mathrxcoroutines.util.LogUtil.logMessage
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.cancelChildren
import kotlinx.coroutines.experimental.channels.ActorScope
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.rx2.await

class MainViewModel : ViewModel() {

    private val parentJob = Job()
    private val numbersApiService = NumbersApiService(NumbersApiHelper.numbersApi)
    private var askForFunFact = false

    // Can be replaced by LiveData
    val viewStateChannel = ConflatedBroadcastChannel<MainViewState>()

    val userActionActor = actor<MainUserAction>(CommonPool, parent = parentJob) {
        for (msg in channel) { // iterate over incoming messages
            when (msg) {
                is MainUserAction.Calculate -> {
                    if (msg.number <= 0) {
                        viewStateChannel.offer(MainViewState.WrongInputError)
                    } else {
                        viewStateChannel.offer(MainViewState.Loading)
                        processCalculation(msg)
                    }
                }
                is MainUserAction.FunFactEnabled -> {
                    askForFunFact = msg.enabled
                }
            }
        }
    }

    override fun onCleared() {
        logMessage("onCleared")
        viewStateChannel.close()
        parentJob.cancelChildren()
        super.onCleared()
    }

    private suspend fun ActorScope<MainUserAction>.processCalculation(msg: MainUserAction.Calculate) {

        // This is executed in the context of the Actor, i.e. CommonPool
        if (askForFunFact) {
            try {
                val (fibonacci, funFact) = Single.zip(FibonacciProducer.fibonacci(msg.number),
                        numbersApiService.getNumberFunFact(msg.number),
                        BiFunction<Long, String, Pair<Long, String>> { fibonacci, funFact -> Pair(fibonacci, funFact) })
                        .await()
                logMessage("Successful response: $fibonacci & $funFact")
                viewStateChannel.offer(MainViewState.Rendered(fibonacci, funFact))
            } catch (e: Exception) {
                logMessage("Failure $e")
                viewStateChannel.offer(MainViewState.RequestError)
            }
        } else {
            try {
                val fibonacci = FibonacciProducer.fibonacci(msg.number)
                        .await()
                logMessage("Calculation happened: $fibonacci")
                viewStateChannel.offer(MainViewState.Rendered(fibonacci, ""))
            } catch (e: Exception) {
                logMessage("Failure $e")
                viewStateChannel.offer(MainViewState.RequestError)
            }
        }
    }
}