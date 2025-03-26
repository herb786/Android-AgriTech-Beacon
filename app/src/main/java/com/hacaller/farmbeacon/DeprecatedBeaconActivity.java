package com.hacaller.farmbeacon;

import android.Manifest;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.BleSignal;
import com.google.android.gms.nearby.messages.Distance;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.MessagesOptions;
import com.google.android.gms.nearby.messages.NearbyPermissions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;

import java.util.List;

/**
 * Created by Herbert Caller on 18/08/2018.
 */
public class DeprecatedBeaconActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public final String TAG = getClass().getSimpleName();
    private GoogleApiClient mGoogleApiClient;

    private static final String[] REQUIRED_PERMISSIONS =
            new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
            };

    MessageListener mMessageListener = new MessageListener() {
        @Override
        public void onFound(Message message) {
            Log.println(Log.ASSERT, TAG, "Found message: " + new String(message.getContent()));
            Log.println(Log.ASSERT, TAG, "Message found: " + message);
            Log.println(Log.ASSERT, TAG, "Message string: " + new String(message.getContent()));
            Log.println(Log.ASSERT, TAG, "Message namespaced type: " + message.getNamespace() +
                    "/" + message.getType());
        }

        @Override
        public void onLost(Message message) {
            Log.println(Log.ASSERT, TAG, "Lost sight of message: " + new String(message.getContent()));
        }

        @Override
        public void onDistanceChanged(Message message, Distance distance) {
            super.onDistanceChanged(message, distance);
            Log.println(Log.ASSERT, TAG, "Distance change message: " + new String(message.getContent()));
        }

        @Override
        public void onBleSignalChanged(Message message, BleSignal bleSignal) {
            super.onBleSignalChanged(message, bleSignal);
            Log.println(Log.ASSERT, TAG, "Ble signal message: " + new String(message.getContent()));
        }
    };

    TextView txtNotification;
    ImageView imgNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon);

        txtNotification = findViewById(R.id.txtNotification);
        imgNotification = findViewById(R.id.imgNotification);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE NOT SUPPORTED", Toast.LENGTH_SHORT).show();
            finish();
        }

        buildGoogleApiClient();

    }

    @Override
    public void onStart() {
        super.onStart();
        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this,REQUIRED_PERMISSIONS, 1492);
        } else {
            /*
            MessagesOptions messagesOptions = new MessagesOptions.Builder()
                    .setPermissions(NearbyPermissions.BLE)
                    .build();
            SubscribeOptions options = new SubscribeOptions.Builder()
                    .setStrategy(Strategy.BLE_ONLY)
                    .build();
            Nearby.getMessagesClient(this, messagesOptions).subscribe(mMessageListener, options);
            */
        }

    }

    @Override
    public void onStop() {
        Nearby.getMessagesClient(this).unsubscribe(mMessageListener);
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 998 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            /*
            MessagesOptions messagesOptions = new MessagesOptions.Builder()
                    .setPermissions(NearbyPermissions.BLE)
                    .build();
            SubscribeOptions options = new SubscribeOptions.Builder()
                    .setStrategy(Strategy.BLE_ONLY)
                    .build();
            Nearby.getMessagesClient(this, messagesOptions).subscribe(mMessageListener, options);
            */
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void buildGoogleApiClient() {
        if (mGoogleApiClient != null) {
            return;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .build();
    }

    private static final int TTL_IN_SECONDS = 3 * 60;
    private static final Strategy PUB_SUB_STRATEGY = new Strategy.Builder()
            .setTtlSeconds(TTL_IN_SECONDS).build();

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        SubscribeOptions options = new SubscribeOptions.Builder()
                .setStrategy(PUB_SUB_STRATEGY)
                .setCallback(new SubscribeCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                        Log.println(Log.ASSERT, TAG, "No longer subscribing");
                    }
                }).build();

        Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener, options)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.println(Log.ASSERT, TAG, "Subscribed successfully.");
                        }
                    }
                });
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.println(Log.ASSERT, TAG, "Connection Suspended.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.println(Log.ASSERT, TAG, "Connection Failed.");
    }
}
