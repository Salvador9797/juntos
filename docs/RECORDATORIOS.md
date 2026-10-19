# Recordatorios con la app cerrada — 0.3.1

Abre Nosotros → Avisos y batería. Permite las notificaciones y el canal de
recordatorios. En Android 12 o superior, activa el acceso de alarmas exactas.
La app conserva una alternativa aproximada si no se concede.

Pulsa Probar aviso en 1 minuto, vuelve a Inicio y apaga la pantalla. No uses
Forzar detención. Si no llega, revisa Batería en los ajustes de Juntos y, en
Samsung, las listas de aplicaciones en suspensión profunda. Estos ajustes
dependen del fabricante y no se modifican automáticamente.

Los avisos de fecha/hora se programan localmente con AlarmManager y no
necesitan Telegram, internet ni una actividad visible. Un receptor con bloqueo
de CPU acotado termina el trabajo local al despertar el dispositivo. Una
tarea persistente sin requisito de red recupera avisos no entregados en las
últimas 24 horas, cuando Android le permite ejecutarse. Reinicio, cambio de
hora, actualización y concesión de permiso vuelven a programar los avisos.
No se marca una entrega cuando el permiso o el canal está bloqueado.

Los cambios de la pareja tienen otro mecanismo: sincronización con Telegram.
El trabajo periódico de Android no garantiza entrega inmediata. La actividad
debe descargarse primero al celular antes de que exista un recordatorio local.
No hay un servicio persistente ni mensajes push en esta versión.

Android puede impedir toda ejecución tras Forzar detención o con restricciones
del fabricante. No se promete superar esas restricciones.
