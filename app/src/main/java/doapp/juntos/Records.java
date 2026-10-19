// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Juntos contributors
package doapp.juntos;
import org.json.*;
final class Records {
 static JSONObject copy(JSONObject v)throws JSONException{return new JSONObject(v.toString());}
 static boolean newer(JSONObject a,JSONObject b){return b==null||Versions.compare(a.optLong("rev"),a.optString("origin"),b.optLong("rev"),b.optString("origin"))>0;}
 static JSONObject deleted(JSONObject v)throws JSONException{JSONObject out=new JSONObject().put("id",v.getString("id")).put("rev",v.optLong("rev")).put("origin",v.optString("origin")).put("updated",v.optLong("updated")).put("by",v.optString("by")).put("deleted",true);if(!v.optString("series").isEmpty())out.put("series",v.optString("series"));return out;}
 static JSONObject snapshot(JSONObject v)throws JSONException {JSONObject c=copy(v);c.remove("_local");return c;}
 static JSONObject nextOccurrence(JSONObject item)throws Exception{
  if(!item.optBoolean("done")||item.optBoolean("deleted")||item.optString("repeat","none").equals("none"))return null;
  java.time.LocalDate date=java.time.LocalDate.parse(item.getString("date"));java.time.LocalDate next=Plans.next(date,item.optString("repeat"),item.optInt("anchorDay",date.getDayOfMonth()),item.optInt("anchorMonth",date.getMonthValue()));if(next==null)return null;
  String series=item.optString("series",item.getString("id"));JSONObject n=copy(item);n.put("series",series).put("id",series+":"+next).put("date",next.toString()).put("done",false).put("deleted",false);n.remove("rev");n.remove("origin");n.remove("updated");n.remove("by");JSONArray checks=n.optJSONArray("checks");if(checks!=null)for(int j=0;j<checks.length();j++)checks.getJSONObject(j).put("done",false);return n;
 }
}
