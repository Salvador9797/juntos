// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Juntos contributors
package doapp.juntos;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
/** A self-contained QR invitation. Possession of its complete URI grants access. */
public final class Invitation {
 static final String PREFIX="juntos://vincular/v2/";
 static final String[] KEYS={"token","chatA","chatB","secret","name","partner","sourceDevice","sourceSlot"};
 public static String make(Map<String,String> data,long now)throws Exception{
  validate(data);ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(DataOutputStream out=new DataOutputStream(bytes)){out.writeInt(0x4A554E32);out.writeLong(now+10*60*1000L);for(String key:KEYS)out.writeUTF(data.getOrDefault(key,""));}
  String key=Base64.getUrlEncoder().withoutPadding().encodeToString(Crypto.random(32));byte[] encrypted=Crypto.seal(bytes.toByteArray(),key);
  return PREFIX+Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted)+"#"+key;
 }
 public static Map<String,String> read(String link,String ownDevice,long now)throws Exception{
  if(link==null||!link.startsWith(PREFIX)||link.length()>8000)throw new Exception("Genera un QR nuevo con la versión actualizada de Juntos.");String[] parts=link.substring(PREFIX.length()).split("#",-1);if(parts.length!=2||!parts[1].matches("[A-Za-z0-9_-]{43}"))throw new Exception("El enlace del QR está incompleto. Escanea el QR nuevo completo.");
  byte[] plain=Crypto.open(Base64.getUrlDecoder().decode(parts[0]),parts[1]);Map<String,String> values=new LinkedHashMap<>();try(DataInputStream in=new DataInputStream(new ByteArrayInputStream(plain))){if(in.readInt()!=0x4A554E32)throw new Exception("QR incompatible.");long expires=in.readLong();if(expires<=now||expires>now+11*60*1000L)throw new Exception("El QR caducó o los relojes no coinciden. Genera otro y activa la hora automática.");for(String k:KEYS)values.put(k,in.readUTF());if(in.available()!=0)throw new Exception("QR inválido.");}
  validate(values);if(values.get("sourceDevice").equals(ownDevice))throw new Exception("Escanea este QR con el otro celular.");return values;
 }
 static void validate(Map<String,String> v)throws Exception{String a=v.getOrDefault("chatA",""),b=v.getOrDefault("chatB","");if(!v.getOrDefault("token","").matches("[0-9]+:[A-Za-z0-9_-]+")||!a.matches("-100[0-9]+")||!b.matches("-100[0-9]+")||a.equals(b)||v.getOrDefault("secret","").length()<12||v.getOrDefault("sourceDevice","").isEmpty()||!(v.getOrDefault("sourceSlot","").equals("A")||v.getOrDefault("sourceSlot","").equals("B")))throw new Exception("Revisa el token, los dos canales y la clave antes de generar el QR.");}
}
