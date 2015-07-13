INTRO
======

This project contains a Cordova plugin for the GoogleFit platform.


Caveats
-------

At this stage (version 0.1.0) the plugin is still in a quite early stage of development and is provided "AS IS".
It works only to read certain data from the GoogleFit API and is not yet ready to store any data.


Content
------

Here the breakdown of the content

- plugin.xml  (The Manifest file for the plugin)
- src/
   - android/
      - <Java source code>  (The native Android Java code)
- www/
   - <JavaScript interface> (The connector code)



Using the plugin
------

To use the plugin you need to call the specific functions

window.plugins.googlefit.getStuff1(
                  1435708800000,    // Start time in milliseconds
                  1436368288000,    // Start time in milliseconds
                  datatypes,        // Datatypes under the URL format specified by GoogleFit
                  function(data) {
                    // Success callback. The data object is a JSON that follows
                    // the structure of GoogleFit data structures
                  },
                  function(e) {
                    // The error e is returned in case of problems with the query
                  });


window.plugins.googlefit.getStuff2(
                  1435708800000,    // Start time in milliseconds
                  1436368288000,    // Start time in milliseconds
                  datatypes,        // Datatypes under the URL format specified by GoogleFit
                  datatypes,        // Datatypes under the URL format specified by GoogleFit
                  1,                // Duration value of the databucket
                  "DAYS",           // TimeUnit that quantify the duration unit (DAYS, HOURS, MINUTES, SECONDS)
                  0,                // Type of the Buckets (0: ByTime, 1: ByActivityType, 2: ByActivitySegment)
                  function(data) {
                    // Success callback. The data object is a JSON that follows
                    // the structure of GoogleFit data structures
                  },
                  function(e) {
                    // The error e is returned in case of problems with the query
                  });

Valid DataTypes
-------

At the moment the datatypes that are readable from the GoogleFit API are:

DataType.TYPE_STEP_COUNT_DELTA
DataType.AGGREGATE_STEP_COUNT_DELTA
DataType.TYPE_CALORIES_EXPENDED
DataType.AGGREGATE_CALORIES_EXPENDED
DataType.TYPE_DISTANCE_DELTA
DataType.AGGREGATE_DISTANCE_DELTA
DataType.TYPE_HEIGHT
DataType.TYPE_ACTIVITY_SAMPLE
DataType.TYPE_ACTIVITY_SEGMENT
DataType.AGGREGATE_ACTIVITY_SUMMARY
DataType.TYPE_WEIGHT
DataType.TYPE_HEART_RATE_BPM
DataType.TYPE_POWER_SAMPLE
DataType.AGGREGATE_POWER_SUMMARY
DataType.TYPE_CYCLING_PEDALING_CADENCE
DataType.TYPE_CYCLING_WHEEL_REVOLUTION
DataType.TYPE_LOCATION_SAMPLE

use them by placing the corresponding GoogleFit URL notation in the arrays passed to the GetStuff1 and GetStuff2 JavaScript calls.

The corresponding values are (as per: https://developers.google.com/fit/rest/v1/data-types):

com.google.step_count.delta
com.google.step_count.delta
com.google.calories.expended
com.google.calories.expended
com.google.distance.delta
com.google.distance.delta
com.google.height
com.google.activity.sample
com.google.activity.segment
com.google.activity.summary
com.google.weight
com.google.heart_rate.bpm
com.google.power.sample
com.google.power.summary
com.google.cycling.pedaling.cadence
com.google.cycling.wheel_revolution.rpm
com.google.location.sample


Online resources for GoogleFit
------

Data Types
https://developers.google.com/fit/android/data-types


Activity Types
https://developers.google.com/fit/rest/v1/reference/activity-types
