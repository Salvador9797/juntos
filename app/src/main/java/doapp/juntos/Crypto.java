// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Juntos contributors
package doapp.juntos;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.*;
import javax.crypto.spec.*;
public final class Crypto {
 static final SecureRandom RANDOM=new SecureRandom();
 static byte[] random(int n){byte[] b=new byte[n]; RANDOM.nextBytes(b); return b;}
 public static byte[] seal(byte[] data, String password) throws Exception {
  byte[] salt=random(16), iv=random(12);
  Cipher c=Cipher.getInstance("AES/GCM/NoPadding"); c.init(Cipher.ENCRYPT_MODE,key(password,salt),new GCMParameterSpec(128,iv)); c.updateAAD(new byte[]{74,85,78,49});
  byte[] enc=c.doFinal(data), out=new byte[32+enc.length];
  System.arraycopy(new byte[]{74,85,78,49},0,out,0,4);System.arraycopy(salt,0,out,4,16);System.arraycopy(iv,0,out,20,12);System.arraycopy(enc,0,out,32,enc.length);return out;
 }
 public static byte[] open(byte[] data,String password)throws Exception{
  if(data.length<48 || data[0]!=74 || data[1]!=85 || data[2]!=78 || data[3]!=49)throw new Exception("Formato inválido");
  Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,key(password,Arrays.copyOfRange(data,4,20)),new GCMParameterSpec(128,Arrays.copyOfRange(data,20,32))); c.updateAAD(new byte[]{74,85,78,49});return c.doFinal(data,32,data.length-32);
 }
 static SecretKeySpec key(String password,byte[] salt)throws Exception {PBEKeySpec spec=new PBEKeySpec(password.toCharArray(),salt,120000,256);try{return new SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(),"AES");}finally{spec.clearPassword();}}
}
