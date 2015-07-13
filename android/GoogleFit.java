package uk.ac.ox.ibme.cordova.plugins;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * This sample demonstrates how to use the History API of the Google Fit platform to insert data,
 * query against existing data, and remove data. It also demonstrates how to authenticate
 * a user with Google Play Services and how to properly represent data in a {@link DataSet}.
 */
public class GoogleFit extends CordovaPlugin {
    public static final String TAG = "cv-pl-googlefit";
    public static final int REQUEST_OAUTH = 1;
    private static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";

    /**
     *  Track whether an authorization activity is stacking over the current activity, i.e. when
     *  a known auth error is being resolved, such as showing the account chooser or presenting a
     *  consent dialog. This avoids common duplications as might happen on screen rotations, etc.
     */
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    public GoogleApiClient mClient = null;
    private CallbackContext savedCallbackContext;

    private Activity actContext;
    private Context appContext;

    public static final String STATUS_CONNECTED = "isConnected";
    public static final String STATUS_NOT_CONNECTED = "isNotConnected";

    private String status;


    private HashMap<String, DataType> DataTypeLookup;
    private HashMap<DataType, String> DataTypeRLookup;

    private HashMap<String, TimeUnit> TimeUnitLookup;
    private HashMap<TimeUnit, String> TimeUnitRLookup;

    private void fillDataTypes(DataType t) {
        DataTypeLookup.put(t.getName(), t);
        DataTypeRLookup.put(t, t.getName());
    }

    private void fillTimeUnit(TimeUnit t) {
        TimeUnitLookup.put(t.name(), t);
        TimeUnitRLookup.put(t, t.name());
    }


    private static DataType AllData[] = {DataType.TYPE_STEP_COUNT_DELTA,
    DataType.AGGREGATE_STEP_COUNT_DELTA,
    DataType.AGGREGATE_CALORIES_EXPENDED,
    DataType.TYPE_CALORIES_EXPENDED,
    DataType.AGGREGATE_DISTANCE_DELTA,
    DataType.TYPE_DISTANCE_DELTA,
    DataType.TYPE_HEIGHT,
    DataType.TYPE_ACTIVITY_SAMPLE,
    DataType.TYPE_ACTIVITY_SEGMENT,
    DataType.AGGREGATE_ACTIVITY_SUMMARY,
    DataType.TYPE_WEIGHT,
    DataType.TYPE_HEART_RATE_BPM,
    DataType.TYPE_POWER_SAMPLE,
    DataType.AGGREGATE_POWER_SUMMARY,
    DataType.TYPE_CYCLING_PEDALING_CADENCE,
    DataType.TYPE_CYCLING_WHEEL_REVOLUTION,
    DataType.TYPE_LOCATION_SAMPLE};


    private void initDataTypes() {
        DataTypeLookup = new HashMap<String, DataType>();
        DataTypeRLookup = new HashMap<DataType, String>();
        TimeUnitLookup = new HashMap<String, TimeUnit>();
        TimeUnitRLookup = new HashMap<TimeUnit, String>();

        for(DataType d : AllData) {
            fillDataTypes(d);
        }

        fillTimeUnit(TimeUnit.DAYS);
        fillTimeUnit(TimeUnit.HOURS);
        fillTimeUnit(TimeUnit.MINUTES);
        fillTimeUnit(TimeUnit.SECONDS);
        fillTimeUnit(TimeUnit.MILLISECONDS);

    }




    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        actContext = cordova.getActivity();
        appContext = actContext.getApplicationContext();

        initDataTypes();

        cordova.setActivityResultCallback(this);


        buildFitnessClient();
    }


    /**
     *  Build a {@link GoogleApiClient} that will authenticate the user and allow the application
     *  to connect to Fitness APIs. The scopes included should match the scopes your app needs
     *  (see documentation for details). Authentication will occasionally fail intentionally,
     *  and in those cases, there will be a known resolution, which the OnConnectionFailedListener()
     *  can address. Examples of this include the user never having signed in before, or
     *  having multiple accounts on the device and needing to specify which account to use, etc.
     */
    private void buildFitnessClient() {

        status = STATUS_NOT_CONNECTED;

        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(appContext)
            .addApi(Fitness.HISTORY_API)
            .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
            .addConnectionCallbacks(
                new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.i(TAG, "Connected!!!");
                        // Now you can make calls to the Fitness APIs.  What to do?
                        // Look at some data!!
                        status = STATUS_CONNECTED;
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        // If your connection to the sensor gets lost at some point,
                        // you'll be able to determine the reason and react to it here.
                        status = STATUS_NOT_CONNECTED;

                        if (i == ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                            Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                        } else if (i == ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                            Log.i(TAG, "Connection lost.  Reason: Service Disconnected");
                        }
                    }
                }
            )
            .addOnConnectionFailedListener(
                new GoogleApiClient.OnConnectionFailedListener() {
                    // Called whenever the API client fails to connect.
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.i(TAG, "Connection failed. Cause: " + result.toString());

                        status = STATUS_NOT_CONNECTED;

                        if (!result.hasResolution()) {
                            // Show the localized error dialog
                            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), actContext, 0).show();
                            return;
                        }
                        // The failure has a resolution. Resolve it.
                        // Called typically when the app is not yet authorized, and an
                        // authorization dialog is displayed to the user.
                        if (!authInProgress) {
                            try {
                                Log.i(TAG, "Attempting to resolve failed connection");
                                authInProgress = true;
                                result.startResolutionForResult(actContext, REQUEST_OAUTH);
                            } catch (IntentSender.SendIntentException e) {
                                Log.e(TAG, "Exception while starting resolution activity", e);
                            }
                        }
                    }
                }
            )
            .build();
    }


    @Override
    public void onStart() {
        super.onStart();
        // Connect to the Fitness API
        Log.i(TAG, "Connecting to GoogleFit...");
        mClient.connect();
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mClient.isConnected()) {
            mClient.disconnect();
        }
    }

    /**
     * Converts the types descriptors into a List<DataType>
     * @param types the type names with inverse URL notation, e.g. com.google.activity.summary
     * @return the datatypes as List<DataType>
     */
    public List<DataType> JSON2DataType(JSONArray types) {
        ArrayList<DataType> list = new ArrayList<DataType>();

        try {
            for(int i=0; i< types.length(); i++) {
                DataType t = DataTypeLookup.get(types.getString(i));
                if (t!=null) {
                    list.add(t);
                }
            }
        } catch (JSONException e) { e.printStackTrace();
        }

        return list;
    }

    /**
     * Converts the time unit descriptor into TimeUnit type
     * @param t the type names with Java notation: "DAYS", "HOURS", "MINUTES", "SECONDS"
     * @return the TimeUnit
     */
    public TimeUnit JSON2TimeUnit(String t) {

        TimeUnit type = TimeUnitLookup.get(t);

        return type;
    }


    /**
     * The "execute" method that Cordova calls whenever the plugin is used from the JavaScript
     * @param action          The action to execute.
     * @param args            The exec() arguments.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return
     * @throws JSONException
     */
    @Override
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {

        // Select the getStuff2: get Buckets+Datasets+Datapoints from GoogleFit according to the query parameters
        if ("getStuff2".equals(action)) {
            long st = args.getJSONObject(0).getLong("startTime");
            long et = args.getJSONObject(0).getLong("endTime");
            List<DataType> dt = JSON2DataType(args.getJSONObject(0).getJSONArray("datatypes"));
            List<DataType> dta = JSON2DataType(args.getJSONObject(0).getJSONArray("dataaggregations"));
            int du = args.getJSONObject(0).getInt("durationBucket");
            TimeUnit tu = JSON2TimeUnit(args.getJSONObject(0).getString("timeUnitBucket"));
            int tb = args.getJSONObject(0).getInt("typeBucket");

            cordova.getThreadPool().execute(new GetStuff(queryDataWithBuckets(st, et, dt, dta, du, tu, tb), callbackContext));
        }

        // Select the getStuff1: get Datasets+Datapoints from GoogleFit according to the query parameters
        if ("getStuff1".equals(action)) {

            long st = args.getJSONObject(0).getLong("startTime");
            long et = args.getJSONObject(0).getLong("endTime");
            JSONArray _dt = args.getJSONObject(0).getJSONArray("datatypes");
            List<DataType> dt = JSON2DataType(_dt);

            cordova.getThreadPool().execute( new GetStuff(queryData(st, et, dt), callbackContext));
        }

        return true;  // Returning false will result in a "MethodNotFound" error.
    }

    /**
     * Handles the registration to the GoogleFit API
     * @param requestCode   The request code originally supplied to startActivityForResult(),
     *                      allowing you to identify who this result came from.
     * @param resultCode    The integer result code returned by the child activity through its setResult().
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            if (resultCode == Activity.RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mClient.isConnecting() && !mClient.isConnected()) {
                    mClient.connect();
                }
            }
        }
    }


    /**
     * The GetStuff thread that execute the given query and calls the right callback (fail/success)
     */
    private class GetStuff extends Thread {
        private DataReadRequest request;
        private CallbackContext callbackContext;

        public GetStuff(DataReadRequest request, final CallbackContext callbackContext) {
            this.request = request;
            this.callbackContext = callbackContext;
        }

        @Override
        public void run() {
            if (!mClient.isConnected()) {
                mClient.connect();
            }

            Fitness.HistoryApi.readData(mClient, request)
                .setResultCallback(new ResultCallback<DataReadResult>() {
                    @Override
                    public void onResult(DataReadResult dataReadResult) {

                        if (dataReadResult.getBuckets().size() > 0) {
                            callbackContext.success(convertBucketsToJson(dataReadResult.getBuckets())); // Thread-safe.
                        } else if (dataReadResult.getDataSets().size() > 0) {
                            callbackContext.success(convertDatasetsToJson(dataReadResult.getDataSets())); // Thread-safe.
                        } else {
                            callbackContext.error("No dataset and no buckets."); // Thread-safe.
                        }
                    }
                });
        }
    }



    /**
     * Return a {@link DataReadRequest} for all step count changes in the past week.
     */
    private DataReadRequest queryData(long startTime, long endTime, List<DataType> types) {


        DataReadRequest.Builder builder = new DataReadRequest.Builder();

        for (DataType dt : types) {
            builder.read(dt);
        }

        return builder.setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS).build();
    }


    /**
     * Return a {@link DataReadRequest} for all step count changes in the past week.
     */
    private DataReadRequest queryDataWithBuckets(long startTime, long endTime, List<DataType> types, List<DataType> aggregations, int duration, TimeUnit time, int bucketType) {

        DataReadRequest.Builder builder = new DataReadRequest.Builder();

        for (int i=0; i< types.size(); i++) {
            builder.aggregate(types.get(i), aggregations.get(i));
        }

        switch (bucketType) {
            case 2:
                builder.bucketByActivitySegment(duration, time);
                break;
            case 1:
                builder.bucketByActivityType(duration, time);
                break;
            case 0:
            default:
                builder.bucketByTime(duration, time);
                break;
        }

        return builder.setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS).build();
    }


    /**
     * Convert a DataSet into its JSON representation
     * @param dataSet
     * @return
     */
    private JSONArray convertDatasetToJson(DataSet dataSet) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        JSONArray dataSet_JSON = new JSONArray();

        for (DataPoint dp : dataSet.getDataPoints()) {
            JSONObject dataPoint_JSON = new JSONObject();

            try {
                dataPoint_JSON.put("type", dp.getDataType().getName());
                dataPoint_JSON.put("source", dp.getDataSource());
                dataPoint_JSON.put("start", dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
                dataPoint_JSON.put("end", dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));

                JSONArray field_value_pairs = new JSONArray();

                for(Field field : dp.getDataType().getFields()) {
                    JSONObject field_value_pair = new JSONObject();
                    field_value_pair.put("field", field.getName());
                    field_value_pair.put("value", dp.getValue(field));
                    field_value_pairs.put(field_value_pair);
                }
                dataPoint_JSON.put("fields", field_value_pairs);

                dataSet_JSON.put(dataPoint_JSON);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return dataSet_JSON;
    }


    /**
     * Convert a list of Datasets into its JSON representation
     * @param dataSets
     * @return
     */
    private JSONArray convertDatasetsToJson(List<DataSet> dataSets) {

        JSONArray bucketSet_JSON = new JSONArray();

        for (DataSet ds : dataSets) {
            bucketSet_JSON.put(convertDatasetToJson(ds));
        }

        return bucketSet_JSON;
    }


     /**
     * Convert a list of Buckets into its JSON representation
     * @param bucketSet
     * @return
     */
    private JSONArray convertBucketsToJson(List<Bucket> bucketSet) {

        JSONArray buckets = new JSONArray();

        for(Bucket b : bucketSet) {

            JSONObject b_ = new JSONObject();

            try {
                b_.put("start", b.getStartTime(TimeUnit.MILLISECONDS));
                b_.put("end", b.getEndTime(TimeUnit.MILLISECONDS));
                b_.put("type", b.getBucketType());
                b_.put("activity", b.getActivity());
                b_.put("datasets", convertDatasetsToJson(b.getDataSets()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            buckets.put(b_);
        }

        return buckets;
    }


}
