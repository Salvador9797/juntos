# Publicación de Juntos

La edición gratuita es software libre bajo Apache-2.0. La solicitud de
F-Droid requiere revisión y no garantiza aceptación. Se envió la solicitud
[#4605](https://gitlab.com/fdroid/rfp/-/work_items/4605) el 19 de octubre de 2026.

## Firma y actualizaciones

El código público no incluye ningún almacén de claves ni contraseñas.
El APK de descarga directa se firma con el certificado usado en las
versiones privadas anteriores, conservando su identidad. El contenedor
privado de esa clave se protege con una contraseña nueva.

Sin variables de firma, `./gradlew assembleRelease` genera un APK sin
firmar, apropiado para que F-Droid lo compile y firme por su cuenta.
Para la publicación propia se usan JUNTOS_RELEASE_KEYSTORE,
JUNTOS_RELEASE_STORE_PASSWORD, JUNTOS_RELEASE_KEY_ALIAS y, si es distinto,
JUNTOS_RELEASE_KEY_PASSWORD; no deben guardarse en Git.

Una compilación firmada por F-Droid con un certificado distinto no puede
actualizar la APK de descarga directa. Antes de cambiar entre canales hay
que exportar una copia, conservar su clave y planificar la recuperación.
La reinstalación cambia la identidad del dispositivo y puede requerir
volver a vincular ambos celulares. La posibilidad de usar la firma del
autor mediante compilaciones reproducibles queda pendiente de verificar
con F-Droid; no se promete intercambiabilidad de APK.

## Clasificación de red

La app puede funcionar de manera local y sin internet. Su sincronización
opcional utiliza Telegram Bot API y no incorpora SDKs de Firebase,
Google Play Services, publicidad o analítica. Se propone declarar
NonFreeNet porque promueve un servicio de red propietario para sincronizar.
La clasificación definitiva corresponde a F-Droid.

## Funciones premium futuras

No existe una edición premium ni hay cobros en la app actual. Una edición
adicional puede prepararse más adelante bajo los términos de Apache-2.0,
con identificador y distribución propios según sus características. La
licencia de esta edición ya publicada no se revoca ni se convierte en
privativa por crear una edición distinta. Otras personas también pueden
reutilizar y redistribuir el código respetando sus obligaciones.
