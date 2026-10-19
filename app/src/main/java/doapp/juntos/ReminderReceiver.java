// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Juntos contributors
package doapp.juntos;
import android.content.*;
public class ReminderReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c,Intent i){PendingResult result=goAsync();new Thread(()->{try{Alerts.fire(c.getApplicationContext());}finally{result.finish();}},"juntos-reminder").start();}
}
