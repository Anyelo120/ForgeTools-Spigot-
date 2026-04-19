# ForgeTools — Guía de Uso de Herramientas

> Cada herramienta usa **Forge Energy (⚡)** que se regenera sola con el tiempo.
> Puedes cargarla más rápido sosteniendo un ítem de carga en la **mano izquierda** y haciendo **clic derecho**.
>
> | Ítem de Carga     | Energía |
> |-------------------|---------|
> | Polvo de Piedra Brillante | +15 |
> | Lingote de Hierro | +20 |
> | Varita de Blaze   | +30 |
> | Lingote de Oro    | +35 |
> | Diamante          | +50 |
> | Lingote de Netherita | +75 |
>
> **Cambiar de modo:** Agáchate (Sneak) + Clic Derecho con la herramienta en mano.
> Cada herramienta tiene un número limitado de usos. Al agotarse, **se rompe** y desaparece.

---

## Categoría 1 — Limpieza de Terreno

---

### 🔨 Chunk Hammer
**ID:** `chunk_hammer` · **Base:** Pico de Diamante

Herramienta de demolición en línea para limpiar columnas o capas de bloques del mismo tipo.

**Modo 0 — Line Break (por defecto)**
1. Mira hacia el bloque que quieres romper.
2. Haz **clic derecho**.
3. Rompe hasta **3 bloques consecutivos** del mismo tipo en la dirección en la que miras.

> Solo rompe bloques del mismo tipo que el primero. Útil para desmantelar paredes, columnas o rocas sin afectar el entorno.

**Modo 1 — Flat Clean 3×3**
1. Mira hacia el suelo.
2. Haz **clic derecho**.
3. Elimina el bloque **más alto** (capa surface) en una cuadrícula de 3×3 bajo tu posición.

> Ideal para emparejar la capa superior de un área antes de construir.

**Configurables:** `radius`, `allowed_blocks` (lista de materiales permitidos)
**Restricciones:** Respeta zonas protegidas · Máx 25 bloques/tick

---

### 🌀 Debris Vacuum
**ID:** `debris_vacuum` · **Base:** Azada de Hierro

Succiona bloques sueltos y escombros directamente a tu inventario.

**Uso:**
1. Apunta con la vista hacia una zona con escombros (grava, arena, hojas, andamios, enredaderas).
2. Haz **clic derecho**.
3. Los bloques en línea recta hasta **6 bloques** se recogen automáticamente.

> Si un bloque está protegido, muestra partículas de humo pero no lo recoge.
> El drop va directo al inventario. Si está lleno, cae al suelo.

**Configurables:** `max_distance`, `debris_types`

---

### ⛏️ Terrain Eraser
**ID:** `terrain_eraser` · **Base:** Pala de Netherita

Borra la capa superficial del terreno en un radio cuadrado.

**Uso:**
1. Párate en el área que quieres limpiar.
2. Haz **clic derecho**.
3. Elimina el bloque **más alto** de cada columna en un radio de **4 bloques** alrededor tuyo.

> Solo afecta bloques configurados como "superficie": tierra, hierba, arena, grava, podzol, arcilla, etc.
> Perfecto para limpiar terreno antes de comenzar una construcción.

**Configurables:** `radius`, `filter_blocks`

---

## Categoría 2 — Construcción Asistida

---

### 🌉 Bridge Forge
**ID:** `bridge_forge` · **Base:** Hacha Dorada

Coloca bloques automáticamente bajo tus pies mientras caminas en el aire.

**Modo 0 — Bridge (por defecto)**
1. Equipa la herramienta en mano principal.
2. Coloca el bloque que quieres usar en el **off-hand** o en los primeros slots del hotbar.
3. **Agáchate (Sneak)** y comienza a caminar hacia el borde.
4. Los bloques se colocan automáticamente bajo tus pies.

> Máximo **12 bloques** por activación. Haz **clic derecho** para reiniciar el contador.

**Modo 1 — Rail Mode**
Igual que Bridge, pero también coloca **vallas de madera** a los costados como barandillas.

> Consumes bloques del off-hand primero, luego del hotbar. Necesitas tener suficientes bloques.

**Configurables:** `max_length`

---

### 🪜 Scaffold Launcher
**ID:** `scaffold_launcher` · **Base:** Bambú

Lanza andamios (scaffolding) en línea recta para acceder a alturas o atravesar espacios.

**Modo 0 — Vertical (por defecto)**
1. Para el uso: mira hacia arriba.
2. Haz **clic derecho**.
3. Se colocan hasta **8 andamios** hacia arriba desde tu posición.

**Modo 1 — Horizontal**
1. Mira en la dirección que quieres extender.
2. Haz **clic derecho**.
3. Se colocan hasta **8 andamios** en horizontal en esa dirección.

> Los andamios se **eliminan automáticamente** después de **30 segundos** (600 ticks).
> Necesitas tener **Scaffolding** en tu inventario para usarlos.

**Configurables:** `max_distance`, `auto_remove_ticks`

---

### 💜 Fill Pulse
**ID:** `fill_pulse` · **Base:** Fragmento de Amatista

Rellena todos los bloques de aire en una esfera alrededor de un punto.

**Uso:**
1. Sostén el bloque con el que quieres rellenar en la **mano izquierda (off-hand)**.
2. Haz **clic derecho** sobre un bloque del mundo.
3. Se rellenan todos los espacios de aire en una **esfera de radio 2** centrada en ese bloque.

> Consume bloques del off-hand. Si se te acaban, el relleno se detiene.
> Útil para cerrar huecos, rellenar cavidades o crear formas redondas rápidas.

**Configurables:** `radius`

---

### 🧱 Wall Raiser
**ID:** `wall_raiser` · **Base:** Ladrillo

Extrude paredes verticales de una sola pulsación.

**Modo 0 — Recto (por defecto)**
1. Equipa el bloque de construcción en el **hotbar**.
2. Haz **clic derecho** sobre una cara de bloque.
3. Se levanta una pared de **5 bloques de alto y 5 de ancho** paralela a la cara clickeada.

**Modo 1 — Curva 90°**
1. Igual que el modo recto, pero la pared se extiende **perpendicular** a la cara clickeada (hacia afuera).

> Consumes bloques del hotbar. Respeta zonas protegidas.

**Configurables:** `height`, `width`

---

## Categoría 3 — Herramientas Únicas

---

### 🔵 Echo Picker
**ID:** `echo_picker` · **Base:** Pico de Diamante

Rompe grupos de bloques conectados del mismo tipo mediante flood-fill.

**Modo 0 — Normal (memoria)**
1. Rompe cualquier bloque con la herramienta equipada.
2. El tipo de bloque queda **memorizado**.

**Modo 1 — Echo**
1. Con el tipo memorizado, rompe un bloque de ese tipo.
2. La herramienta busca todos los bloques **conectados del mismo tipo** en radio 3 y los rompe también.

> Máximo **25 bloques** por uso (flood-fill BFS). Solo conectados: no salta bloques aislados.
> Útil para limpiar venas de mineral, remover árboles o demoler paredes homogéneas.

**Configurables:** `flood_radius`, `max_echo_blocks`

---

### ⚓ Gravity Anchor
**ID:** `gravity_anchor` · **Base:** Yunque

Hace que todos los bloques flotantes en un radio caigan al suelo como entidades físicas.

**Uso:**
1. Apunta hacia una zona con bloques flotantes (bloques sin soporte abajo).
2. Haz **clic derecho**.
3. Todos los bloques flotantes en radio **4** se convierten en **FallingBlock** y caen.

> Los bloques no dañan entidades al caer. Al aterrizar, se transforman en el bloque correspondiente o drops.
> No afecta bedrock, obsidiana ni bloques de comandos.
> Ideal para limpiar islas flotantes accidentales o derrumbar estructuras.

**Configurables:** `radius`

---

### 💡 Light Weaver
**ID:** `light_weaver` · **Base:** Polvo de Piedra Brillante

Coloca o elimina fuentes de luz en patrón de cruz.

**Modo 0 — Place Lights (por defecto)**
1. Equipa linternas de mar (sea lanterns) u otro bloque de luz configurado en el **hotbar**.
2. Apunta hacia donde quieres iluminar.
3. Haz **clic derecho**.
4. Se colocan fuentes de luz en una **cruz de radio 3** centrada en el objetivo.

**Modo 1 — Dark Mode**
1. Haz **clic derecho** en una zona iluminada.
2. Elimina todas las fuentes de luz (antorchas, linternas, glowstone, etc.) en la misma cruz.

> Consumes bloques del hotbar en modo iluminación.

**Configurables:** `radius`, `light_block`

---

### ⚗️ Resource Recycler
**ID:** `resource_recycler` · **Base:** Minecart con Horno

Funde automáticamente los drops al romper bloques.

**Uso:**
1. Equipa la herramienta.
2. Rompe bloques normalmente.
3. El **50%** de los drops se funden automáticamente antes de ir al inventario.

> Ejemplo: Mina piedra → obtienes Smooth Stone. Mina Iron Ore → obtienes Iron Ingot.
> Los ítems fundidos van directo al inventario. El otro 50% cae sin fundir.
> Recetas soportadas: minerales, piedra, arena, arcilla, netherrack, y más (22 recetas).

**Configurables:** `smelt_chance` (0.0 - 1.0)

---

### ⚖️ Auto Level Shovel
**ID:** `auto_level_shovel` · **Base:** Pala de Netherita

Nivela el terreno a la altura de tus ojos en un área 5×5.

**Uso:**
1. Párate en el nivel al que quieres igualar el terreno.
2. Equipa el bloque de relleno en el **hotbar** (opcional, para rellenar huecos).
3. Haz **clic derecho**.
4. Los bloques que sobresalen (±1 bloque) se rompen. Los huecos se rellenan con el material del hotbar.

> Solo nivela diferencias de **±1 bloque**. No aplana diferencias grandes de un golpe.
> Útil para preparar cimientos planos o emparejar caminos.

**Configurables:** `radius`, `max_delta`

---

### 🧹 Claim Polisher
**ID:** `claim_polisher` · **Base:** Azada de Netherita

Elimina todos los bloques de un tipo específico en el chunk actual.

**Configuración previa (obligatoria):**
```
/forgetools setfilter <MATERIAL>
```
Ejemplo: `/forgetools setfilter STONE`

**Uso:**
1. Configura el filtro con el comando anterior.
2. Párate en el chunk que quieres limpiar.
3. Haz **clic derecho**.
4. Todos los bloques de ese tipo en el chunk (de Y mínimo a Y máximo) se eliminan.

> Procesamiento en lotes de 25 bloques/tick para evitar lag.
> **Solo funciona dentro de tu claim** (si GriefPrevention o Towny está activo).
> Solo elimina el tipo exacto de bloque configurado, sin afectar el entorno.

**Configurables:** (configurado por comando `/forgetools setfilter`)

---

### 👻 Phantom Placer
**ID:** `phantom_placer` · **Base:** Perla de Ender

Coloca bloques a distancia en línea recta hacia un objetivo lejano.

**Uso:**
1. Equipa el bloque que quieres colocar en el **hotbar**.
2. Apunta hacia un bloque a lo lejos (máximo **15 bloques**).
3. Haz **clic derecho**.
4. Se colocan bloques en los espacios de aire con soporte sólido abajo, entre tú y el objetivo.

> Consumes bloques del hotbar por cada uno colocado.
> Solo coloca bloques donde hay un bloque sólido debajo (no bloques flotantes).
> Útil para tender puentes a distancia o rellenar huecos difíciles de alcanzar.

**Configurables:** `max_distance`

---

### 🌿 Biome Stitcher
**ID:** `biome_stitcher` · **Base:** Bloque de Musgo
**Permiso requerido:** `forgetools.use.biome_stitcher` (solo OPs por defecto)

Cambia el bioma de una sección 16×16×16 alrededor tuyo.

**Seleccionar bioma:**
Agáchate (Sneak) + Clic Derecho para ciclar entre biomas disponibles:
`Llanuras → Bosque → Desierto → Taiga → Pantano → Jungla → Sabana → Llanuras Nevadas → Campos de Champiñones → Grove de Cerezos`

**Uso:**
1. Elige el bioma con Sneak + Clic Derecho hasta el que quieras.
2. Párate en la zona a modificar.
3. Haz **clic derecho**.
4. El bioma cambia en una sección de **16×16×16** centrada en ti.
5. El chunk se refresca automáticamente para ver el cambio visual.

> Afecta vegetación, colores del agua/cielo y spawns de mob (al reiniciar el chunk).
> El cambio es permanente hasta que uses la herramienta nuevamente.

**Configurables:** `section_size`, `mode_names` (lista de biomas)

---

## Resumen Rápido

| Herramienta | Acción | Coste ⚡ | Usos |
|---|---|---|---|
| Chunk Hammer | Clic derecho en bloque | 8 | 500 |
| Debris Vacuum | Clic derecho hacia escombros | 5 | 800 |
| Terrain Eraser | Clic derecho en suelo | 12 | 300 |
| Bridge Forge | Sneak + Caminar | 3/bloque | 1000 |
| Scaffold Launcher | Clic derecho | 6 | 400 |
| Fill Pulse | Clic derecho en bloque | 25 | 200 |
| Wall Raiser | Clic derecho en cara | 4/bloque | 600 |
| Echo Picker | Romper bloque | 10 | 400 |
| Gravity Anchor | Clic derecho | 15 | 250 |
| Light Weaver | Clic derecho | 12 | 400 |
| Resource Recycler | Romper bloque | 7 | 600 |
| Auto Level Shovel | Clic derecho | 18 | 250 |
| Claim Polisher | Clic derecho (requiere `/ft setfilter`) | 30 | 150 |
| Phantom Placer | Clic derecho hacia objetivo | 9 | 500 |
| Biome Stitcher | Clic derecho (solo OPs) | 40 | 100 |
