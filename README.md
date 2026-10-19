# Juntos

Agenda Android para organizar actividades en pareja. Software libre bajo
[Apache License 2.0](LICENSE). Android 8 o posterior. Versión 0.3.1, beta pública.

[Página y descarga](https://juntos-agenda-en-pareja.eagerwand17.chatgpt.site/)
· [Privacidad](https://juntos-agenda-en-pareja.eagerwand17.chatgpt.site/privacidad.html)
· [Código y compilación](https://juntos-agenda-en-pareja.eagerwand17.chatgpt.site/codigo/)

**F-Droid: [solicitud #4605 enviada](https://gitlab.com/fdroid/rfp/-/work_items/4605), pendiente de revisión.**

## Funciones

- Actividades con fecha, hora opcional, notas, etiquetas, emoji y responsable.
- Agenda, búsqueda, calendario con días pendientes destacados en rojo.
- Listas de tareas, recordatorios con avisos exactos opcionales y repeticiones.
- Historial y papelera locales y copias exportadas cifradas.
- Widget, avisos de cambios y vinculación por QR sin PIN adicional.
- Asistente guiado que conserva el progreso de configuración.
- Uso local sin conexión; sincronización opcional mediante Telegram.

Cada pareja configura su propio bot, dos canales privados y una clave de
cifrado. Una persona prepara la conexión y la otra se une mediante QR.
La APK no contiene el token del creador ni credenciales compartidas entre
parejas. **El QR sí contiene acceso completo a la agenda de origen**;
solo debe mostrarse a la persona que se va a vincular.

## Compilar

JDK 17, Android SDK 35, Build Tools 34.0.0 y Gradle 8.9. Configura ANDROID_HOME
o un `local.properties` local con `sdk.dir`. Ejecuta:

```sh
./gradlew assembleRelease
```

El resultado sin firma queda en `app/build/outputs/apk/release/`.
Para pruebas locales, `./gradlew assembleDebug` utiliza la firma de depuración
generada por Android. Las claves privadas de publicación no están en este
repositorio. Consulta [publicación y firma](docs/PUBLICATION.md).

## Qué se ha comprobado

Compilación, Android Lint, cifrado autenticado, comparación de revisiones,
recurrencias, reglas del asistente, invitaciones y asignación A/B. El QR se
comparó con un generador independiente. Pruebas JVM en `tests/`.

La sincronización de extremo a extremo, el recorrido completo de configuración,
las notificaciones bajo restricciones de batería y el widget aún requieren
más pruebas en teléfonos reales con bots y canales reales. No se presenta
como una aplicación auditada de forma independiente.

## Límites importantes

- Sincroniza cada 30 segundos en pantalla y mediante tareas de Android en
  segundo plano desde intervalos de 15 minutos; pueden retrasarse.
- Los recordatorios usan alarmas exactas si el usuario concede el acceso en
  Android 12 o superior. Sin el permiso son aproximados. La actividad debe
  haber llegado al celular; no se necesita internet para el aviso local.
- Nosotros → Avisos y batería permite revisar permisos, abrir ajustes y
  probar una notificación en un minuto. Hay recuperación local periódica.
- Forzar la detención impide el trabajo en segundo plano hasta volver a abrir
  la app. Las restricciones del fabricante también pueden impedir avisos.
- Las ediciones simultáneas de una misma actividad no se combinan por campo.
  Prevalece una revisión; las versiones observadas pueden recuperarse desde
  el historial local, sujeto a sus límites.
- El historial no es un registro completo compartido.
- La copia actual de Telegram se actualiza en el mensaje fijado de cada canal.
  No se garantiza el borrado definitivo de archivos antiguos o sustituidos.
- Telegram puede observar datos técnicos y metadatos, aunque los documentos
  de la agenda se cifran antes de enviarse.
- Conservar una exportación y su clave es necesario para recuperar datos.

No incluye publicidad, compras, suscripciones ni SDKs de seguimiento.
Una edición premium es una posibilidad futura, no una función actual.

## Participación

Este proyecto acaba de iniciar su publicación. No se ha habilitado un
buzón de incidencias propio. La propuesta de inclusión y sus datos se
encuentran en `fdroid/`; cualquier estado de envío debe verificarse en
la página del proyecto. No publiques tokens, claves ni QR al pedir ayuda.
