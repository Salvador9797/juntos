// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Juntos contributors
package doapp.juntos;
import java.net.URI;
public final class SetupRules {
 public static boolean tokenValid(String token){return token!=null&&token.trim().matches("[0-9]+:[A-Za-z0-9_-]+");}
 public static String channelId(String value)throws Exception{String raw=value==null?"":value.trim();if(raw.matches("-100[0-9]+"))return raw;
  if(raw.startsWith("t.me/")||raw.startsWith("telegram.me/"))raw="https://"+raw;
  try{URI uri=new URI(raw);if(!"https".equalsIgnoreCase(uri.getScheme())&&!"http".equalsIgnoreCase(uri.getScheme()))throw new Exception();String host=uri.getHost();if(!("t.me".equalsIgnoreCase(host)||"telegram.me".equalsIgnoreCase(host))||uri.getUserInfo()!=null)throw new Exception();String[] parts=uri.getPath().split("/");if(parts.length<4||!parts[1].equals("c")||!parts[2].matches("[0-9]+")||!parts[3].matches("[0-9]+"))throw new Exception();return "-100"+parts[2];}catch(Exception e){throw new Exception("Pega el ID (-100…) o el enlace de un mensaje del canal privado (t.me/c/…). El enlace de invitación no sirve.");}}
 public static boolean shouldStart(boolean ready,boolean activeDraft,boolean skipped,boolean hasActivities){return !skipped&&(activeDraft||!ready&&!hasActivities);}
 public static int step(int value){return Math.max(0,Math.min(8,value));}
}
