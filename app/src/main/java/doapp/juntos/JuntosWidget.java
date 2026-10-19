// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Juntos contributors
package doapp.juntos;
import android.appwidget.*;
import android.content.*;
import android.widget.*;
import org.json.*;
import java.util.*;
public class JuntosWidget extends AppWidgetProvider {
 static void updateAll(Context c){AppWidgetManager m=AppWidgetManager.getInstance(c);int[] ids=m.getAppWidgetIds(new ComponentName(c,JuntosWidget.class));update(c,m,ids);}
 static void update(Context c,AppWidgetManager manager,int[] ids){for(int widget:ids){RemoteViews v=new RemoteViews(c.getPackageName(),R.layout.widget_juntos);v.setOnClickPendingIntent(R.id.widget_root,Alerts.open(c,""));try{Store s=Store.get(c);StringBuilder b=new StringBuilder();int count=0;for(JSONObject i:s.list())if(!i.optBoolean("done")){if(count++>=3)break;if(b.length()>0)b.append("\n\n");b.append(i.optString("emoji","📌")).append(" ").append(i.optString("title")).append("\n").append(i.optString("date")).append(i.optString("time").isEmpty()?"":" · "+i.optString("time"));}v.setTextViewText(R.id.widget_plans,b.length()==0?"Un espacio para sus próximos planes.\nToca para crear una actividad.":b.toString());}catch(Exception e){v.setTextViewText(R.id.widget_plans,"Abre Juntos para ver tu agenda.");}manager.updateAppWidget(widget,v);}}
 @Override public void onUpdate(Context c,AppWidgetManager m,int[] ids){update(c,m,ids);}
 @Override public void onAppWidgetOptionsChanged(Context c,AppWidgetManager m,int id,android.os.Bundle options){update(c,m,new int[]{id});}
}
