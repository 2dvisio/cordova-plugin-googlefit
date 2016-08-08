function GoogleFit() {
}

GoogleFit.prototype.getData = function (startTime, endTime, datatypes, successCallback, failureCallback) {
  cordova.exec(successCallback,
               failureCallback,
               "GoogleFit",
               "getData",
               [{
                 "startTime" : startTime,
                 "endTime" : endTime,
                 "datatypes": datatypes
               }]);
};


GoogleFit.prototype.getAggregateData = function (startTime, endTime, datatypes, dataaggregations, durationBucket, timeUnitBucket, typeBucket, successCallback, failureCallback) {
  cordova.exec(successCallback,
               failureCallback,
               "GoogleFit",
               "getAggregateData",
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
