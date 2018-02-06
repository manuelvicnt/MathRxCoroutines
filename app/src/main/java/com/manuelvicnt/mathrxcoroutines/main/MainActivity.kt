package com.manuelvicnt.mathrxcoroutines.main

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.manuelvicnt.mathrxcoroutines.R
import com.manuelvicnt.mathrxcoroutines.util.LogUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.consumeEach

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private val parentJob = Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        setupViews()
    }

    override fun onStart() {
        super.onStart()
        listenViewModel()
    }

    override fun onStop() {
        super.onStop()
        parentJob.cancelChildren()
    }

    private fun setupViews() {
        calcButton.setOnClickListener {
            runBlocking {
                LogUtil.logMessage("Calc Button Pressed")
                viewModel.userActionActor.send(MainUserAction.Calculate(input.text.toString().toLong()))
            }
        }

        funFact.setOnCheckedChangeListener { _, isChecked ->
            runBlocking {
                LogUtil.logMessage("Fun Fact $isChecked")
                viewModel.userActionActor.send(MainUserAction.FunFactEnabled(isChecked))
            }
        }
    }

    private fun listenViewModel() {
        // Launch on the CommonPool to not block the MainThread
        launch(parentJob + CommonPool) {
            viewModel.viewStateChannel.consumeEach {
                withContext(UI) {
                    LogUtil.logMessage("Processing $it")
                    when (it) {
                        MainViewState.Loading -> {
                            progressBar.visibility = View.VISIBLE
                            result.text = "Loading..."
                            funFactText.text = ""
                        }
                        is MainViewState.Rendered -> {
                            progressBar.visibility = View.GONE
                            result.text = "Fibonacci = ${it.fibonacciNumber.toInt()}"
                            funFactText.text = "${it.funFact}"
                        }
                        MainViewState.WrongInputError -> {
                            showError()
                        }
                        MainViewState.RequestError -> {
                            showError()
                        }
                    }
                }
            }
        }
    }

    private fun showError() {
        progressBar.visibility = View.GONE
        result.text = "Something happened when calculating your input! Sorry!"
        funFactText.text = ""
    }
}
