package doapp.juntos;
import java.util.*;
import java.nio.file.*;
public class LinkChecks {
 static int count;
 interface Checked {void run()throws Exception;}
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);count++;System.out.println("PASS: "+message);}
 static void rejects(Checked call,String message)throws Exception{boolean no=false;try{call.run();}catch(Exception e){no=true;}check(no,message);}
 static Map<String,String> config(String slot){Map<String,String> c=new LinkedHashMap<>();c.put("token","123456789:FAKE_TOKEN_FOR_OFFLINE_TESTS");c.put("chatA","-1001111111111");c.put("chatB","-1002222222222");c.put("secret","Clave de prueba muy privada ❤️");c.put("name","María");c.put("partner","Salvador");c.put("sourceDevice","source-phone");c.put("sourceSlot",slot);return c;}
 public static void main(String[] args)throws Exception{
  long now=1800000000000L;Map<String,String> source=config("A");String link=Invitation.make(source,now);Map<String,String> read=Invitation.read(link,"receiving-phone",now+1000);
  check(read.equals(source),"Self-contained QR round trip preserves token, channel IDs, secret and Unicode names without a PIN");
  check(!link.contains(source.get("token"))&&!link.contains("Clave"),"QR URI does not contain plaintext credentials");
  check(LinkRules.receivingSlot(read.get("sourceSlot"),read.get("sourceDevice"),"source-phone","").equals("B"),"Source in A assigns B to receiving phone");
  Map<String,String> sourceB=Invitation.read(Invitation.make(config("B"),now),"receiving-phone",now);
  check(LinkRules.receivingSlot(sourceB.get("sourceSlot"),sourceB.get("sourceDevice"),"","source-phone").equals("A"),"Source in B assigns A to receiving phone");
  check(LinkRules.localSlot("A","source-phone","source-phone","partner-phone").equals("A"),"Keeps source A when cloud ownership is correct");
  check(LinkRules.localSlot("B","source-phone","source-phone","").equals("A"),"Repairs wrong local slot using existing channel ownership");
  check(LinkRules.localSlot("A","receiving-phone","source-phone","").equals("B"),"Two phones configured as A are assigned separate spaces when B is empty");
  check(LinkRules.localSlot("B","receiving-phone","source-phone","receiving-phone").equals("B"),"Existing linked receiving phone keeps its own space");
  check(LinkRules.receivingSlot("A","source-phone","","source-phone").equals("A"),"QR receiving assignment follows actual source ownership if local configuration was stale");
  check(LinkRules.receivingSlot("A","source-phone","source-phone","source-phone").equals("B"),"Explicit source slot disambiguates legacy copies published by the same phone");
  rejects(()->LinkRules.localSlot("A","new-phone","owner-a","owner-b"),"Normal sync cannot overwrite two occupied spaces");
  rejects(()->LinkRules.receivingSlot("A","source-phone","",""),"Joining refuses to report success before source snapshot exists");
  rejects(()->Invitation.read(link,"source-phone",now),"Source cannot join its own QR");
  rejects(()->Invitation.read(link,"other",now+600001),"Expired QR rejected");
  rejects(()->Invitation.read(link.substring(0,link.indexOf('#')),"other",now),"QR missing embedded key is rejected instead of asking for a PIN");
  String[] p=link.split("#");String tampered=p[0].substring(0,p[0].length()-12)+"AAAAAAAAAAAA#"+p[1];rejects(()->Invitation.read(tampered,"other",now),"Tampered invitation rejected");
  rejects(()->Invitation.read("juntos://vincular/legacy","other",now),"Legacy two-step invitation requests a newly generated QR");
  Map<String,String> duplicate=config("A");duplicate.put("chatB",duplicate.get("chatA"));rejects(()->Invitation.make(duplicate,now),"Cannot create QR with duplicate channels");
  Map<String,String> finalBad=config("A");finalBad.put("token","");rejects(()->Invitation.make(finalBad,now),"Cannot create QR with empty token");
  check(!Invitation.make(source,now).equals(link),"Every QR invitation has a fresh random key, salt and IV");
  boolean[][] qr=QrCode.encode(link);StringBuilder grid=new StringBuilder();for(boolean[] row:qr){for(boolean b:row)grid.append(b?'1':'0');grid.append('\n');}Files.createDirectories(Path.of(args[0]));Files.writeString(Path.of(args[0],"payload-3.txt"),link);Files.writeString(Path.of(args[0],"qr-3.txt"),grid);
  check(link.getBytes(java.nio.charset.StandardCharsets.UTF_8).length<=1273,"Actual self-contained invitation fits the QR capacity");
  System.out.println(count+" linking checks passed");
 }
}
