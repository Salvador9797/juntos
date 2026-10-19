// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Juntos contributors
package doapp.juntos;
import android.app.*;
import android.app.job.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;
import java.time.*;
import java.util.*;
import org.json.*;
final class Alerts {
 static final String REMIND="juntos.reminders",CHANGES="juntos.changes";
 static void channels(Context c){NotificationManager n=c.getSystemService(NotificationManager.class);n.createNotificationChannel(new NotificationChannel(REMIND,"Recordatorios de actividades",NotificationManager.IMPORTANCE_HIGH));n.createNotificationChannel(new NotificationChannel(CHANGES,"Cambios de mi pareja",NotificationManager.IMPORTANCE_DEFAULT));}
 static boolean allowed(Context c){return (Build.VERSION.SDK_INT<33||c.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED)&&c.getSystemService(NotificationManager.class).areNotificationsEnabled();}
 static PendingIntent open(Context c,String id){Intent i=new Intent(c,MainActivity.class).putExtra("openItem",id).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);return PendingIntent.getActivity(c,id.hashCode(),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
 static void show(Context c,String channel,int id,String title,String text,String item){channels(c);if(!allowed(c))return;Notification n=new Notification.Builder(c,channel).setSmallIcon(doapp.juntos.R.drawable.ic_notification).setContentTitle(title).setContentText(text).setStyle(new Notification.BigTextStyle().bigText(text)).setContentIntent(open(c,item)).setAutoCancel(true).setVisibility(Notification.VISIBILITY_PRIVATE).build();c.getSystemService(NotificationManager.class).notify(id,n);}
 static void refresh(Context c){try{Store s=Store.get(c);s.ensureRepeats();schedule(c,s);JuntosWidget.updateAll(c);scheduleSync(c,s);}catch(Exception ignored){}}
 static PendingIntent alarm(Context c){return PendingIntent.getBroadcast(c,600,new Intent(c,ReminderReceiver.class).setAction("doapp.juntos.REMIND"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
 static long due(JSONObject i){return Plans.reminder(i.optString("date"),i.optString("time"),i.optInt("remind",-1),ZoneId.systemDefault());}
 static String firedKey(String id){return "reminded:"+id;}
 static void schedule(Context c,Store s)throws Exception{AlarmManager a=c.getSystemService(AlarmManager.class);a.cancel(alarm(c));long now=System.currentTimeMillis(),nearest=Long.MAX_VALUE;for(JSONObject i:s.list()){if(i.optBoolean("done"))continue;if(!i.optString("repeat","none").equals("none")){try{LocalDate day=LocalDate.parse(i.optString("date"));if(!day.isBefore(LocalDate.now()))nearest=Math.min(nearest,day.plusDays(1).atStartOfDay(ZoneId.systemDefault()).plusMinutes(1).toInstant().toEpochMilli());}catch(Exception ignored){}}long at=due(i);if(at<=0||s.stateLong(firedKey(i.optString("id")))==at||at<now-24*60*60000L)continue;nearest=Math.min(nearest,Math.max(at,now+1500));}if(nearest!=Long.MAX_VALUE)a.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,nearest,alarm(c));}
 static void fire(Context c){try{Store s=Store.get(c);s.ensureRepeats();long now=System.currentTimeMillis();for(JSONObject i:s.list()){long at=due(i);String key=firedKey(i.optString("id"));if(!i.optBoolean("done")&&at>0&&at<=now+1000&&at>=now-24*60*60000L&&s.stateLong(key)!=at){show(c,REMIND,i.optString("id").hashCode(),i.optString("emoji","📌")+" "+i.optString("title"),i.optString("date")+" · "+i.optString("time")+" · "+i.optString("person","Ambos"),i.optString("id"));s.stateLong(key,at);}}schedule(c,s);JuntosWidget.updateAll(c);}catch(Exception ignored){}}
 static void change(Context c,Store s,long before)throws Exception{JSONArray notices=s.notices();if(notices.length()==0)return;JSONObject last=notices.getJSONObject(notices.length()-1);if(last.optLong("at")<=before)return;int count=0;for(int j=0;j<notices.length();j++)if(notices.getJSONObject(j).optLong("at")>before)count++;if(s.config().optBoolean("changeAlerts",true))show(c,CHANGES,9001,count==1?"Un cambio en su agenda":count+" cambios en su agenda",last.optString("text"),last.optString("id"));}
 static void scheduleSync(Context c,Store s)throws Exception{JobScheduler jobs=c.getSystemService(JobScheduler.class);if(s.config().optString("token").isEmpty()||!s.config().optBoolean("backgroundSync",true)){jobs.cancel(602);return;}if(jobs.getPendingJob(602)==null)jobs.schedule(new JobInfo.Builder(602,new ComponentName(c,SyncJob.class)).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPersisted(true).setPeriodic(15*60*1000L,5*60*1000L).build());}
}
