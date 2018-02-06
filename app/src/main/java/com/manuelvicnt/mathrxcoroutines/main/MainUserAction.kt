package com.manuelvicnt.mathrxcoroutines.main

sealed class MainUserAction {
    class Calculate(val number: Long) : MainUserAction()
    class FunFactEnabled(val enabled: Boolean) : MainUserAction()
}
