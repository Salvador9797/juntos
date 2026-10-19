package doapp.juntos;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
public class CoreChecks {
 static int count;
 static void check(boolean b,String what){if(!b)throw new AssertionError(what);System.out.println("PASS: "+what);count++;}
 public static void main(String[] args)throws Exception{
  String password="Una clave compartida 2026";
  byte[] plain="Cena con mi esposa ❤️ · notas privadas · ñ".getBytes(StandardCharsets.UTF_8);
  byte[] one=Crypto.seal(plain,password),two=Crypto.seal(plain,password);
  check(Arrays.equals(plain,Crypto.open(one,password)),"Encrypted backup round trip with emoji and Spanish");
  check(!Arrays.equals(one,two),"Fresh salt and IV for every backup");
  check(!new String(one,StandardCharsets.UTF_8).contains("Cena"),"No plaintext activity title in encrypted backup");
  boolean rejected=false;try{Crypto.open(one,"Otra contraseña totalmente distinta");}catch(Exception e){rejected=true;}check(rejected,"Wrong password rejected");
  one[one.length-1]^=1;rejected=false;try{Crypto.open(one,password);}catch(Exception e){rejected=true;}check(rejected,"Tampered ciphertext rejected");
  rejected=false;try{Crypto.open(new byte[3],password);}catch(Exception e){rejected=true;}check(rejected,"Truncated backup rejected");
  byte[] bad=two.clone();bad[0]=0;rejected=false;try{Crypto.open(bad,password);}catch(Exception e){rejected=true;}check(rejected,"Unrecognized envelope rejected");
  check(Versions.compare(2,"a",1,"z")>0,"Newer revision wins regardless of device");
  check(Versions.compare(1,"b",1,"a")>0,"Concurrent edits have a deterministic winner");
  check(Versions.compare(1,"a",1,"b")<0,"Concurrent comparison is symmetric");
  check(Versions.compare(1,"a",1,"a")==0,"Repeated revisions are idempotent");
  check(Versions.compare(Long.MAX_VALUE,"a",1,"a")>0,"Revision comparison avoids integer overflow");
  java.util.Random random=new java.util.Random(7);for(int i=0;i<10000;i++){long ar=random.nextInt(100),br=random.nextInt(100);String a="device"+random.nextInt(10),b="device"+random.nextInt(10);if(Integer.signum(Versions.compare(ar,a,br,b))!=-Integer.signum(Versions.compare(br,b,ar,a)))throw new AssertionError("Comparison symmetry");}check(true,"10000 version pairs converge independent of merge order");
  check(Arrays.equals(Streams.read(new java.io.ByteArrayInputStream(plain),1000),plain),"Bounded stream reader retains complete data");
  rejected=false;try{Streams.read(new java.io.ByteArrayInputStream(plain),2);}catch(java.io.IOException e){rejected=true;}check(rejected,"Oversize stream rejected");
  System.out.println(count+" checks passed");
 }
}
