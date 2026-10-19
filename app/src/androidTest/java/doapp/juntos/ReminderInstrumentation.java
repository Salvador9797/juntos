// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Juntos contributors
package doapp.juntos;
import android.app.*;
import android.os.*;
import android.content.*;
import org.json.*;
import java.time.*;
/** Runs only in the disposable emulator; never packaged into the release APK. */
public class ReminderInstrumentation extends Instrumentation {
 String phase;
 @Override public void onCreate(Bundle args){super.onCreate(args);phase=args.getString("phase","seed");start();}
 void require(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 @Override public void onStart(){Bundle report=new Bundle();try{
  Context c=getTargetContext();Store s=Store.get(c);NotificationManager n=c.getSystemService(NotificationManager.class);Alerts.channels(c);
  if(phase.equals("blocked")){
   LocalDateTime date=LocalDateTime.now().minusMinutes(1).withSecond(0).withNano(0);
   JSONObject i=new JSONObject().put("id","qa-blocked").put("title","QA blocked recovery").put("date",date.toLocalDate().toString()).put("time",date.toLocalTime().toString()).put("remind",0).put("repeat","none");s.put(i);Alerts.fire(c);
   require(!Alerts.allowed(c),"Test precondition: notifications must be blocked");require(s.stateLong(Alerts.firedKey("qa-blocked"))==0,"Blocked notification incorrectly consumed");
  }else if(phase.equals("recover")){
   require(Alerts.allowed(c),"Notifications must be allowed again");Alerts.fire(c);JSONObject i=s.item("qa-blocked");require(s.stateLong(Alerts.firedKey("qa-blocked"))==Alerts.due(i),"Recovered reminder not delivered");
   long before=0;for(android.service.notification.StatusBarNotification item:n.getActiveNotifications())if(item.getId()=="qa-blocked".hashCode())before=item.getPostTime();require(before>0,"No recovered notification");Alerts.fire(c);long after=0;for(android.service.notification.StatusBarNotification item:n.getActiveNotifications())if(item.getId()=="qa-blocked".hashCode())after=item.getPostTime();require(before==after,"Repeated notification was posted twice");s.complete("qa-blocked",true);n.cancelAll();
  }else if(phase.equals("seed")){
   LocalDateTime date=LocalDateTime.now().plusMinutes(2).withSecond(0).withNano(0);
   JSONObject i=new JSONObject().put("id","qa-closed").put("title","QA CLOSED SCREEN REMINDER").put("date",date.toLocalDate().toString()).put("time",date.toLocalTime().toString()).put("remind",0).put("repeat","none");s.put(i);Alerts.refresh(c);
   require(Alerts.exact(c),"Exact alarms unavailable");require(c.getSystemService(android.app.job.JobScheduler.class).getPendingJob(604)!=null,"Offline recovery job missing");report.putLong("due",Alerts.due(i));
  }else if(phase.equals("verify")){
   JSONObject i=s.item("qa-closed");require(s.stateLong(Alerts.firedKey("qa-closed"))==Alerts.due(i),"Reminder state not delivered");boolean found=false;for(android.service.notification.StatusBarNotification item:n.getActiveNotifications())if(item.getId()=="qa-closed".hashCode())found=true;require(found,"Notification absent after closed-process alarm");
  }
  report.putString("result","PASS "+phase);finish(Activity.RESULT_OK,report);
 }catch(Throwable e){report.putString("result","FAIL "+phase+": "+e.toString());finish(Activity.RESULT_CANCELED,report);}}
}
