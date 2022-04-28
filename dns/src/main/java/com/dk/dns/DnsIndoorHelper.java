package com.dk.dns;

import static com.dk.dns.DnsOutdoorHelper.PREFIX_OUTDOOR;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.github.druk.rx2dnssd.BonjourService;
import com.github.druk.rx2dnssd.Rx2Dnssd;
import com.github.druk.rx2dnssd.Rx2DnssdBindable;

import java.net.Inet4Address;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DnsIndoorHelper {
    private static final String TAG = "DnsIndoorHelper";
    public final static String REG_TYPE_DNS = "_rxdnssd._tcp";
    public final static String REG_TYPE_HTTP = "_http._tcp";
    public final static String DOMAIN_INDOOR = "local.";
    public final static String PREFIX_INDOOR = "indoor.";

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
        selfService = new BonjourService.Builder(0, 0, PREFIX_INDOOR+ outdoorId, REG_TYPE_DNS, "local.")
                .port(1234)
                .build();

        Log.i(TAG, "init indoor: " + selfService);

        // 注册设备到域
        rxdnssd.register(selfService)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bonjourService -> {
                    Log.i(TAG, "Register successfully " + bonjourService.toString());
                }, throwable -> {
                    selfService = null;
                    Log.e(TAG, "Register error", throwable);
                });

        // 发现outdoor设备
        rxdnssd.browse(REG_TYPE_DNS, "local.")
                .compose(rxdnssd.resolve())
                .compose(rxdnssd.queryRecords())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bonjourService -> {
                    if (TextUtils.isEmpty(bonjourService.getServiceName()) ||
                            bonjourService.getServiceName().contains(PREFIX_INDOOR) ||
                            bonjourService.getInet4Address() == null) {
                        return;
                    }
                    Log.d(TAG, "browser outdoor: " + bonjourService.toString());
                    dump(bonjourService);
                    if (bonjourService.isLost() ) {
                        BonjourService cached = servicesMap.get(bonjourService.getServiceName());
                        if (cached == null || cached.getInet4Address() == null)  return;
                        if (cached.getInet4Address().getHostAddress().equals(bonjourService.getInet4Address().getHostAddress())) {
                            servicesMap.remove(bonjourService.getServiceName());
                        }
//                        indoorService.remove(bonjourService);
                    } else {
                        servicesMap.put(bonjourService.getServiceName(), bonjourService);
//                        indoorService.add(bonjourService);
                    }
                }, throwable -> Log.e(TAG, "error", throwable));
    }

    public String getServiceName() {
        return selfService ==  null?"" :selfService.getServiceName();
    }

    public String getOutdoorIp4(String indoorId) {
        BonjourService cached = servicesMap.get(PREFIX_OUTDOOR + indoorId);
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
