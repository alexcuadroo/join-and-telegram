# Changelog

## v1.5.1

### Corrección: crash al entrar sin LuckPerms

- `PluginListener` referenciaba directamente clases del API de LuckPerms, lo que provocaba un `NoClassDefFoundError` (no capturable como `Exception`) al unirse un jugador si LuckPerms no estaba instalado.
- Se aísla la integración en `dev.jointg.integration.PrefixProvider` y `LuckPermsPrefixProvider`, que solo se instancian si LuckPerms está presente. LuckPerms vuelve a ser **totalmente opcional**: sin él el plugin funciona y el `<prefix>` queda vacío.

## v1.5.0

### Soporte para Minecraft 26.2 y 26.3

- Añadidos perfiles de Maven `mc-26_2` (activo por defecto) y `mc-26_3`, compilando contra las APIs estables `26.2.build.133-stable` y `26.3.build.176-stable`.
- Actualizado el perfil `mc-26_1` a la API estable `26.1.2.build.74-stable`.
- Como la API de Paper es retrocompatible, el JAR de `mc-26_1` cubre 26.1/26.2/26.3 y el de `mc-26_2` cubre 26.2/26.3.
- `plugin.yml` ahora toma `version` de `${project.version}` para evitar que la versión se desincronice del `pom.xml`.

### Limpieza y buenas prácticas

- `/jointg reload` ya no envía el mensaje `start-message` a Telegram; solo se anuncia el arranque al habilitar el plugin.
- Eliminados los códigos de color `§` embebidos en el comando; ahora se usa MiniMessage.
- El parsing de la respuesta de GitHub usa Gson (incluido en Paper) en lugar de expresiones regulares.
- `TelegramNotifier` ya no crea un `HttpClient` cuando las notificaciones están deshabilitadas y ahora registra respuestas HTTP de error de Telegram.
- Eliminada la comprobación muerta de `notifier != null` y comprobaciones duplicadas de `getCommand("jointg")` / registros de listener sin desregistrar.
- Se elimina el plugin `maven-shade-plugin`, que no aportaba nada al ser todas las dependencias `provided`.

## v1.4.5

### Tab completion para /jointg

- Se añade `TabCompleter` al comando `/jointg` para que `reload` aparezca como sugerencia al presionar Tab.

## v1.4.4

### Dependencia de LuckPerms

- Se añade un aviso en consola al iniciar el plugin si **LuckPerms** no está instalado.
- Se declara `LuckPerms` como `softdepend` en `plugin.yml`.
- Se documenta LuckPerms como requisito en el README.

## v1.4.3

### Soporte multi-versión de Minecraft

- Añadidos perfiles de Maven para compilar contra **Minecraft 1.21** y **Minecraft 26.1**.
- El perfil `mc-26_1` es el activo por defecto (Java 25, Paper API `26.1.1.build.15-alpha`).
- El perfil `mc-1_21` permite seguir compilando para servidores 1.21 (Java 21, Paper API `1.21.1-R0.1-SNAPSHOT`).
- El JAR resultante ahora incluye la versión de MC en el nombre (e.g. `join-tg-1.4.2-mc26.1.jar`).
- `plugin.yml` usa filtrado de Maven para establecer `api-version` según el perfil activo.

### Cómo compilar

```bash
# Minecraft 26.1 (por defecto)
mvn clean package

# Minecraft 1.21
mvn clean package -Pmc-1_21
```
