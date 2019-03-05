package de.digisocken.fatchat;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.github.data5tream.emojilib.EmojiEditText;
import com.github.data5tream.emojilib.EmojiGridView;
import com.github.data5tream.emojilib.EmojiPopup;
import com.github.data5tream.emojilib.emoji.Emojicon;

import java.util.ArrayList;

public class SendActivity extends AppCompatActivity {
    private SmsManager smsManager = SmsManager.getDefault();
    private ArrayList<String> parts;
    private ArrayList<PendingIntent> sentPIs;
    private ArrayList<PendingIntent> deliveredPIs;

    private static SharedPreferences mPreferences;
    private EditText addredit;
    private EmojiEditText emojiEditText;


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
                //ab.setDisplayUseLogoEnabled(true);
                //ab.setLogo(R.mipmap.ic_launcher);
                ab.setTitle("Send");
                ab.setElevation(10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        addredit = (EditText) findViewById(R.id.editAddr);
        emojiEditText = (EmojiEditText) findViewById(R.id.editMsg);
        addredit.setText(mPreferences.getString("phone", "").trim());
        final View rootView = findViewById(R.id.rootView);
        final ImageButton ebtn = (ImageButton) findViewById(R.id.emojiBtn);

        final EmojiPopup popup = new EmojiPopup(rootView, this, getResources().getColor(R.color.colorAccent));
        popup.setWidth(rootView.getMeasuredWidth());
        popup.setHeight(rootView.getMeasuredHeight() / 2);

        popup.setOnSoftKeyboardOpenCloseListener(new EmojiPopup.OnSoftKeyboardOpenCloseListener() {

            @Override
            public void onKeyboardOpen(int keyBoardHeight) { }

            @Override
            public void onKeyboardClose() {
                if (popup.isShowing()) popup.dismiss();
            }
        });

        popup.setOnEmojiconClickedListener(new EmojiGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                if (emojicon == null) return;
                emojiEditText.append(emojicon.getEmoji());
                popup.dismiss();
            }
        });

        popup.setOnEmojiconBackspaceClickedListener(new EmojiPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {
                KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                emojiEditText.dispatchKeyEvent(event);
            }
        });

        ebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popup.isShowing()) {
                    popup.dismiss();
                } else {
                    Rect rectgle= new Rect();
                    Window window= getWindow();
                    window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
                    int statusBarHeight = rectgle.top;
                    Point p = new Point();
                    ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(p);
                    Rect windowRect = new Rect();
                    rootView.getWindowVisibleDisplayFrame(windowRect);
                    int height = windowRect.height();
                    int width = windowRect.width();
                    popup.setHeight((p.y - statusBarHeight) - height);
                    popup.setWidth(width);
                    popup.showAtBottom();
                }
            }
        });
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

        parts = smsManager.divideMessage(emojiEditText.getText().toString());
        smsManager.sendMultipartTextMessage(phone, null, parts, sentPIs, deliveredPIs);
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }
}
