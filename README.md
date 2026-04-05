# JoinTgPlugin

Plugin de Paper que notifica en Telegram cuando jugadores se unen, salen o mueren. También permite mostrar un título in-game y un mensaje en el chat.
**Requisitos**
- Java 21+ (MC 1.21) o Java 25+ (MC 26.1)
- Maven 3.6+
- Servidor Paper compatible

**Construir**

El proyecto usa perfiles de Maven para compilar contra distintas versiones de Minecraft.

| Perfil | Versión MC | Java | Comando |
|--------|-----------|------|---------|
| `mc-26_1` (por defecto) | 26.1 | 25 | `mvn clean package` |
| `mc-1_21` | 1.21 | 21 | `mvn clean package -Pmc-1_21` |

```bash
# Minecraft 26.1 (perfil por defecto)
mvn clean package

# Minecraft 1.21
mvn clean package -Pmc-1_21
```

El JAR resultante incluye la versión de MC en el nombre, por ejemplo `join-tg-1.4.2-mc26.1.jar`. Cópialo a la carpeta `plugins/` de tu servidor Paper.

**Configuración**

Edita `config.yml`: por defecto `telegram-enabled` está en `false`; cámbialo a `true` y completa `bot-token` + `chat-id` para habilitar las notificaciones de Telegram.

