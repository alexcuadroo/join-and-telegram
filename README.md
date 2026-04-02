# JoinTgPlugin

Plugin de Paper que notifica en Telegram cuando jugadores se unen, salen o mueren. También permite mostrar un título in-game y un mensaje en el chat.
**Requisitos**
- Java 21+
- Maven 3.6+
- Servidor Paper/Spigot compatible

**Construir**

```bash
mvn clean package
```

El artefacto resultante estará en `target/`. Copia el JAR a la carpeta `plugins/` de tu servidor Paper.

**Configuración**

Edita `config.yml`: por defecto `telegram-enabled` está en `false`; cámbialo a `true` y completa `bot-token` + `chat-id` para habilitar las notificaciones de Telegram.

**CI**: Incluye un workflow de GitHub Actions que compila con Maven.
