// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Juntos contributors
package doapp.juntos;
public class ReminderChecks {
 static int checks;
 static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);checks++;System.out.println("PASS: "+why);}
 public static void main(String[] args){long now=200000000L,at=now-1000;
  check(ReminderRules.deliver(at,0,now,false,false),"Overdue reminder can recover with app closed");
  check(!ReminderRules.deliver(at,at,now,false,false),"Delivered revision is not posted twice");
  check(ReminderRules.deliver(at,0,now,false,false),"Unsent reminder remains eligible after blocked permission");
  check(!ReminderRules.deliver(now+1,0,now,false,false),"A reminder never fires early");
  check(ReminderRules.deliver(now,0,now,false,false),"Reminder due now is eligible");
  check(!ReminderRules.deliver(at,0,now,true,false),"Completed activity does not notify");
  check(!ReminderRules.deliver(at,0,now,false,true),"Deleted activity does not notify");
  check(!ReminderRules.pending(0,0,now,false,false),"Invalid/no reminder is ignored");
  check(ReminderRules.deliver(now-ReminderRules.RECOVERY_WINDOW,0,now,false,false),"24 hour recovery boundary is included");
  check(!ReminderRules.deliver(now-ReminderRules.RECOVERY_WINDOW-1,0,now,false,false),"Older reminders do not flood the phone");
  check(ReminderRules.pending(now+60000,0,now,false,false),"Future activity is scheduled");
  check(ReminderRules.trigger(at,now)==now+1500,"Past due reminder rearmed promptly");
  check(ReminderRules.trigger(now+60000,now)==now+60000,"Future reminder retains its original deadline");
  System.out.println(checks+" reminder checks passed");
 }
}
