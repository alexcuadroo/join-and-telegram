# Changelog

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
