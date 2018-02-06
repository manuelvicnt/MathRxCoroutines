package com.manuelvicnt.mathrxcoroutines.fibonacci

import io.reactivex.Single

object FibonacciProducer {

    fun fibonacci(number: Long): Single<Long> =
            Single.create({ emitter ->
                val result = fib(number)
                emitter.onSuccess(result)
            })

    private fun fib(n: Long): Long {
        return if (n <= 1)
            n
        else
            fib(n - 1) + fib(n - 2)
    }

}