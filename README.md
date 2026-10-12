# JoinTgPlugin

Plugin de Paper que notifica en Telegram cuando jugadores se unen, salen o mueren. También permite mostrar un título in-game y un mensaje en el chat.
**Requisitos**
- Java 21+ (MC 1.21) o Java 25+ (MC 26.x)
- Maven 3.6+
- Servidor **Paper** compatible (usa APIs de Paper como Adventure, `ComponentLogger` y el scheduler asíncrono; no funciona en Spigot/CraftBukkit puros)
- [LuckPerms](https://luckperms.net/) — **opcional**, solo para mostrar prefijos en los mensajes de join

**Construir**

El proyecto usa perfiles de Maven para compilar contra distintas versiones de Minecraft.

| Perfil | Versión MC | Java | Comando |
|--------|-----------|------|---------|
| `mc-26_2` (por defecto) | 26.2 / 26.3 | 25 | `mvn clean package` |
| `mc-26_3` | 26.3 | 25 | `mvn clean package -Pmc-26_3` |
| `mc-26_1` | 26.1 / 26.2 / 26.3 | 25 | `mvn clean package -Pmc-26_1` |
| `mc-1_21` | 1.21 | 21 | `mvn clean package -Pmc-1_21` |

```bash
# Minecraft 26.2 (perfil por defecto, también compatible con 26.3)
mvn clean package

# Minecraft 26.3
mvn clean package -Pmc-26_3

# Minecraft 26.1 (compilado contra la API más antigua: un solo JAR cubre 26.1, 26.2 y 26.3)
mvn clean package -Pmc-26_1

# Minecraft 1.21
mvn clean package -Pmc-1_21
```

> La API de Paper es **retrocompatible**: un plugin compilado contra una API antigua se ejecuta en
> servidores más nuevos. Por eso el JAR de `mc-26_1` sirve para 26.1, 26.2 y 26.3, y el de `mc-26_2`
> sirve también para 26.3. El campo `api-version` de `plugin.yml` se ajusta automáticamente a cada perfil.

El JAR resultante incluye la versión de MC en el nombre, por ejemplo `join-tg-1.5.1-mc26.2.jar`. Cópialo a la carpeta `plugins/` de tu servidor Paper.

**Configuración**

Edita `config.yml`: por defecto `telegram-enabled` está en `false`; cámbialo a `true` y completa `bot-token` + `chat-id` para habilitar las notificaciones de Telegram.

