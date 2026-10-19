// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Juntos contributors
package doapp.juntos;
import android.app.*;
import android.app.job.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;
import java.time.*;
import org.json.*;
final class Alerts {
 static final String REMIND="juntos.reminders",CHANGES="juntos.changes",TEST="doapp.juntos.TEST_REMINDER";
 static final Object DELIVERY_LOCK=new Object();
 static void channels(Context c){NotificationManager n=c.getSystemService(NotificationManager.class);n.createNotificationChannel(new NotificationChannel(REMIND,"Recordatorios de actividades",NotificationManager.IMPORTANCE_HIGH));n.createNotificationChannel(new NotificationChannel(CHANGES,"Cambios de mi pareja",NotificationManager.IMPORTANCE_DEFAULT));}
 static boolean allowed(Context c){return (Build.VERSION.SDK_INT<33||c.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED)&&c.getSystemService(NotificationManager.class).areNotificationsEnabled();}
 static boolean allowed(Context c,String channel){if(!allowed(c))return false;NotificationChannel ch=c.getSystemService(NotificationManager.class).getNotificationChannel(channel);return ch==null||ch.getImportance()!=NotificationManager.IMPORTANCE_NONE;}
 static boolean exact(Context c){return Build.VERSION.SDK_INT<31||c.getSystemService(AlarmManager.class).canScheduleExactAlarms();}
 static PendingIntent open(Context c,String id){Intent i=new Intent(c,MainActivity.class).putExtra("openItem",id).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);return PendingIntent.getActivity(c,id.hashCode(),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
 static boolean show(Context c,String channel,int id,String title,String text,String item){channels(c);if(!allowed(c,channel))return false;try{Notification n=new Notification.Builder(c,channel).setSmallIcon(doapp.juntos.R.drawable.ic_notification).setContentTitle(title).setContentText(text).setStyle(new Notification.BigTextStyle().bigText(text)).setContentIntent(open(c,item)).setAutoCancel(true).setVisibility(Notification.VISIBILITY_PRIVATE).build();c.getSystemService(NotificationManager.class).notify(id,n);return true;}catch(SecurityException e){return false;}}
 static void refresh(Context c){try{channels(c);Store s=Store.get(c);s.ensureRepeats();scheduleMaintenance(c);schedule(c,s);JuntosWidget.updateAll(c);scheduleSync(c,s);}catch(Exception e){deliveryState(c).edit().putBoolean("error",true).apply();}}
 static SharedPreferences deliveryState(Context c){return c.getSharedPreferences("juntos.delivery",Context.MODE_PRIVATE);}
 static void invalidateSchedule(Context c){synchronized(DELIVERY_LOCK){deliveryState(c).edit().remove("at").apply();}}
 static PendingIntent alarm(Context c){return PendingIntent.getBroadcast(c,600,new Intent(c,ReminderReceiver.class).setAction("doapp.juntos.REMIND"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
 static long due(JSONObject i){return Plans.reminder(i.optString("date"),i.optString("time"),i.optInt("remind",-1),ZoneId.systemDefault());}
 static String firedKey(String id){return "reminded:"+id;}
 static boolean setAlarm(Context c,long at,PendingIntent intent){AlarmManager a=c.getSystemService(AlarmManager.class);if(exact(c)){try{a.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,intent);return true;}catch(SecurityException ignored){/* Permission may have changed: keep an inexact fallback. */}}a.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,intent);return false;}
 static void schedule(Context c,Store s)throws Exception{synchronized(DELIVERY_LOCK){
  long now=System.currentTimeMillis(),nearest=Long.MAX_VALUE;
  boolean enabled=allowed(c,REMIND);
  for(JSONObject i:s.list()){
   if(i.optBoolean("done"))continue;
   if(!i.optString("repeat","none").equals("none")){try{LocalDate day=LocalDate.parse(i.optString("date"));if(!day.isBefore(LocalDate.now()))nearest=Math.min(nearest,day.plusDays(1).atStartOfDay(ZoneId.systemDefault()).plusMinutes(1).toInstant().toEpochMilli());}catch(Exception ignored){}}
   long at=due(i);
   if(enabled&&ReminderRules.pending(at,s.stateLong(firedKey(i.optString("id"))),now,i.optBoolean("done"),i.optBoolean("deleted")))nearest=Math.min(nearest,ReminderRules.trigger(at,now));
  }
  SharedPreferences state=deliveryState(c);
  if(nearest==Long.MAX_VALUE){c.getSystemService(AlarmManager.class).cancel(alarm(c));state.edit().remove("at").apply();return;}
  // Do not repeatedly postpone the same future alarm whenever the UI or a sync refreshes.
  if(nearest==state.getLong("at",0)&&nearest>now&&state.getBoolean("exact",false)==exact(c))return;
  boolean precise=setAlarm(c,nearest,alarm(c));state.edit().putLong("at",nearest).putBoolean("exact",precise).putBoolean("error",false).apply();
 }}
 static void fire(Context c){synchronized(DELIVERY_LOCK){try{
  Store s=Store.get(c);s.ensureRepeats();long now=System.currentTimeMillis();
  for(JSONObject i:s.list()){
   long at=due(i);String key=firedKey(i.optString("id"));
   if(ReminderRules.deliver(at,s.stateLong(key),now,i.optBoolean("done"),i.optBoolean("deleted"))&&show(c,REMIND,i.optString("id").hashCode(),i.optString("emoji","📌")+" "+i.optString("title"),i.optString("date")+" · "+i.optString("time")+" · "+i.optString("person","Ambos"),i.optString("id")))s.stateLong(key,at);
  }
  // A blocked permission/channel must not consume a reminder as if it had been delivered.
  schedule(c,s);JuntosWidget.updateAll(c);deliveryState(c).edit().putLong("lastCheck",now).putBoolean("error",false).apply();
 }catch(Exception e){deliveryState(c).edit().putBoolean("error",true).apply();}}}
 static void test(Context c){setAlarm(c,System.currentTimeMillis()+60000,PendingIntent.getBroadcast(c,601,new Intent(c,ReminderReceiver.class).setAction(TEST),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE));}
 static void change(Context c,Store s,long before)throws Exception{JSONArray notices=s.notices();if(notices.length()==0)return;JSONObject last=notices.getJSONObject(notices.length()-1);if(last.optLong("at")<=before)return;int count=0;for(int j=0;j<notices.length();j++)if(notices.getJSONObject(j).optLong("at")>before)count++;if(s.config().optBoolean("changeAlerts",true))show(c,CHANGES,9001,count==1?"Un cambio en su agenda":count+" cambios en su agenda",last.optString("text"),last.optString("id"));}
 static void scheduleMaintenance(Context c){JobScheduler jobs=c.getSystemService(JobScheduler.class);if(jobs.getPendingJob(604)==null)jobs.schedule(new JobInfo.Builder(604,new ComponentName(c,ReminderJob.class)).setPersisted(true).setPeriodic(15*60*1000L).build());}
 static void scheduleSync(Context c,Store s)throws Exception{JobScheduler jobs=c.getSystemService(JobScheduler.class);if(s.config().optString("token").isEmpty()||!s.config().optBoolean("backgroundSync",true)){jobs.cancel(602);return;}if(jobs.getPendingJob(602)==null)jobs.schedule(new JobInfo.Builder(602,new ComponentName(c,SyncJob.class)).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPersisted(true).setPeriodic(15*60*1000L,5*60*1000L).build());}
}
