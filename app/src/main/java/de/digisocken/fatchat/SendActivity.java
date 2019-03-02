package de.digisocken.fatchat;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

public class SendActivity extends AppCompatActivity {
    private SmsManager smsManager = SmsManager.getDefault();
    private ArrayList<String> parts;
    private ArrayList<PendingIntent> sentPIs;
    private ArrayList<PendingIntent> deliveredPIs;

    private static SharedPreferences mPreferences;
    private EditText addredit,msgedit;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        try {
            ActionBar ab = getSupportActionBar();
            if(ab != null) {
                ab.setDisplayShowHomeEnabled(true);
                ab.setHomeButtonEnabled(true);
                ab.setDisplayUseLogoEnabled(true);
                ab.setLogo(R.mipmap.ic_launcher);
                ab.setTitle(" Send");
                ab.setElevation(10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        addredit = (EditText) findViewById(R.id.editAddr);
        msgedit  = (EditText) findViewById(R.id.editMsg);
        addredit.setText(mPreferences.getString("phone", "").trim());
    }

    public void cancelIt(View view) {
        mPreferences.edit().putString("phone", addredit.getText().toString()).apply();
        NavUtils.navigateUpFromSameTask(this);
    }

    public void sendIt(View view) {
        Button btn = (Button) findViewById(R.id.send);
        btn.setEnabled(false);
        String phone = addredit.getText().toString().trim();
        mPreferences.edit().putString("phone", phone).apply();
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        sentPIs = new ArrayList<>();
        deliveredPIs = new ArrayList<>();
        sentPIs.add(sentPI);
        deliveredPIs.add(deliveredPI);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        NavUtils.navigateUpFromSameTask(SendActivity.this);
                        break;
                    case Activity.RESULT_CANCELED:
                        Button btn = (Button) findViewById(R.id.send);
                        btn.setEnabled(true);
                        btn.setText("failed");
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        parts = smsManager.divideMessage(msgedit.getText().toString());
        smsManager.sendMultipartTextMessage(phone, null, parts, sentPIs, deliveredPIs);
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }
}
