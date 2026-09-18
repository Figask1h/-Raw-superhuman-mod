# Raw superhuman mod

[![NeoForge 1.21.1](https://img.shields.io/badge/NeoForge-1.21.1-orange)](https://neoforged.net)
[![License: MIT](https://img.shields.io/badge/license-MIT-green)](LICENSE)

A mod that adds one unbalanced ring.

Put the **DNA Alteration Ring** into a Curios ring slot and you are superhuman: flight that accelerates to
150 blocks per second, cruise control, a dash, near-total damage immunity and two attacks that delete anything.
With Ad Astra installed, fly above the atmosphere and the planet selection screen opens — space without a rocket.

> [Русская версия ниже](#русский)

## Abilities

### Flight
Creative-style flight (double-jump to toggle), with a **Boost** on top: hold sprint + forward and you accelerate
along your camera — up to **150 blocks/s** in 7 seconds. In boost you are steered by the camera only: WASD, jump and
sneak are ignored, the body goes into the elytra pose and the field of view grows with speed. Gentle steering is free;
turns sharper than 70° bleed some speed. Release forward and you slow back to hover in half a second.

### Cruise
Sneak while boosting and your current speed is locked. Let go of every key — you keep flying, steering with the mouse,
and any speed lost in a sharp turn is regained automatically. Holding sprint + forward in cruise pushes the locked
speed higher; sneak again to brake and straighten up.

### Dash
A burst of movement in the direction you are moving (WASD relative to the camera; up/down with jump/sneak while
hovering; standing still dashes forward). About 8 blocks on foot or in hover; in fast flight it is an instant
+40 blocks/s along your current course. 1.5 s cooldown.

### Rebound
Hit a wall at 50 blocks/s or more and you bounce back about 4 blocks and drop to hover. Slower than that you just stop.

### Defense
- **Physical** damage — melee, arrows, tridents, projectiles, explosions, thorns, stings, sonic boom, falling blocks, cactus — is absorbed by **93 %** (after armor).
- **Everything else does nothing**: fire, lava, fall damage, drowning, suffocation, freezing, poison, wither, magic and potions, starvation, cramming, lightning, dragon breath, Ad Astra oxygen and temperature. Harmful effects still apply, they just cannot hurt you.
- `/kill` and the void always work, so you can never get stuck.

### Slash
175 damage to every living thing in a 120° arc in front of you at sword reach (3 blocks). 0.5 s cooldown. Mouse 4 by default.

### Strike
500 damage to the single target under your crosshair within 4 blocks, with a heavy knockback. 5 s cooldown, spent even on a miss. Mouse 5 by default.

### Space (Ad Astra)
With **Ad Astra** installed, flying above the atmosphere-leave height (600 by default) opens the planet selection
screen exactly like a rocket would — every planet available, no rocket, no fuel. You arrive at the top of the target
planet's sky and fly down.

### Haste & Night Vision
Permanent Haste III and Night Vision while the ring is worn.

### HUD
Speedometer left of the hotbar with a CRUISE marker; Slash, Strike and Dash icons right of the hotbar, shaded while on cooldown.

## Controls

| Action | Default |
| --- | --- |
| Toggle flight | double-jump |
| Boost | sprint + forward (your own key bindings) |
| Cruise | sneak while boosting; sneak again to brake |
| Slash | mouse button 4 |
| Strike | mouse button 5 |
| Dash | Caps Lock |

Slash, Strike and Dash are rebindable under *Controls → Raw superhuman mod*.

## Getting the ring

Crafting:

```
N N N
B D B     N = netherite block, B = beacon, D = diamond block
N N N
```

The ring also has its own creative tab, or `/give @s rsm:ring`.

## Dependencies

| Mod | Status |
| --- | --- |
| [NeoForge](https://neoforged.net) 21.1+ | required |
| [Curios API](https://modrinth.com/mod/curios) 9.x | required |
| [Ad Astra](https://modrinth.com/mod/ad-astra) 1.16+ | optional — space travel |

## Configuration

- `<world>/serverconfig/rsm-server.toml` — speeds, thresholds, damage, cooldowns, dash. Synced to every client; defaults can go into `defaultconfigs/rsm-server.toml`.
- `config/rsm-client.toml` — FOV boost and HUD.
- What counts as physical damage — the `rsm:physical` damage type tag (`data/rsm/tags/damage_type/physical.json`), editable with a datapack.

## Building

```bash
./gradlew build
```

The jar lands in `build/libs/`. Dev client: `./gradlew runClient` (set `adastra_in_dev=false` in `gradle.properties` to run without Ad Astra).

Project vocabulary lives in [CONTEXT.md](CONTEXT.md). Licensed under MIT.

Heavily inspired by [Viltrumite](https://www.curseforge.com/minecraft/mc-mods/viltrumite). THX baranhan123.

---

## Русский

Мод добавляющий дизбалансное кольцо.

Надел **Кольцо изменения ДНК** в слот кольца Curios — и ты сверхчеловек: полёт с разгоном до 150 блоков в секунду,
круиз-контроль, дэш, почти полная защита от урона и две атаки, которыми можно снести что угодно.
С установленным Ad Astra выше атмосферы открывается экран выбора планеты — в космос без ракеты.

### Способности

- **Полёт** — как в креативе (двойной прыжок), а бег + вперёд включает Ускорение по направлению взгляда: до 150 б/с за 7 секунд, поза элитры, растущий FOV. В ускорении рулишь только камерой; плавные повороты бесплатны, резче 70° — теряешь часть скорости.
- **Круиз** — присел во время разгона: скорость зафиксирована, отпускай все клавиши и рули мышью; потерянная на повороте скорость добирается сама. Присел ещё раз — тормозишь.
- **Дэш** — рывок по направлению движения (WASD, в зависании ещё вверх/вниз, стоя — вперёд): ~8 блоков на земле, в быстром полёте — +40 б/с по курсу. Перезарядка 1,5 с.
- **Отскок** — влетел в стену на 50+ б/с: отбрасывает на 4 блока назад.
- **Защита** — физический урон поглощается на 93 %, всё остальное (огонь, лава, падение, удушение, холод, яд, магия, голод, кислород Ad Astra) не проходит вообще. `/kill` и бездна работают.
- **Слэш** — 175 урона всем в дуге 120° на дистанции меча, перезарядка 0,5 с.
- **Прямой** — 500 урона цели под прицелом до 4 блоков с сильным отбросом, перезарядка 5 с (уходит и при промахе).
- **Космос** — с Ad Astra выше 600 открывается экран планет, все планеты доступны.
- **Спешка III и Ночное зрение** — постоянно, пока кольцо надето.

### Управление

| Действие | По умолчанию |
| --- | --- |
| Полёт вкл/выкл | двойной прыжок |
| Ускорение | бег + вперёд (твои клавиши из настроек) |
| Круиз | приседание во время ускорения; ещё раз — снять |
| Слэш | кнопка мыши 4 |
| Прямой | кнопка мыши 5 |
| Дэш | Caps Lock |

Слэш, Прямой и Дэш перебиндиваются в «Управление → Raw superhuman mod».

### Крафт и зависимости

Кольцо: 6 незеритовых блоков + 2 маяка (по бокам) + алмазный блок (в центре). Есть своя креатив-вкладка и `/give @s rsm:ring`.

Нужны NeoForge 21.1+ и Curios 9.x; Ad Astra 1.16+ — по желанию.

### Настройка

- `<мир>/serverconfig/rsm-server.toml` — скорости, пороги, урон, перезарядки, дэш (синхронизируется клиентам).
- `config/rsm-client.toml` — FOV и HUD.
- Тег `rsm:physical` — что считать физическим уроном, правится датапаком.

Сборка: `./gradlew build`. Лицензия MIT.

Heavily inspired by [Viltrumite](https://www.curseforge.com/minecraft/mc-mods/viltrumite). THX baranhan123.
