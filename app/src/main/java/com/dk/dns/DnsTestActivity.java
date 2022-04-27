package com.dk.dns;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class DnsTestActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dns_test);
    }

    public void indoor(View view) {
        EditText deviceId = findViewById(R.id.deviceId);
        String id = deviceId.getEditableText().toString();

        DnsIndoorHelper indoorHelper = new DnsIndoorHelper();
        indoorHelper.init(getApplicationContext(), id);
    }

    public void outdoor(View view) {
        EditText deviceId = findViewById(R.id.deviceId);
        String id = deviceId.getEditableText().toString();

        DnsOutdoorHelper helper = new DnsOutdoorHelper();
        helper.init(getApplicationContext(), id);
    }
}