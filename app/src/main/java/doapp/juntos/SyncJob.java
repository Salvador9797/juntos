// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Juntos contributors
package doapp.juntos;
import android.app.job.*;
public class SyncJob extends JobService {
 volatile long generation;volatile Thread task;
 @Override public boolean onStartJob(JobParameters p){final long run=++generation;task=new Thread(()->{boolean retry=false;try{Store s=Store.get(this);new TelegramSync(s).sync();Alerts.refresh(this);}catch(Exception e){retry=true;}Alerts.fire(this);if(generation==run){Alerts.deliveryState(this).edit().putLong("backgroundCheck",System.currentTimeMillis()).putBoolean("backgroundError",retry).apply();jobFinished(p,retry);}},"juntos-background");task.start();return true;}
 @Override public boolean onStopJob(JobParameters p){generation++;Thread t=task;if(t!=null)t.interrupt();return true;}
}
