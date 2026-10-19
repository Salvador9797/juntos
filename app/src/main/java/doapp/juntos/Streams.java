// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Juntos contributors
package doapp.juntos;
import java.io.*;
final class Streams {
 static byte[] read(InputStream in,int limit)throws IOException{try(in;ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1){if(out.size()+n>limit)throw new IOException("Archivo demasiado grande");out.write(buf,0,n);}return out.toByteArray();}}
}
