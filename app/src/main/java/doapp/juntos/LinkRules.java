// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Juntos contributors
package doapp.juntos;
public final class LinkRules {
 public static String opposite(String slot){if("A".equals(slot))return "B";if("B".equals(slot))return "A";throw new IllegalArgumentException("Espacio inválido");}
 public static String localSlot(String configured,String device,String writerA,String writerB)throws Exception{
  String own="A".equals(configured)?writerA:writerB,other="A".equals(configured)?writerB:writerA;
  if(device.equals(own))return configured;
  if(device.equals(other))return opposite(configured);
  if(own.isEmpty())return configured;
  if(other.isEmpty())return opposite(configured);
  throw new Exception("Los dos espacios ya pertenecen a otros celulares. Vincula este celular con un QR nuevo del celular que conserva la agenda.");
 }
 public static String receivingSlot(String encodedSource,String source,String writerA,String writerB)throws Exception{
  String expected="A".equals(encodedSource)?writerA:writerB;
  if(source.equals(expected))return opposite(encodedSource);
  if(source.equals(writerA))return "B";
  if(source.equals(writerB))return "A";
  throw new Exception("La agenda de origen aún no está publicada. Genera otro QR después de sincronizar el primer celular.");
 }
}
