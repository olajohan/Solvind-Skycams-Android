package com.solvind.skycams.app

/**
 * - Knows how to start and stop listening to updates from skycams
 * - Displays a foreground notification with all the skycams that is currently being monitored
 * - Alert the user with a notification if any of the skycams indicate that there is aurora
 *
 * */
interface IAlarmService {

    fun startListeningToUpdatesFromSkycam(skycamKey: String)
    fun stopListeningToUpdatesFromSkycam(skycamKey: String)

}