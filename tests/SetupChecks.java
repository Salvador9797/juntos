package doapp.juntos;
public class SetupChecks {
 static int count;
 interface Checked{void run()throws Exception;}
 static void check(boolean b,String label){if(!b)throw new AssertionError(label);System.out.println("PASS: "+label);count++;}
 static void reject(Checked call,String label)throws Exception{boolean rejected=false;try{call.run();}catch(Exception e){rejected=true;}check(rejected,label);}
 public static void main(String[] args)throws Exception{
  check(SetupRules.channelId("-1001234567890").equals("-1001234567890"),"Existing Bot API channel IDs preserved");
  check(SetupRules.channelId(" https://t.me/c/1234567890/17 ").equals("-1001234567890"),"Private message link converts to Bot API channel ID");
  check(SetupRules.channelId("t.me/c/1234567890/8").equals("-1001234567890"),"Pasted Telegram link without scheme accepted");
  check(SetupRules.channelId("https://t.me/c/1234567890/2/99?thread=2").equals("-1001234567890"),"Thread and message IDs do not contaminate channel ID");
  check(SetupRules.channelId("https://telegram.me/c/1234567890/4").equals("-1001234567890"),"Official Telegram alias supported");
  reject(()->SetupRules.channelId("https://t.me/+secretInvite"),"Invitation link is not misread as a channel ID");
  reject(()->SetupRules.channelId("https://t.me/public_channel/2"),"Public username link requires a private channel link");
  reject(()->SetupRules.channelId("https://t.me.attacker.example/c/1234567890/2"),"Lookalike host rejected");
  reject(()->SetupRules.channelId("https://t.me@attacker.example/c/1234567890/2"),"Userinfo host spoofing rejected");
  reject(()->SetupRules.channelId("https://t.me/c/1234567890/"),"Incomplete message link rejected");
  reject(()->SetupRules.channelId("https://t.me/c/not-a-number/2"),"Malformed channel number rejected");
  reject(()->SetupRules.channelId(""),"Empty channel input rejected");
  check(SetupRules.tokenValid(" 12345:FAKE_TOKEN_FOR_TESTS "),"Pasted token outer whitespace handled");
  check(!SetupRules.tokenValid("12345:FAKE TOKEN"),"Broken token with internal whitespace rejected");
  check(!SetupRules.tokenValid("/newbot"),"BotFather command is not mistaken for a token");
  check(SetupRules.shouldStart(false,false,false,false),"New empty installation receives setup guide");
  check(!SetupRules.shouldStart(true,false,false,true),"Existing linked couple bypasses onboarding on update");
  check(!SetupRules.shouldStart(false,false,false,true),"Existing offline activities are not interrupted by forced onboarding");
  check(SetupRules.shouldStart(true,true,false,false),"Incomplete verification draft resumes even after connection fields were saved");
  check(!SetupRules.shouldStart(false,true,true,false),"Explicit postponement is respected while preserving a draft");
  check(SetupRules.step(-9)==0&&SetupRules.step(100)==8,"Restored setup step is bounded");
  System.out.println(count+" setup checks passed");
 }
}
