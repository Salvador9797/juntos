// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Juntos contributors
package doapp.juntos;
import org.json.*;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
final class TelegramSync {
 static final Object SYNC_LOCK=new Object();
 final Store store; final JSONObject config; final String token; static final String MARKER="Juntos • copia cifrada v1";
 TelegramSync(Store s)throws Exception{this(s,s.config());}
 TelegramSync(Store s,JSONObject c)throws Exception{store=s;config=new JSONObject(c.toString());token=config.optString("token");}
 static final class SyncError extends Exception{SyncError(String s){super(s);}}
 byte[] read(InputStream in)throws Exception{try(in;ByteArrayOutputStream b=new ByteArrayOutputStream()){byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1){if(b.size()+n>10*1024*1024)throw new SyncError("La copia supera 10 MB.");b.write(buf,0,n);}return b.toByteArray();}}
 JSONObject request(String method,JSONObject params)throws Exception{
  HttpURLConnection c=(HttpURLConnection)new URL("https://api.telegram.org/bot"+token+"/"+method).openConnection(); c.setConnectTimeout(15000);c.setReadTimeout(20000);c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");
  try{try(OutputStream out=c.getOutputStream()){out.write(params.toString().getBytes(StandardCharsets.UTF_8));}InputStream in=c.getResponseCode()<400?c.getInputStream():c.getErrorStream();if(in==null)throw new SyncError("Telegram no respondió.");JSONObject response=new JSONObject(new String(read(in),StandardCharsets.UTF_8));if(!response.optBoolean("ok"))throw new SyncError("Telegram rechazó la conexión. Revisa token, canales y permisos de administrador.");return response;}finally{c.disconnect();}
 }
 JSONObject remote(String chat)throws Exception{
  JSONObject info=request("getChat",new JSONObject().put("chat_id",chat)).getJSONObject("result");
  if(!info.optString("type").equals("channel"))throw new SyncError("Usa dos canales privados de Telegram, no chats ni grupos.");
  JSONObject pin=info.optJSONObject("pinned_message");if(pin==null)return null;
  if(!MARKER.equals(pin.optString("caption")))throw new SyncError("Hay un mensaje fijado ajeno a Juntos. Usa canales dedicados sin otros mensajes fijados.");
  JSONObject doc=pin.optJSONObject("document");if(doc==null)throw new SyncError("Copia de Juntos no válida.");
  String path=request("getFile",new JSONObject().put("file_id",doc.getString("file_id"))).getJSONObject("result").getString("file_path");
  if(!path.matches("[A-Za-z0-9_./-]+")||path.contains(".."))throw new SyncError("Ruta de archivo inválida.");
  HttpURLConnection c=(HttpURLConnection)new URL("https://api.telegram.org/file/bot"+token+"/"+path).openConnection();c.setConnectTimeout(15000);c.setReadTimeout(20000);
  try{byte[] plain;try{plain=Crypto.open(read(c.getInputStream()),config.getString("secret"));}catch(javax.crypto.AEADBadTagException e){throw new SyncError("La clave compartida no coincide con la copia. No se enviaron cambios.");}JSONObject result=new JSONObject(new String(plain,StandardCharsets.UTF_8));if(result.optInt("schema")!=1)throw new SyncError("Formato de copia incompatible.");result.put("_message",pin.getInt("message_id"));return result;}finally{c.disconnect();}
 }
 void upload(String chat,JSONObject snapshot,int previousMessage)throws Exception{
  byte[] data=Crypto.seal(snapshot.toString().getBytes(StandardCharsets.UTF_8),config.getString("secret"));if(data.length>9*1024*1024)throw new SyncError("La agenda supera el límite de esta versión.");
  String boundary="Juntos"+java.util.UUID.randomUUID(); HttpURLConnection c=(HttpURLConnection)new URL("https://api.telegram.org/bot"+token+(previousMessage>0?"/editMessageMedia":"/sendDocument")).openConnection();c.setRequestMethod("POST");c.setDoOutput(true);c.setConnectTimeout(15000);c.setReadTimeout(30000);c.setRequestProperty("Content-Type","multipart/form-data; boundary="+boundary);
  int message;
  try{try(OutputStream out=c.getOutputStream()){
   java.util.List<String[]> fields=new java.util.ArrayList<>();fields.add(new String[]{"chat_id",chat});if(previousMessage>0){fields.add(new String[]{"message_id",String.valueOf(previousMessage)});fields.add(new String[]{"media",new JSONObject().put("type","document").put("media","attach://document").put("caption",MARKER).toString()});}else{fields.add(new String[]{"caption",MARKER});fields.add(new String[]{"disable_notification","true"});}for(String[] p:fields){out.write(("--"+boundary+"\r\nContent-Disposition: form-data; name=\""+p[0]+"\"\r\n\r\n"+p[1]+"\r\n").getBytes(StandardCharsets.UTF_8));}
   out.write(("--"+boundary+"\r\nContent-Disposition: form-data; name=\"document\"; filename=\"juntos.enc\"\r\nContent-Type: application/octet-stream\r\n\r\n").getBytes(StandardCharsets.UTF_8));out.write(data);out.write(("\r\n--"+boundary+"--\r\n").getBytes(StandardCharsets.UTF_8));}
   InputStream in=c.getResponseCode()<400?c.getInputStream():c.getErrorStream();if(in==null)throw new SyncError("No se pudo enviar la copia.");JSONObject r=new JSONObject(new String(read(in),StandardCharsets.UTF_8));if(!r.optBoolean("ok"))throw new SyncError("No se pudo guardar en Telegram. Revisa permisos.");message=r.getJSONObject("result").getInt("message_id");
  }finally{c.disconnect();}
  if(previousMessage<=0)request("pinChatMessage",new JSONObject().put("chat_id",chat).put("message_id",message).put("disable_notification",true));
 }
 String sync()throws Exception{synchronized(SYNC_LOCK){return runSync();}}
 String runSync()throws Exception{
  if(token.isEmpty())return "Solo en este celular";
  store.ensureRepeats();long before=store.lastNotice();boolean initialized=store.stateLong("syncInitialized")>0;JSONObject a=remote(config.getString("chatA")),b=remote(config.getString("chatB"));boolean pairing=config.optBoolean("pairingPending");String target;
  try{target=pairing?LinkRules.receivingSlot(config.optString("pairSourceSlot"),config.optString("pairSource"),writer(a),writer(b)):LinkRules.localSlot(config.optString("slot","A"),store.device(),writer(a),writer(b));}catch(Exception e){throw new SyncError(e.getMessage());}
  if(!config.optString("slot").equals(target)){JSONObject current=store.config();current.put("slot",target);store.config(current);config.put("slot",target);}
  JSONObject own=target.equals("A")?a:b;
  // A fresh invitation explicitly authorizes replacing the receiving phone after merging both copies.
  if(!pairing&&own!=null&&!own.optString("writer").equals(store.device()))throw new SyncError("El espacio está ocupado. Vincula este celular con un QR nuevo.");
  checkConfiguration();if(Thread.currentThread().isInterrupted())throw new SyncError("Sincronización aplazada por Android.");
  if(a!=null)store.merge(a.getJSONObject("items"),true);if(b!=null)store.merge(b.getJSONObject("items"),true);
  store.ensureRepeats();JSONObject all=store.items();boolean upload=pairing||own==null||!own.optString("appVersion").equals("0.3.2")||!same(all,own.getJSONObject("items"));
  if(upload){checkConfiguration();JSONObject snapshot=new JSONObject().put("schema",1).put("writer",store.device()).put("appVersion","0.3.2").put("items",all);upload(config.getString(config.optString("slot","A").equals("A")?"chatA":"chatB"),snapshot,own==null?0:own.optInt("_message"));}
  if(pairing){JSONObject current=store.config();current.remove("pairingPending");current.remove("pairSource");current.remove("pairSourceSlot");store.config(current);}
  if(initialized)Alerts.change(store.context,store,before);store.stateLong("syncInitialized",1);Alerts.refresh(store.context);
  return "Sincronizado · "+java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
 }
 void checkConfiguration()throws Exception{JSONObject current=store.config();for(String key:new String[]{"token","chatA","chatB","secret","slot"})if(!config.optString(key).equals(current.optString(key)))throw new SyncError("La configuración cambió. Vuelve a sincronizar.");}
 static String writer(JSONObject snapshot){return snapshot==null?"":snapshot.optString("writer");}
 static boolean same(JSONObject a,JSONObject b)throws Exception{if(a.length()!=b.length())return false;java.util.Iterator<String> keys=a.keys();while(keys.hasNext()){String id=keys.next();JSONObject av=a.getJSONObject(id),bv=b.optJSONObject(id);if(bv==null||Versions.compare(av.optLong("rev"),av.optString("origin"),bv.optLong("rev"),bv.optString("origin"))!=0)return false;}return true;}
}
