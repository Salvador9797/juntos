// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Juntos contributors
package doapp.juntos;
import android.content.Context;
import android.security.keystore.*;
import android.util.AtomicFile;
import org.json.*;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.security.KeyStore;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;
final class Store {
 @android.annotation.SuppressLint("StaticFieldLeak") // Only the application context is retained.
 private static Store instance;
 static synchronized Store get(Context c)throws Exception{if(instance==null)instance=new Store(c.getApplicationContext());return instance;}
 final Context context; final AtomicFile file; JSONObject root;
 Store(Context c)throws Exception{context=c;file=new AtomicFile(new File(c.getFilesDir(),"juntos.sec")); if(file.getBaseFile().exists()){byte[] b;try(InputStream in=file.openRead()){b=Streams.read(in,64*1024*1024);}root=new JSONObject(new String(local(false,b),StandardCharsets.UTF_8));}else{root=new JSONObject();root.put("device",UUID.randomUUID().toString());root.put("items",new JSONObject());root.put("config",new JSONObject());save();}}
 SecretKey localKey()throws Exception{KeyStore k=KeyStore.getInstance("AndroidKeyStore");k.load(null);if(!k.containsAlias("juntos.local")){KeyGenerator g=KeyGenerator.getInstance("AES","AndroidKeyStore");g.init(new KeyGenParameterSpec.Builder("juntos.local",KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());g.generateKey();}return (SecretKey)k.getKey("juntos.local",null);}
 byte[] local(boolean encrypt,byte[] b)throws Exception{Cipher c=Cipher.getInstance("AES/GCM/NoPadding");if(encrypt){c.init(Cipher.ENCRYPT_MODE,localKey());byte[] e=c.doFinal(b),iv=c.getIV(),out=new byte[iv.length+e.length];System.arraycopy(iv,0,out,0,iv.length);System.arraycopy(e,0,out,iv.length,e.length);return out;}c.init(Cipher.DECRYPT_MODE,localKey(),new GCMParameterSpec(128,Arrays.copyOfRange(b,0,12)));return c.doFinal(b,12,b.length-12);}
 synchronized void save()throws Exception{trimHistory();byte[] b=local(true,root.toString().getBytes(StandardCharsets.UTF_8));FileOutputStream out=null;try{out=file.startWrite();out.write(b);file.finishWrite(out);}catch(Exception e){if(out!=null)file.failWrite(out);throw e;}}
 void trimHistory()throws Exception{JSONObject history=root.optJSONObject("history");if(history==null)return;while(history.toString().length()>1000000){String oldest=null;long at=Long.MAX_VALUE;Iterator<String> ids=history.keys();while(ids.hasNext()){String id=ids.next();JSONArray list=history.optJSONArray(id);if(list!=null&&list.length()>0&&list.getJSONObject(0).optLong("at")<at){oldest=id;at=list.getJSONObject(0).optLong("at");}}if(oldest==null)break;JSONArray list=history.getJSONArray(oldest);list.remove(0);if(list.length()==0)history.remove(oldest);}}
 synchronized JSONObject setupDraft()throws Exception{JSONObject d=root.optJSONObject("setupDraft");return d==null?new JSONObject():new JSONObject(d.toString());}
 synchronized void setupDraft(JSONObject d)throws Exception{root.put("setupDraft",new JSONObject(d.toString()));save();}
 synchronized void setupComplete()throws Exception{root.remove("setupDraft");root.put("setupSkipped",0);root.put("setupCompleted",1);save();}
 synchronized JSONObject config()throws Exception{return new JSONObject(root.getJSONObject("config").toString());}
 synchronized String device(){return root.optString("device");}
 synchronized void config(JSONObject c)throws Exception{root.put("config",c);save();}
 synchronized JSONObject items()throws Exception{JSONObject all=root.getJSONObject("items");Iterator<String> ids=all.keys();while(ids.hasNext()){String id=ids.next();JSONObject v=all.getJSONObject(id);if(v.optBoolean("deleted")&&v.has("title")){remember(v,"Antes de limpiar la actividad eliminada");all.put(id,Records.deleted(v));}}return new JSONObject(all.toString());}
 synchronized List<JSONObject> list()throws Exception{List<JSONObject> list=new ArrayList<>();JSONObject all=items();Iterator<String> keys=all.keys();while(keys.hasNext()){JSONObject i=all.getJSONObject(keys.next());if(!i.optBoolean("deleted"))list.add(i);}list.sort(Comparator.comparing(i->i.optString("date")+i.optString("time")));return list;}
 synchronized JSONObject item(String id)throws Exception{JSONObject v=root.getJSONObject("items").optJSONObject(id);return v==null?null:Records.copy(v);}
 synchronized void remember(JSONObject old,String reason)throws Exception{
  if(old==null)return;JSONObject history=root.optJSONObject("history");if(history==null){history=new JSONObject();root.put("history",history);}String id=old.getString("id");JSONArray list=history.optJSONArray(id);if(list==null)list=new JSONArray();for(int k=0;k<list.length();k++){JSONObject prior=list.getJSONObject(k).getJSONObject("record");if(prior.optLong("rev")==old.optLong("rev")&&prior.optString("origin").equals(old.optString("origin")))return;}JSONArray trimmed=new JSONArray();for(int j=Math.max(0,list.length()-11);j<list.length();j++)trimmed.put(list.get(j));trimmed.put(new JSONObject().put("record",old).put("reason",reason).put("at",System.currentTimeMillis()));history.put(id,trimmed);
 }
 synchronized JSONArray history(String id)throws Exception{JSONObject all=root.optJSONObject("history");JSONArray a=all==null?null:all.optJSONArray(id);return a==null?new JSONArray():new JSONArray(a.toString());}
 synchronized void put(JSONObject item)throws Exception{JSONObject all=root.getJSONObject("items"),old=all.optJSONObject(item.optString("id"));remember(old,item.optBoolean("deleted")?"Eliminada":"Cambio anterior");JSONObject next=Records.copy(item);next.put("rev",old==null?1:old.optLong("rev")+1);next.put("origin",device());next.put("updated",System.currentTimeMillis());next.put("by",config().optString("name","Yo"));if(next.optBoolean("deleted"))next=Records.deleted(next);all.put(next.getString("id"),next);save();}
 synchronized void complete(String id,boolean done)throws Exception{JSONObject v=item(id);if(v==null||v.optBoolean("deleted"))return;v.put("done",done);put(v);ensureRepeats();}
 synchronized void ensureRepeats()throws Exception{
  JSONObject all=items();Map<String,JSONObject> latest=new HashMap<>();Set<String> stopped=new HashSet<>();Iterator<String> keys=all.keys();while(keys.hasNext()){JSONObject v=all.getJSONObject(keys.next());String series=v.optString("series",v.optString("id"));if(v.optBoolean("deleted")){if(v.has("series"))stopped.add(series);continue;}JSONObject previous=latest.get(series);if(previous==null||v.optString("date").compareTo(previous.optString("date"))>0)latest.put(series,v);}
  java.time.LocalDate today=java.time.LocalDate.now();for(Map.Entry<String,JSONObject> e:latest.entrySet()){JSONObject v=e.getValue();if(stopped.contains(e.getKey())||v.optString("repeat","none").equals("none"))continue;java.time.LocalDate date;try{date=java.time.LocalDate.parse(v.getString("date"));}catch(Exception ex){continue;}if(!v.optBoolean("done")&&!date.isBefore(today))continue;JSONObject next=Records.nextOccurrence(Records.copy(v).put("done",true));if(next==null)continue;java.time.LocalDate day=Plans.nextOnOrAfter(date,v.optString("repeat"),v.optInt("anchorDay",date.getDayOfMonth()),v.optInt("anchorMonth",date.getMonthValue()),today);if(day==null)continue;next.put("date",day.toString()).put("id",e.getKey()+":"+day);if(item(next.getString("id"))==null)put(next);}
 }
 synchronized void deleteActivity(String id)throws Exception{JSONObject v=item(id);if(v==null)return;String series=v.optString("series",v.optString("id"));if(!v.optString("repeat","none").equals("none")){v.put("series",series);put(v.put("deleted",true));JSONObject all=items();Iterator<String> keys=all.keys();while(keys.hasNext()){JSONObject other=all.getJSONObject(keys.next());if(!other.optBoolean("done")&&!other.optBoolean("deleted")&&series.equals(other.optString("series",other.optString("id"))))put(other.put("series",series).put("deleted",true));}}else put(v.put("deleted",true));}
 synchronized void forgetDeletedHistory()throws Exception{JSONObject history=root.optJSONObject("history");if(history==null)return;JSONObject all=root.getJSONObject("items");Iterator<String> ids=all.keys();while(ids.hasNext()){String id=ids.next();if(all.getJSONObject(id).optBoolean("deleted"))history.remove(id);}save();}
 synchronized boolean merge(JSONObject incoming)throws Exception{return merge(incoming,false);}
 synchronized boolean merge(JSONObject incoming,boolean notify)throws Exception{
  JSONObject all=root.getJSONObject("items"),candidate=new JSONObject(all.toString());java.util.List<JSONObject[]> changes=new java.util.ArrayList<>();Iterator<String> keys=incoming.keys();while(keys.hasNext()){String id=keys.next();JSONObject v=incoming.getJSONObject(id),old=all.optJSONObject(id);if(!v.optString("id").equals(id)||v.optLong("rev")<1)throw new Exception("Copia inválida");if(v.optBoolean("deleted"))v=Records.deleted(v);if(Records.newer(v,old)){candidate.put(id,v);changes.add(new JSONObject[]{old,v});}else if(old!=null&&old.optLong("rev")==v.optLong("rev")&&!old.optString("origin").equals(v.optString("origin"))&&!old.optBoolean("deleted")&&!v.optBoolean("deleted")){remember(v,"Edición simultánea conservada localmente");}}
  if(changes.isEmpty())return false;for(JSONObject[] pair:changes){remember(pair[0],"Antes de recibir un cambio");if(notify&&!pair[1].optString("origin").equals(device()))addNotice(pair[0],pair[1]);}root.put("items",candidate);save();return true;
 }
 synchronized void addNotice(JSONObject old,JSONObject value)throws Exception{JSONArray list=root.optJSONArray("notices");if(list==null)list=new JSONArray();JSONArray trimmed=new JSONArray();for(int j=Math.max(0,list.length()-59);j<list.length();j++)trimmed.put(list.get(j));String action=value.optBoolean("deleted")?"eliminó":old==null?"añadió":value.optBoolean("done")&&!old.optBoolean("done")?"completó":"modificó";String title=value.optBoolean("deleted")?(old==null?"una actividad":old.optString("title","una actividad")):value.optString("title","una actividad");trimmed.put(new JSONObject().put("id",value.optString("id")).put("at",System.currentTimeMillis()).put("text",value.optString("by","Tu pareja")+" "+action+" «"+title+"»"));root.put("notices",trimmed);}
 synchronized JSONArray notices()throws Exception{JSONArray a=root.optJSONArray("notices");return a==null?new JSONArray():new JSONArray(a.toString());}
 synchronized long lastNotice()throws Exception{JSONArray a=notices();return a.length()==0?0:a.getJSONObject(a.length()-1).optLong("at");}
 synchronized void markNoticesRead()throws Exception{root.put("noticesRead",lastNotice());save();}
 synchronized int unread()throws Exception{JSONArray a=notices();int count=0;long read=root.optLong("noticesRead");for(int j=0;j<a.length();j++)if(a.getJSONObject(j).optLong("at")>read)count++;return count;}
 synchronized long stateLong(String key){return root.optLong(key);}
 synchronized void stateLong(String key,long value)throws Exception{root.put(key,value);save();}
}
