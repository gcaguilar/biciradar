# BiciRadar para Garmin Connect IQ

Widget que muestra las estaciones de bicis más cercanas en tu reloj Garmin.

## Requisitos para construir

### 1. Iconos de Launcher

Garmin requiere iconos PNG en múltiples tamaños. Crea las siguientes imágenes (PNG con transparencia):

| Directorio | Tamaño | Uso |
|-----------|--------|-----|
| 30x30 | 30px | Pantallas pequeñas |
| 40x40 | 40px | - |
| 50x50 | 50px | - |
| 60x60 | 60px | - |
| 70x70 | 70px | - |
| 90x90 | 90px | - |
| 100x100 | 100px | Predeterminado |
| 110x110 | 110px | - |
| 140x140 | 140px | - |
| 150x150 | 150px | - |
| 180x180 | 180px | - |
| 210x210 | 210px | Pantallas grandes |
| 220x220 | 220px | - |

**Sugerencia:** Crea un icono de 512x512 y redimensiona a todos los tamaños.

### 2. Desarrollo

1. Instalar [VS Code](https://code.visualstudio.com/)
2. Instalar extensión **Monkey C** (Garmin)
3. Descargar [Connect IQ SDK](https://developer.garmin.com/connect-iq/sdk/)
4. Abrir este proyecto en VS Code
5. Compilar: `Ctrl+Shift+P` → "Monkey C: Build"
6. Probar en simulador o dispositivo real

### 3. Compilar para la Store en local

Requisitos:

1. Tener instalado el Connect IQ SDK
2. Tener `garminApp/developer_key`
3. Tener Pillow instalado solo si necesitas regenerar iconos:

```bash
python3 -m pip install pillow
```

Comando:

```bash
./garminApp/build_store_iq.sh
```

Por defecto reutiliza los iconos ya generados.

Si quieres regenerarlos desde el icono Android:

```bash
./garminApp/build_store_iq.sh --refresh-icons
```

Si tu SDK no está en la ruta por defecto, indica la ruta manualmente:

```bash
CONNECTIQ_SDK_ROOT="$HOME/Library/Application Support/Garmin/ConnectIQ/Sdks/connectiq-sdk-mac-9.1.0-2026-03-09-6a872a80b" \
./garminApp/build_store_iq.sh
```

Si tu `developer_key` está en otra ruta:

```bash
DEVELOPER_KEY_PATH="$HOME/path/to/developer_key" ./garminApp/build_store_iq.sh
```

Salida generada:

```bash
garminApp/bin/BiciRadar.iq
```

### 4. Subir a Connect IQ Store

1. Compilar en modo release
2. Subir el archivo `.iq` generado a [Garmin Developer Dashboard](https://developer.garmin.com/connect-iq/overview/)
3. Añadir descripción, screenshots y categoría
4. Esperar aprobación (~1-2 días)

## App ID (para pairing)

```
9b02e1cf-d60a-42d8-ad70-8c8ef1c4bdfa
```

Este ID debe coincidir con el configurado en la app Android (WearApp).
