package com.solvind.skycams.app.core

import androidx.lifecycle.LiveData

interface IExposeNetworkStateLiveData {
    val internetConnectionType: LiveData<InternetConnection>
}


sealed class InternetConnection {
    object Connected: InternetConnection()
    object NotConnected: InternetConnection()
}
