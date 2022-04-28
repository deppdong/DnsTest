package com.dk.dns;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class DnsTestActivity extends AppCompatActivity {
    private static final String TAG = "DnsTestActivity";
    DnsIndoorHelper indoorHelper ;
    DnsOutdoorHelper outdoorHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dns_test);

        SipCallManager.getInstance().init(this);
    }

    public void indoor(View view) {
        EditText deviceId = findViewById(R.id.deviceId);
        String id = deviceId.getEditableText().toString();

        indoorHelper = new DnsIndoorHelper();
        indoorHelper.init(getApplicationContext(), id);

        SipCallManager.getInstance().reg(indoorHelper.getServiceName(), NetworkUtils.getIP(this));

        AppCompatButton btn = findViewById(R.id.btn_indoor);
        btn.setText("已设置为室内机：" + id );

        findViewById(R.id.btn_outdoor).setEnabled(false);
    }

    public void outdoor(View view) {
        EditText deviceId = findViewById(R.id.deviceId);
        String id = deviceId.getEditableText().toString();

        outdoorHelper = new DnsOutdoorHelper();
        outdoorHelper.init(getApplicationContext(), id);

        SipCallManager.getInstance().reg(outdoorHelper.getServiceName(), NetworkUtils.getIP(this));

        findViewById(R.id.btn_indoor).setEnabled(false);
        AppCompatButton btn = findViewById(R.id.btn_outdoor);
        btn.setText("已设置为门口机：" + id );
    }

    public void dial(View view) {
        EditText deviceId = findViewById(R.id.sip_dial);
        String id = deviceId.getEditableText().toString();
        String ip = "";
        if (indoorHelper != null) {
            ip = indoorHelper.getOutdoorIp4(id);
        } else if (outdoorHelper!= null) {
                ip = outdoorHelper.getIndoorIp4(id);
        }
        
        if (TextUtils.isEmpty(ip)) {
            Log.d(TAG, "dial: 设备未联网");
            return ;
        }

        SipCallManager.getInstance().dial(id, ip);
    }

    public void accept(View view) {
        SipCallManager.getInstance().accept();
    }

    public void hangup(View view) {
        SipCallManager.getInstance().hangup();
    }
}