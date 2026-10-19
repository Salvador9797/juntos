// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Juntos contributors
package doapp.juntos;
import android.content.*;
public class ReminderReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c,Intent i){Context app=c.getApplicationContext();boolean test=Alerts.TEST.equals(i.getAction());ReceiverWork.run(app,goAsync(),()->{if(test)Alerts.show(app,Alerts.REMIND,9002,"Juntos · Prueba de aviso","El recordatorio llegó sin abrir la app.","test");else{Alerts.invalidateSchedule(app);Alerts.fire(app);}});}
}
