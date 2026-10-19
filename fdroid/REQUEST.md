# Juntos — request for packaging (draft, not submitted)

* Application ID: doapp.juntos
* License: Apache-2.0
* Website: https://juntos-agenda-en-pareja.eagerwand17.chatgpt.site
* Source information: https://juntos-agenda-en-pareja.eagerwand17.chatgpt.site/codigo/
* Public Git (read-only HTTPS): https://juntos-agenda-en-pareja.eagerwand17.chatgpt.site/git/juntos.git
* Release tag: v0.3.0 (versionCode 6)
* Proposed metadata: https://juntos-agenda-en-pareja.eagerwand17.chatgpt.site/fdroid/doapp.juntos.yml
* Privacy: https://juntos-agenda-en-pareja.eagerwand17.chatgpt.site/privacidad.html

Juntos is a native Android shared planner with dates, notes, tags, emoji,
checklists, reminders and a calendar. It works locally without an account.
Optional two-device synchronization uses the users' own Telegram bot and two
private channels, with AES-GCM encrypted snapshots. The linking QR includes
credentials and must remain private. No analytics, advertisements or purchases.

I am submitting on behalf of the project owner, who authorized publication
under Apache-2.0. The repository contains the Gradle wrapper and Java source;
there are no third-party runtime dependencies. Gradle 8.9, AGP 8.7.3, JDK 17,
compile/target SDK 35 and minimum SDK 26. Build from app using gradle yes.
Signing is configured only via optional private environment variables; without
those variables assembleRelease produces an unsigned APK for F-Droid signing.

Proposed anti-feature: NonFreeNet, because optional synchronization uses Telegram.
There are no proprietary SDKs. This is the initial public beta. Android lint and
JVM logic checks passed; real-device end-to-end Telegram testing is still pending.
F-Droid inclusion and the proposed metadata have not been reviewed or approved.
The Git repository is self-hosted as a read-only dumb-HTTPS repository.
