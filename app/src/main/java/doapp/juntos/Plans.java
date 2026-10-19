// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Juntos contributors
package doapp.juntos;
import java.time.*;
public final class Plans {
 public static LocalDate next(LocalDate current,String repeat,int anchorDay,int anchorMonth){
  switch(repeat){case "daily":return current.plusDays(1);case "weekly":return current.plusWeeks(1);case "monthly":YearMonth m=YearMonth.from(current).plusMonths(1);return m.atDay(Math.min(Math.max(1,anchorDay),m.lengthOfMonth()));case "yearly":int y=current.getYear()+1;YearMonth ym=YearMonth.of(y,Math.max(1,Math.min(12,anchorMonth)));return ym.atDay(Math.min(Math.max(1,anchorDay),ym.lengthOfMonth()));default:return null;}
 }
 public static LocalDate nextOnOrAfter(LocalDate date,String repeat,int anchorDay,int anchorMonth,LocalDate floor){LocalDate next=next(date,repeat,anchorDay,anchorMonth);int guard=0;while(next!=null&&next.isBefore(floor)&&guard++<100000)next=next(next,repeat,anchorDay,anchorMonth);return next;}
 public static long reminder(String date,String time,int minutes,ZoneId zone){if(minutes<0||time.isEmpty())return -1;try{return LocalDateTime.of(LocalDate.parse(date),LocalTime.parse(time)).atZone(zone).toInstant().toEpochMilli()-minutes*60000L;}catch(Exception e){return -1;}}
}
