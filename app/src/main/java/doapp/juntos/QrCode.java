// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Juntos contributors
package doapp.juntos;
import java.nio.charset.StandardCharsets;
import java.util.*;
/** QR Model 2, version 25, level L, byte mode, mask 0. Fixed size keeps the encoder small. */
public final class QrCode {
 public static final int SIZE=117;
 final boolean[][] cells=new boolean[SIZE][SIZE],function=new boolean[SIZE][SIZE];
 void set(int x,int y,boolean dark){if(x>=0&&y>=0&&x<SIZE&&y<SIZE){cells[y][x]=dark;function[y][x]=true;}}
 void finder(int x,int y){for(int dy=-4;dy<=4;dy++)for(int dx=-4;dx<=4;dx++){int d=Math.max(Math.abs(dx),Math.abs(dy));set(x+dx,y+dy,d!=2&&d!=4);}}
 void alignment(int x,int y){for(int dy=-2;dy<=2;dy++)for(int dx=-2;dx<=2;dx++)set(x+dx,y+dy,Math.max(Math.abs(dx),Math.abs(dy))!=1);}
 void format(){int data=8,rem=data;for(int j=0;j<10;j++)rem=(rem<<1)^((rem>>>9)*0x537);int bits=((data<<10)|rem)^0x5412;for(int j=0;j<=5;j++)set(8,j,bit(bits,j));set(8,7,bit(bits,6));set(8,8,bit(bits,7));set(7,8,bit(bits,8));for(int j=9;j<15;j++)set(14-j,8,bit(bits,j));for(int j=0;j<8;j++)set(SIZE-1-j,8,bit(bits,j));for(int j=8;j<15;j++)set(8,SIZE-15+j,bit(bits,j));set(8,SIZE-8,true);}
 static boolean bit(int x,int p){return ((x>>>p)&1)!=0;}
 void patterns(){for(int i=0;i<SIZE;i++){set(6,i,i%2==0);set(i,6,i%2==0);}finder(3,3);finder(SIZE-4,3);finder(3,SIZE-4);int[] a={6,32,58,84,110};for(int y=0;y<a.length;y++)for(int x=0;x<a.length;x++)if(!(x==0&&y==0||x==0&&y==a.length-1||x==a.length-1&&y==0))alignment(a[x],a[y]);format();int rem=25;for(int i=0;i<12;i++)rem=(rem<<1)^((rem>>>11)*0x1F25);int bits=25<<12|rem;for(int i=0;i<18;i++){boolean v=bit(bits,i);int x=SIZE-11+i%3,y=i/3;set(x,y,v);set(y,x,v);}}
 static int mul(int x,int y){int z=0;for(int i=7;i>=0;i--){z=(z<<1)^((z>>>7)*0x11D);z^=((y>>>i)&1)*x;}return z;}
 static byte[] divisor(){byte[] a=new byte[26];a[25]=1;int root=1;for(int i=0;i<26;i++){for(int j=0;j<a.length;j++){a[j]=(byte)mul(a[j]&255,root);if(j+1<a.length)a[j]^=a[j+1];}root=mul(root,2);}return a;}
 static byte[] ecc(byte[] data,byte[] divisor){byte[] r=new byte[26];for(byte b:data){int f=(b^r[0])&255;System.arraycopy(r,1,r,0,25);r[25]=0;for(int j=0;j<26;j++)r[j]^=(byte)mul(divisor[j]&255,f);}return r;}
 static void append(List<Boolean> bits,int value,int count){for(int i=count-1;i>=0;i--)bits.add(bit(value,i));}
 public static boolean[][] encode(String value){byte[] source=value.getBytes(StandardCharsets.UTF_8);if(source.length>1273)throw new IllegalArgumentException("El código de vinculación es demasiado largo");List<Boolean> bits=new ArrayList<>();append(bits,4,4);append(bits,source.length,16);for(byte b:source)append(bits,b&255,8);append(bits,0,Math.min(4,1276*8-bits.size()));while(bits.size()%8!=0)bits.add(false);for(int pad=0;bits.size()<1276*8;pad++)append(bits,pad%2==0?0xEC:0x11,8);byte[] data=new byte[1276];for(int i=0;i<bits.size();i++)if(bits.get(i))data[i/8]|=1<<(7-i%8);
  byte[][] blocks=new byte[12][133];byte[] div=divisor();int at=0;for(int b=0;b<12;b++){int len=b<8?106:107;byte[] part=Arrays.copyOfRange(data,at,at+len);at+=len;System.arraycopy(part,0,blocks[b],0,len);byte[] check=ecc(part,div);System.arraycopy(check,0,blocks[b],107,26);}byte[] code=new byte[1588];at=0;for(int i=0;i<133;i++)for(int b=0;b<12;b++)if(i!=106||b>=8)code[at++]=blocks[b][i];if(at!=code.length)throw new AssertionError();
  QrCode qr=new QrCode();qr.patterns();int index=0;for(int right=SIZE-1;right>=1;right-=2){if(right==6)right=5;for(int vert=0;vert<SIZE;vert++){int y=((right+1)&2)==0?SIZE-1-vert:vert;for(int j=0;j<2;j++){int x=right-j;if(!qr.function[y][x]){boolean dark=index<code.length*8&&bit(code[index>>>3]&255,7-(index&7));qr.cells[y][x]=dark^((x+y)%2==0);index++;}}}}return qr.cells;
 }
}
