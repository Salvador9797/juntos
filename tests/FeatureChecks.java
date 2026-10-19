package doapp.juntos;
import java.time.*;
import java.io.*;
import java.nio.file.*;
public class FeatureChecks {
 static int count;
 static void check(boolean pass,String label){if(!pass)throw new AssertionError(label);System.out.println("PASS: "+label);count++;}
 public static void main(String[] args)throws Exception{
  check(Plans.next(LocalDate.of(2026,1,31),"monthly",31,1).equals(LocalDate.of(2026,2,28)),"Monthly recurrence clamps end of month");
  check(Plans.next(LocalDate.of(2026,2,28),"monthly",31,1).equals(LocalDate.of(2026,3,31)),"Monthly recurrence preserves original day after February");
  check(Plans.next(LocalDate.of(2024,2,29),"yearly",29,2).equals(LocalDate.of(2025,2,28)),"Leap day anniversary in common year");
  check(Plans.next(LocalDate.of(2027,2,28),"yearly",29,2).equals(LocalDate.of(2028,2,29)),"Leap day anniversary recovers February 29");
  check(Plans.next(LocalDate.of(2026,12,31),"daily",31,12).equals(LocalDate.of(2027,1,1)),"Daily recurrence crosses year boundary");
  check(Plans.next(LocalDate.of(2026,10,19),"weekly",19,10).equals(LocalDate.of(2026,10,26)),"Weekly recurrence keeps weekday");
  check(Plans.next(LocalDate.now(),"none",1,1)==null,"Nonrepeating activity does not recur");
  check(Plans.nextOnOrAfter(LocalDate.of(2020,1,31),"monthly",31,1,LocalDate.of(2026,10,19)).equals(LocalDate.of(2026,10,31)),"Overdue monthly series resumes without duplicating all missed instances");
  check(Plans.nextOnOrAfter(LocalDate.of(2020,1,1),"daily",1,1,LocalDate.of(2026,10,19)).equals(LocalDate.of(2026,10,19)),"Overdue daily series resumes on today");
  ZoneId rd=ZoneId.of("America/Santo_Domingo");long expected=LocalDateTime.of(2026,10,19,9,30).atZone(rd).toInstant().toEpochMilli();
  check(Plans.reminder("2026-10-19","10:00",30,rd)==expected,"Reminder honors Dominican timezone and lead time");
  check(Plans.reminder("2026-10-19","",30,rd)==-1,"Reminder needs a time");
  check(Plans.reminder("2026-10-19","10:00",-1,rd)==-1,"Disabled reminder is not scheduled");
  check(Plans.reminder("invalid","10:00",30,rd)==-1,"Invalid reminder date is rejected");
  String[] payloads={"juntos://vincular/test", "juntos://vincular/"+"AbCd0123_-".repeat(65),"X".repeat(1273)};
  Files.createDirectories(Path.of(args[0]));
  for(int p=0;p<payloads.length;p++){boolean[][] qr=QrCode.encode(payloads[p]);check(qr.length==117&&qr[0].length==117,"QR dimensions for payload "+p);StringBuilder out=new StringBuilder();for(boolean[] line:qr){for(boolean bit:line)out.append(bit?'1':'0');out.append('\n');}Files.writeString(Path.of(args[0],"qr-"+p+".txt"),out);Files.writeString(Path.of(args[0],"payload-"+p+".txt"),payloads[p]);}
  boolean rejected=false;try{QrCode.encode("X".repeat(1274));}catch(IllegalArgumentException e){rejected=true;}check(rejected,"QR oversize input is rejected");
  System.out.println(count+" feature checks passed");
 }
}
