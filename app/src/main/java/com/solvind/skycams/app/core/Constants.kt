package com.solvind.skycams.app.core

/**
 * Used to notify the user of that the service is running in the foreground.
 * */
const val FOREGROUND_NOTIFICATIONS_CHANNEl_ID = "foreground_notification"

/**
 * Used to alarm the user about northern lights.
 * */
const val ALARM_NOTIFICATIONS_CHANNEL_ID = "alarm_notification"

/**
 * The ID of the foreground notification displayed by the alarm service while it is listening
 * to the user's activated alarms
 * */
const val FOREGROUND_NOTIFICATION_ID = 1

/**
 * Used by the service to handle when the user clicks the clear all alarms button on the
 * foreground notification displayed by the service
 * */
const val SERVICE_CANCEL_ALL_ALARM_INTENT_ACTION = "clear_all_alarms"
const val SERVICE_CANCEL_ALL_ALARMS_REQUEST_CODE = 1
/**
 * Used by the reactivate alarm button on a alarm notification to tell the service to
 * reactivate the alarm that was set off.
 * */
const val SERVICE_REACTIVATE_ALARM_INTENT_ACTION = "reactivate_alarm"
const val SERVICE_REACTIVATE_ALARM_REQUEST_CODE = 2

/**
 * Used by the foreground notification to open the main activity without any navigation requests
 * */
const val ACTIVITY_OPEN_DESTINATION_HOME = 3

/**
 * Used by the alarm notification to send the user straight to a detailed fragment of the alarm picture
 * */
const val ACTIVITY_WITH_EXTRAS_KEY_STORAGE_LOCATION_REQUEST_CODE = 4
const val ACTIVITY_INTENT_EXTRAS_STORAGE_LOCATION_KEY = "storage_location"
const val INTENT_EXTRA_SKYCAMKEY = "skycamKey"