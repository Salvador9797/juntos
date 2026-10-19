# Pruebas de lógica sin Android

Las pruebas utilizan Java 17 y no requieren credenciales reales. Compila
Crypto, Versions, Streams, Plans, QrCode, Invitation, LinkRules y SetupRules
junto con las clases `*Checks.java` de este directorio mediante `javac -d`.
Ejecuta CoreChecks, FeatureChecks con un directorio temporal como argumento,
LinkChecks con ese mismo directorio y SetupChecks.

Los resultados guardados describen pruebas realizadas durante el desarrollo.
La comparación independiente del QR utilizó qrcode-terminal, de Kazuhiko
Arase (MIT), únicamente como herramienta de contraste; no se incorpora esa
biblioteca en la aplicación. Los tokens de las pruebas son ficticios.

ReminderChecks cubre las reglas temporales de entrega y recuperación.
`app/src/androidTest` contiene un Instrumentation sin dependencias para el
emulador desechable: permisos bloqueados, recuperación sin duplicados,
programación y comprobación del aviso tras cerrar el proceso. Nunca se
incluye en la APK de publicación. No usar sobre una agenda con datos reales.
