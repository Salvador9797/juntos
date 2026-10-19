// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Juntos contributors
package doapp.juntos;
/** Time rules shared by alarm scheduling and recovery. */
public final class ReminderRules {
 public static final long RECOVERY_WINDOW=24L*60*60*1000;
 public static boolean pending(long due,long fired,long now,boolean done,boolean deleted){
  return !done&&!deleted&&due>0&&fired!=due&&due>=now-RECOVERY_WINDOW;
 }
 public static boolean deliver(long due,long fired,long now,boolean done,boolean deleted){
  return pending(due,fired,now,done,deleted)&&due<=now;
 }
 public static long trigger(long due,long now){return Math.max(due,now+1500);}
}
