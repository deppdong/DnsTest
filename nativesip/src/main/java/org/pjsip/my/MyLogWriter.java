package org.pjsip.my;

import android.util.Log;

import org.pjsip.pjsua2.LogEntry;
import org.pjsip.pjsua2.LogWriter;

public class MyLogWriter   extends LogWriter {

    public void write(LogEntry entry)
    {
      Log.e("pjsua2",entry.getMsg());
    }

}
