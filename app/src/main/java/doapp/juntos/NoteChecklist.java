// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Juntos contributors
package doapp.juntos;

import java.util.*;
import java.util.regex.*;

/** Conservative, local-only detection; quantities without punctuation stay intact. */
final class NoteChecklist {
 static final class Entry {
  final String text; final boolean done;
  Entry(String text,boolean done){this.text=text;this.done=done;}
 }
 private static final Pattern MARKER=Pattern.compile("^(?:[-*•●▪‣]\\s+|\\d{1,3}[.)]\\s+)");
 private static final Pattern BOX=Pattern.compile("^(?:\\[([ xX])\\]|([☐☑✓✅]))\\s*(.+)$");
 static List<Entry> parse(String note){
  List<Entry> result=new ArrayList<>();
  if(note==null||note.trim().isEmpty())return result;
  String[] lines=note.trim().split("\\R",-1);
  boolean allMarked=true,plainList=true;
  for(String raw:lines){
   String line=raw.trim();
   if(line.isEmpty())return Collections.emptyList(); // paragraphs remain notes
   Matcher marker=MARKER.matcher(line);boolean marked=marker.find();
   if(marked)line=line.substring(marker.end()).trim();
   Matcher box=BOX.matcher(line);boolean done=false;
   if(box.matches()){
    marked=true;done="x".equalsIgnoreCase(box.group(1))||"☑".equals(box.group(2))||"✓".equals(box.group(2))||"✅".equals(box.group(2));line=box.group(3).trim();
   }
   if(line.isEmpty()||line.length()>500)return Collections.emptyList();
   allMarked &= marked;
   // Unmarked lists are short separate items, not sentences or wrapped prose.
   plainList &= !marked&&line.length()<=120&&line.split("\\s+").length<=12&&!line.matches(".*[.!?…:;]$");
   result.add(new Entry(line,done));
  }
  return result.size()>=2&&(allMarked||plainList)?result:Collections.emptyList();
 }
 static String key(String text){return text.trim().replaceAll("\\s+"," ").toLowerCase(Locale.ROOT);}
 private NoteChecklist(){}
}
