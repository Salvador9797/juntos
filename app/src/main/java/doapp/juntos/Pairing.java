// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Juntos contributors
package doapp.juntos;
import org.json.*;
import java.util.*;
final class Pairing {
 static String make(Store store)throws Exception{JSONObject c=store.config();Map<String,String> values=new LinkedHashMap<>();for(String key:new String[]{"token","chatA","chatB","secret"})values.put(key,c.optString(key));values.put("name",c.optString("partner","Mi pareja"));values.put("partner",c.optString("name","Yo"));values.put("sourceDevice",store.device());values.put("sourceSlot",c.optString("slot","A"));return Invitation.make(values,System.currentTimeMillis());}
 static JSONObject read(String link,String ownDevice)throws Exception{Map<String,String> values=Invitation.read(link,ownDevice,System.currentTimeMillis());JSONObject c=new JSONObject();for(String key:new String[]{"token","chatA","chatB","secret","name","partner"})c.put(key,values.get(key));return c.put("slot",LinkRules.opposite(values.get("sourceSlot"))).put("pairingPending",true).put("pairSource",values.get("sourceDevice")).put("pairSourceSlot",values.get("sourceSlot"));}
}
