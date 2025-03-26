package com.hacaller.farmbeacon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.BleSignal;
import com.google.android.gms.nearby.messages.Distance;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageFilter;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeOptions;
import com.hacaller.farmbeacon.models.AdvertisedId;
import com.hacaller.farmbeacon.models.Attachment;
import com.hacaller.farmbeacon.models.Beacon;
import com.hacaller.farmbeacon.models.Observation;
import com.hacaller.farmbeacon.models.ProximityRequest;
import com.hacaller.farmbeacon.models.ProximityResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class BeaconActivity extends AppCompatActivity {

    public final String TAG = getClass().getSimpleName();
    public final String IBEACONSIM = "4f46707a-b447-4382-b3dc-f790a5bf9fed";
    public final String IBEACON = "fda50693-a4e2-4fb1-afcf-c6eb07647825";
    public final String NAMESPACE = "androidapidemo-209607";
    public final String BEACON_ID = "/aUGk6TiT7Gvz8brB2R4JQAAUAA=";

    private BluetoothAdapter mBluetoothAdapter;
    BluetoothLeScanner bluetoothLeScanner;

    TextView txtNotification;
    ImageView imgNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon);

        txtNotification = findViewById(R.id.txtNotification);
        imgNotification = findViewById(R.id.imgNotification);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            bluetoothLeScanner.startScan(myScanCallback);
        }

    }

    MessageListener mMessageListener = new MessageListener() {
        @Override
        public void onFound(Message message) {
            Log.println(Log.ASSERT, TAG, "Message found: " + message);
            imgNotification.setImageResource(R.drawable.plums1);
            if (!message.getNamespace().equals(NAMESPACE)){
                new ProximityAsyncTask().execute();
            } else{
                txtNotification.setText(new String(message.getContent()));
            }
        }

        @Override
        public void onLost(Message message) {
            Log.println(Log.ASSERT, TAG, "Lost sight of message.");
            imgNotification.setImageResource(R.drawable.farm);
            txtNotification.setText("Welcome to my Farm! ");
        }

        @Override
        public void onDistanceChanged(Message message, Distance distance) {
            super.onDistanceChanged(message, distance);
            Log.println(Log.ASSERT, TAG, "Distance change message: " + distance.toString());
        }

        @Override
        public void onBleSignalChanged(Message message, BleSignal bleSignal) {
            super.onBleSignalChanged(message, bleSignal);
            Log.println(Log.ASSERT, TAG, "Ble signal message: " + bleSignal.getRssi());

        }
    };


    @Override
    public void onStart() {
        super.onStart();
        MessageFilter messageFilter = new MessageFilter.Builder()
                .includeIBeaconIds(UUID.fromString(IBEACON), null, null)
                //.includeEddystoneUids("fda50693c6eb07647825", "484552423031"/* any instance */)
                .includeNamespacedType(NAMESPACE, "string")
                .includeAllMyTypes()
                .build();
        SubscribeOptions options = new SubscribeOptions.Builder()
                .setStrategy(Strategy.DEFAULT)
                .setFilter(messageFilter)
                .build();
        Nearby.getMessagesClient(this).subscribe(mMessageListener, options);
    }

    @Override
    public void onStop() {
        Nearby.getMessagesClient(this).unsubscribe(mMessageListener);
        super.onStop();
    }

    ScanCallback myScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            //Log.println(Log.ASSERT, "BLE_TYPE", String.valueOf(result.getDevice().getType() == BluetoothDevice.DEVICE_TYPE_LE));
            StringBuilder builder = new StringBuilder();
            StringBuilder proxUUID = new StringBuilder();
            int i=0;
            for (byte b : result.getScanRecord().getBytes()) {
                if (i>8 && i<25){
                    proxUUID.append(String.format("%02X", b));
                }
                builder.append(String.format("%02X ", b));
                i++;
            }
            proxUUID.insert(20,"-");
            proxUUID.insert(16,"-");
            proxUUID.insert(12,"-");
            proxUUID.insert(8,"-");
            //Log.println(Log.ASSERT, "BLE_DATA", builder.toString());
            //Log.println(Log.ASSERT, "BLE_PROXUUID", proxUUID.toString());

        }
    };



    public interface ProximityService {
        @POST("https://proximitybeacon.googleapis.com/v1beta1/beaconinfo:getforobserved")
        Call<ProximityResponse> getBeacons(@Body ProximityRequest request, @Query("key") String apikey);
    }


    public class ProximityAsyncTask extends AsyncTask<Void,Void,ProximityResponse>{

        @Override
        protected ProximityResponse doInBackground(Void... voids) {

            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();


            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://proximitybeacon.googleapis.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();

            ProximityRequest request = new ProximityRequest();
            Observation observation = new Observation();
            ArrayList<Observation> observations = new ArrayList<>();
            observations.add(observation);
            request.setObservations(observations);
            AdvertisedId advertisedId = new AdvertisedId();
            advertisedId.setId(BEACON_ID);
            advertisedId.setType("IBEACON");
            observation.setAdvertisedId(advertisedId);
            ArrayList<String> namespacedTypes = new ArrayList<>();
            namespacedTypes.add(NAMESPACE+"/string");
            request.setNamespacedTypes(namespacedTypes);
            ProximityService service = retrofit.create(ProximityService.class);
            Call<ProximityResponse> beacons = service.getBeacons(request, getString(R.string.proximity_api_key));
            ProximityResponse response = null;
            try {
                response = beacons.execute().body();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(ProximityResponse proximityResponse) {
            super.onPostExecute(proximityResponse);
            if (proximityResponse != null) {
                for (Beacon beacon : proximityResponse.getBeacons()) {
                    if (beacon.getAdvertisedId().getId().equals("/aUGk6TiT7Gvz8brB2R4JQAAUAA=")) {
                        for (Attachment attachment : beacon.getAttachments()) {
                            if (attachment.getNamespacedType().equals(NAMESPACE+"/string")) {
                                txtNotification.setText(new String(Base64.decode(attachment.getData(), Base64.DEFAULT)));
                            }
                        }
                    }
                }
            }
        }
    }


}
