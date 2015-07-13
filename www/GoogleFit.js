function GoogleFit() {
}

GoogleFit.prototype.getStuff1 = function (startTime, endTime, datatypes, successCallback, failureCallback) {
  cordova.exec(successCallback,
               failureCallback,
               "GoogleFit",
               "getStuff1",
               [{
                 "startTime" : startTime,
                 "endTime" : endTime,
                 "datatypes": datatypes
               }]);
};


GoogleFit.prototype.getStuff2 = function (startTime, endTime, datatypes, dataaggregations, durationBucket, timeUnitBucket, typeBucket, successCallback, failureCallback) {
  cordova.exec(successCallback,
               failureCallback,
               "GoogleFit",
               "getStuff2",
               [{
                 "startTime" : startTime,
                 "endTime" : endTime,
                 "datatypes": datatypes,
                 "dataaggregations": dataaggregations,
                 "durationBucket": durationBucket,
                 "timeUnitBucket": timeUnitBucket,
                 "typeBucket": typeBucket
               }]);
};


GoogleFit.install = function () {
  if (!window.plugins) {
    window.plugins = {};
  }

  window.plugins.googlefit = new GoogleFit();
  return window.plugins.googlefit;
};

cordova.addConstructor(GoogleFit.install);
