package org.pjsip.pjsua2;

public class NativeLib {

    // Used to load the 'pjsua2' library on application startup.
    static {
        System.loadLibrary("pjsua2");
    }

    /**
     * A native method that is implemented by the 'pjsua2' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}