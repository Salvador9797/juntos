// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Juntos contributors
package doapp.juntos;
import android.app.job.*;
/** Offline recovery if a manufacturer delayed/cancelled the normal alarm. */
public class ReminderJob extends JobService {
 volatile long generation;
 @Override public boolean onStartJob(JobParameters p){long run=++generation;new Thread(()->{Alerts.fire(this);if(generation==run)jobFinished(p,false);},"juntos-reminder-recovery").start();return true;}
 @Override public boolean onStopJob(JobParameters p){generation++;return true;}
}
