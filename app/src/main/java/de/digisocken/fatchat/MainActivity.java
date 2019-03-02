package de.digisocken.fatchat;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private TextView dataview;
    private ArrayList<HmSms> smsArrayList;
    static final int READLIMIT = 50;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_read) {
            readAll();
            return true;
        } else if (id == R.id.action_send) {
            Intent myIntent = new Intent(MainActivity.this, SendActivity.class);
            startActivity(myIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataview = (TextView) findViewById(R.id.data);

        try {
            ActionBar ab = getSupportActionBar();
            if(ab != null) {
                ab.setDisplayShowHomeEnabled(true);
                ab.setHomeButtonEnabled(true);
                ab.setDisplayUseLogoEnabled(true);
                ab.setLogo(R.mipmap.ic_launcher);
                ab.setTitle(" " + getString(R.string.app_name));
                ab.setElevation(10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        readAll();
    }

    public void readAll() {
        smsArrayList = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Cursor cu = contentResolver.query(
                Uri.parse("content://sms/inbox"),
                null, null, null, null
        );
        int indexBody = cu.getColumnIndex("body");
        int indexAddr = cu.getColumnIndex("address");
        int indexDate = cu.getColumnIndex("date");
        //for (String s : cu.getColumnNames()){ Log.d("COLUMN_NAME", s); }

        cu.moveToFirst();
        int smscount = 0;
        do {
            HmSms sms = new HmSms();
            sms.t = cu.getLong(indexDate);
            sms.addr = cu.getString(indexAddr);
            sms.date = new Date(sms.t);
            sms.body = cu.getString(indexBody);
            sms.person = getContactName(getApplicationContext(), cu.getString(indexAddr));
            sms.datestr = DateFormat.format(getString(R.string.date_format), sms.date).toString();
            sms.me = false;
            smsArrayList.add(sms);
            smscount++;

        } while (cu.moveToNext() && smscount<READLIMIT);


        cu = contentResolver.query(
                Uri.parse("content://sms/sent"),
                null, null, null, null
        );
        cu.moveToFirst();
        smscount = 0;
        do {
            HmSms sms = new HmSms();
            sms.t = cu.getLong(indexDate);
            sms.addr = cu.getString(indexAddr);
            sms.date = new Date(sms.t);
            sms.body = cu.getString(indexBody);
            sms.person = getContactName(getApplicationContext(), cu.getString(indexAddr));
            sms.datestr = DateFormat.format(getString(R.string.date_format), sms.date).toString();
            sms.me = true;
            smsArrayList.add(sms);
            smscount++;

        } while (cu.moveToNext() && smscount<READLIMIT);

        Collections.sort(smsArrayList, new Comparator<HmSms>() {
            @Override
            public int compare(HmSms s1, HmSms s2) {
                return (int) (s2.t - s1.t);
            }
        });

        StringBuilder sb = new StringBuilder();
        String me = "";
        for (HmSms sms : smsArrayList) {
            me = sms.me == true ? ">  ": "";
            sb.append(me + sms.addr);
            sb.append("\n");
            sb.append(me + sms.person);
            sb.append("\n");
            sb.append(me + sms.datestr);
            sb.append("\n");
            sb.append(sms.body);
            sb.append("\n\n");
        }
        dataview.setText(sb.toString());
    }

    public String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri,
                new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return contactName;
    }
}
