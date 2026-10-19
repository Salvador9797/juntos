// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Juntos contributors
package doapp.juntos;
import android.content.*;
import android.os.PowerManager;
/** Keep the CPU awake only while a short local receiver operation is finishing. */
final class ReceiverWork {
 static void run(Context context,BroadcastReceiver.PendingResult result,Runnable work){
  Context c=context.getApplicationContext();
  PowerManager.WakeLock lock=c.getSystemService(PowerManager.class).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"Juntos:reminderDelivery");
  lock.acquire(9000);
  try{new Thread(()->{try{work.run();}finally{if(lock.isHeld())lock.release();result.finish();}},"juntos-local-receiver").start();}
  catch(RuntimeException e){if(lock.isHeld())lock.release();result.finish();}
 }
}
