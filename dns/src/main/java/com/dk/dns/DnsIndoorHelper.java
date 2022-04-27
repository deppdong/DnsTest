package com.dk.dns;

import static com.dk.dns.DnsOutdoorHelper.DOMAIN_OUTDOOR;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.github.druk.rx2dnssd.BonjourService;
import com.github.druk.rx2dnssd.Rx2Dnssd;
import com.github.druk.rx2dnssd.Rx2DnssdBindable;
import com.github.druk.rx2dnssd.Rx2DnssdEmbedded;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DnsIndoorHelper {
    private static final String TAG = "DnsIndoor";
    public final static String REG_TYPE_DNS = "_rxdnssd._tcp";
//    public final static String REG_TYPE_HTTP = "_http._tcp";
    public final static String DOMAIN_INDOOR  = "local.";

    private Rx2Dnssd rxdnssd;
    private BonjourService selfService;

    private List<BonjourService> outdoorService = new ArrayList<>();

    public void init(Context context, String indoorId) {
        if (selfService != null) {
            Log.w(TAG, "init: registered " + selfService.getServiceName()  + " vs " + indoorId);
            return;
        }

        rxdnssd = new Rx2DnssdBindable(context);
        selfService = new BonjourService.Builder(0, 0, "room."+ indoorId, REG_TYPE_DNS, DOMAIN_INDOOR)
                .hostname(indoorId)
                .port(1234)
                .build();
        Log.i(TAG, "init: " + selfService);

        // 注册设备到域
        rxdnssd.register(selfService)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bonjourService -> {
                    Log.i("TAG", "Register successfully " + bonjourService.toString());
                }, throwable -> {
                    selfService = null;
                    Log.e("TAG", "Register error", throwable);
                });

        // 发现域上的设备
        rxdnssd.browse(REG_TYPE_DNS, DOMAIN_OUTDOOR)
                .compose(rxdnssd.resolve())
                .compose(rxdnssd.queryRecords())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bonjourService -> {
                    Log.d("TAG", "browser outdoor: " +  bonjourService.toString() );
                    dump(bonjourService);
                    if (bonjourService.isLost()) {
                        outdoorService.remove(bonjourService);
                    } else {
                        outdoorService.add(bonjourService);
                    }
                }, throwable -> Log.e("TAG", "error", throwable));
    }

    public String getIp4(String outdoorId) {

        // TODO find ip outdoorService
        return "";
    }

    private void dump (BonjourService service) {
        Log.d(TAG, "dump: " + service);
        Inet4Address ip4 = service.getInet4Address();
        if (ip4 != null   ) {
            Log.d(TAG, "dump: " + ip4.getHostAddress() + " " + ip4.getHostName());
        }
    }
}
