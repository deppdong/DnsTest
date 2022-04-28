package com.dk.dns;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import org.pjsip.MyApp;
import org.pjsip.my.MyAccount;
import org.pjsip.my.MyAppObserver;
import org.pjsip.my.MyBuddy;
import org.pjsip.my.MyCall;
import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.AuthCredInfo;
import org.pjsip.pjsua2.AuthCredInfoVector;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.Media;
import org.pjsip.pjsua2.StringVector;
import org.pjsip.pjsua2.pjsip_status_code;

public class SipCallManager implements MyAppObserver {
    private static final String TAG = "SipCallManager";

    private static final String PASSWORD = "h1234";
    private static final String PORT = ":7060";

    private static SipCallManager sInstance = new SipCallManager();

    private MyCall currentCall = null;
    private MediaPlayer mBgmMediaPlayer = null;

    public static SipCallManager getInstance() {
        return sInstance;
    }

    public MyApp app = null;
    public static AccountConfig accCfg = null;
    public MyAccount account = null;

    public SipCallManager() {
    }

    public void init(Context context) {
        if (app == null) {
            app = new MyApp();
            app.init(this, context.getFilesDir().getAbsolutePath());
            Log.w(TAG, "init: Sip App");
        }
    }

    /**
     * 注册账号
     *
     * @param username
     * @param ip
     */
    public void reg(String username, String ip) {
        String  ipport = ip + PORT;
        username = "1005";
        ipport = "121.40.157.185:7060";
        Log.i(TAG, "reg " + username + " " +ipport);
        accCfg = new AccountConfig();

        accCfg.setIdUri("sip:" + username + "@" + ipport);
        accCfg.getRegConfig().setRegistrarUri("sip:" + username + "@" + ipport);

        AuthCredInfoVector creds = accCfg.getSipConfig().getAuthCreds();
        creds.clear();

        creds.add(new AuthCredInfo("Digest", "*", username, 0, PASSWORD));

        StringVector proxies = accCfg.getSipConfig().getProxies();
        proxies.clear();
        //  proxies.add(proxy);
        /* Enable ICE */

        //AccountNatConfig x =new AccountNatConfig();
        //x.setIceEnabled(true);x.setIceEnabled(true);
        //x.setTurnServer("118.190.151.162:3478");x.setTurnPassword("lilinaini");x.setTurnUserName("lilin");

        /**
         accCfg.getNatConfig().setIceEnabled(true);
         accCfg.getNatConfig().setTurnServer("ip3478");
         accCfg.getNatConfig().setTurnEnabled(true);
         accCfg.getNatConfig().setTurnUserName("lilin");
         accCfg.getNatConfig().setTurnPassword("lilinaini");
         **/
        accCfg.getVideoConfig().setAutoShowIncoming(true);
        try {
            for (MyAccount m : app.accList) {
                app.delAcc(m);
            }
            account = app.addAcc(accCfg);
            account.modify(accCfg);

        } catch (Exception e) {
            Log.w(TAG, "reg: ", e);
        }
    }

    /**
     * 拨打电话
     *
     * @param username
     * @param ip
     */
    public void dial(String username, String ip) {
        String ipport = ip+ PORT;
        ipport = "121.40.157.185:7060";
        if (currentCall != null || account == null) {
            return;
        }
        MyCall my = null;
        try {
            accCfg.getVideoConfig().setAutoTransmitOutgoing(false);
            account.modify(accCfg);
            my = new MyCall(account, -1);
            CallOpParam prm = new CallOpParam(true);

            my.makeCall("sip:" + username + "@" + ipport, prm);
        } catch (Exception e) {
            if (my != null) {
                my.delete();
            }
            Log.w(TAG, "dial: ", e);
            return;
        }
        currentCall = my;
    }

    public void dialVideoCall(String username, String ipport) {
        if (currentCall != null || account == null) {
            return;
        }
        MyCall my = null;
        try {
            accCfg.getVideoConfig().setAutoTransmitOutgoing(true);
            account.modify(accCfg);
            my = new MyCall(account, -1);
            CallOpParam prm = new CallOpParam(true);
            my.makeCall("sip:" + username + "@" + ipport, prm);
        } catch (Exception e) {
            if (my != null) {
                my.delete();
            }
            Log.w(TAG, "dialVideoCall: ", e);
            return;
        }
        currentCall = my;
//        TODO call video activity
//        Intent intent = new Intent(this, VideoActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(intent);
    }

    /**
     * 接听来电
     */
    public void accept() {
        if (currentCall != null) {
            if (mBgmMediaPlayer != null) {
                mBgmMediaPlayer.stop();
                mBgmMediaPlayer.release();
                mBgmMediaPlayer = null;
            }

            try {
                CallInfo ci = currentCall.getInfo();
                //AudioMedia *aud_med = NULL;
                Media x = currentCall.getMedia(0);
                // Find out which media index is the audio
            } catch (Exception e) {
                Log.w(TAG, "accept: ", e);
            }

            CallOpParam prm = new CallOpParam();
            //prm.setStatusCode(pjsip_status_code.PJSIP_SC_RINGING);
            prm.setStatusCode(pjsip_status_code.PJSIP_SC_OK);
            try {
                CallInfo c = currentCall.getInfo();
                //Log.e("提示1","》》"+c.getRemVideoCount()+"<>"+currentCall.vidGetStreamIdx());//0 -1
                if (c.getRemOfferer() && c.getRemVideoCount() > 0) {//有视频 1<>-1
                    // TODO 视频通话
//                    Intent intent = new Intent(this, VideoActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    startActivity(intent);
                }
                currentCall.answer(prm);
            } catch (Exception e) {
                Log.w(TAG, "accept: ", e);
            }
        }
    }

    public void hangup() {
        if (currentCall != null) {
            CallOpParam prm = new CallOpParam();
            prm.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE);
            try {
                currentCall.hangup(prm);
                currentCall.delete();
                currentCall = null;
            } catch (Exception e) {
                Log.w(TAG, "hangup: ", e);
            }
        }
    }

    public void dtmf(String dtmf) {
        if (currentCall != null) {
            try {
                log("dtmf send " + dtmf);
                currentCall.dialDtmf(dtmf);  //2833
                /** info down
                 CallSendRequestParam prm = new CallSendRequestParam();
                 prm.setMethod("INFO");
                 SipTxOption txo = new SipTxOption();
                 txo.setContentType(" application/dtmf-relay");
                 txo.setMsgBody("Signal=9\nDuration=160");
                 prm.setTxOption(txo);
                 currentCall.sendRequest(prm);
                 */
            } catch (Exception e) {
                Log.w(TAG, "dtmf: ", e);
            }

        }

    }

    @Override
    public void notifyRegState(pjsip_status_code code, String reason, int expiration) {
        log("notifyRegState " + code.toString() + " / " + reason + " / " + expiration);
    }

    @Override
    public void notifyIncomingCall(MyCall call) {
        log("notifyIncomingCall " + call.getId());
        try {
            CallInfo callInfo = call.getInfo();
            dumpCallInfo("notifyIncomingCall", callInfo);
            currentCall = call;
            log("notifyIncomingCall " + callInfo);
        } catch (Exception e) {
            Log.w(TAG, "notifyIncomingCall: ", e);
        }
    }

    @Override
    public void notifyCallState(MyCall call) {
        log("notifyCallState " + call.getId());
        try {
            CallInfo callInfo = call.getInfo();
            dumpCallInfo("notifyCallState", callInfo);
        } catch (Exception e) {
            Log.w(TAG, "notifyCallState: ", e);
        }
    }

    @Override
    public void notifyCallMediaState(MyCall call) {
        log("notifyCallMediaState " + call.getId());
        try {
            CallInfo callInfo = call.getInfo();
            dumpCallInfo("notifyCallMediaState", callInfo);
        } catch (Exception e) {
            Log.w(TAG, "notifyCallMediaState: ", e);
        }
    }

    @Override
    public void notifyBuddyState(MyBuddy buddy) {
        log("notifyBuddyState ");
    }

    @Override
    public void notifyChangeNetwork() {
        log("notifyChangeNetwork ");
    }

    private void log(String msg) {
        Log.d(TAG, msg);
    }

    public void dumpCallInfo(String msg ,  CallInfo callInfo) {
        log(msg + " : " + callInfo.getCallIdString() +" " + callInfo.getState() +" " + callInfo.getStateText() +" " + callInfo.getRemoteUri() );
    }
}
