package com.dk.dns;

import static com.dk.dns.DnsIndoorHelper.DOMAIN_INDOOR;

import android.content.Context;
import android.util.Log;

import com.github.druk.rx2dnssd.BonjourService;
import com.github.druk.rx2dnssd.Rx2Dnssd;
import com.github.druk.rx2dnssd.Rx2DnssdBindable;
import com.github.druk.rx2dnssd.Rx2DnssdEmbedded;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DnsOutdoorHelper {
    private static final String TAG = "DnsOutdoor";
    public final static String REG_TYPE_DNS = "_rxdnssd._tcp";
    public final static String REG_TYPE_HTTP = "_http._tcp";
    public final static String DOMAIN_OUTDOOR = "outdoor.";

    private Rx2Dnssd rxdnssd;
    private BonjourService selfService;

//    private List<BonjourService> indoorService = new ArrayList<>();
    private ConcurrentHashMap<String, BonjourService> servicesMap = new ConcurrentHashMap<String, BonjourService>();
    
    public void init(Context context, String outdoorId) {
        if (selfService != null) {
            Log.w(TAG, "init: registered " + selfService.getServiceName()  + " vs " + outdoorId);
            return;
        }

        rxdnssd = new Rx2DnssdBindable(context);
        selfService = new BonjourService.Builder(0, 0, "gate."+ outdoorId, REG_TYPE_DNS, DOMAIN_OUTDOOR)
                .hostname(outdoorId)
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
        rxdnssd.browse(REG_TYPE_DNS, DOMAIN_INDOOR)
                .compose(rxdnssd.resolve())
                .compose(rxdnssd.queryRecords())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bonjourService -> {
                    Log.d("TAG", "browser indoor: " + bonjourService.toString());
                    dump(bonjourService);
                    if (bonjourService.isLost()) {
                        BonjourService cached = servicesMap.get(bonjourService.getHostname());
                        if (cached == null || cached.getInet4Address() == null)  return;
                        if (cached.getInet4Address().getHostAddress().equals(bonjourService.getInet4Address().getHostAddress())) {
                            servicesMap.remove(bonjourService.getHostname());
                        }
//                        indoorService.remove(bonjourService);
                    } else {
                        if (bonjourService.getInet4Address() != null) {
                            servicesMap.put(bonjourService.getHostname(), bonjourService);
                        }
//                        indoorService.add(bonjourService);
                    }
                }, throwable -> Log.e("TAG", "error", throwable));
    }

    public String getIp4(String outdoorId) {
        BonjourService cached = servicesMap.get(outdoorId);
        if (cached != null) {
            return cached.getInet4Address().getHostAddress();
        }
        return "";
    }

    private void dump (BonjourService service) {
        Log.d(TAG, "dump: " + service);
        Inet4Address ip4 = service.getInet4Address();
        if (ip4 != null   ) {
            Log.d(TAG, "dump: " + ip4.getHostAddress() );
        }
    }
}
