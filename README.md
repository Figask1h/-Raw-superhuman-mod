# Raw superhuman mod

[![NeoForge 1.21.1](https://img.shields.io/badge/NeoForge-1.21.1-orange)](https://neoforged.net)
[![License: MIT](https://img.shields.io/badge/license-MIT-green)](LICENSE)

Мод добавляющий дизбалансное кольцо.

Надел **Кольцо изменения ДНК** в слот кольца Curios — и ты сверхчеловек: полёт с разгоном до 150 блоков в секунду,
круиз-контроль, дэш, почти полная защита от урона и две атаки, которыми можно снести что угодно.
С установленным Ad Astra выше атмосферы открывается экран выбора планеты — в космос без ракеты.

> [English below](#english)

## Возможности

- **Полёт** — как в креативе (двойной прыжок), но бег + вперёд включает Ускорение по направлению взгляда: линейный разгон до 150 б/с за 7 секунд, элитровая поза, растущий FOV.
- **Круиз** — присел во время разгона: скорость зафиксирована, можно отпустить все клавиши и просто рулить камерой. Присел ещё раз — тормозишь.
- **Дэш** — рывок на ~8 блоков по взгляду; в быстром полёте — мгновенная прибавка скорости.
- **Отскок** — влетел в стену на 50+ б/с: отбрасывает назад на 4 блока.
- **Защита** — физический урон (удары, снаряды, взрывы…) поглощается на 93 %, всё остальное (огонь, лава, падение, удушение, холод, яд, магия, голод, кислород Ad Astra) — не проходит вообще. `/kill` и бездна работают.
- **Слэш** — 175 урона всем в дуге перед собой на дистанции меча, перезарядка 0,5 с.
- **Прямой** — 500 урона одной цели под прицелом с сильным отбросом, перезарядка 5 с.
- **Космос** — с Ad Astra: выше высоты выхода из атмосферы (600) открывается экран планет, все планеты доступны.

Кольцо только из креатив-вкладки «Инструменты и утилиты» или `/give @s rsm:ring`.

## Управление

| Действие | По умолчанию |
| --- | --- |
| Полёт вкл/выкл | двойной прыжок |
| Ускорение | бег + вперёд (твои клавиши из настроек) |
| Круиз | приседание во время ускорения; ещё раз — снять |
| Слэш | кнопка мыши 4 |
| Прямой | кнопка мыши 5 |
| Дэш | Caps Lock |

Слэш, Прямой и Дэш перебиндиваются в «Управление → Raw superhuman mod».

## Зависимости

| Мод | Статус |
| --- | --- |
| [NeoForge](https://neoforged.net) 21.1+ | обязательно |
| [Curios API](https://modrinth.com/mod/curios) 9.x | обязательно |
| [Ad Astra](https://modrinth.com/mod/ad-astra) 1.16+ | по желанию — космос |

## Настройка

- `<мир>/serverconfig/rsm-server.toml` — скорости, пороги, урон, перезарядки, дэш. Синхронизируется всем клиентам; значения по умолчанию можно положить в `defaultconfigs/rsm-server.toml`.
- `config/rsm-client.toml` — прибавка FOV и показ HUD.
- Что считается физическим уроном — тег `data/rsm/tags/damage_type/physical.json`, правится датапаком.

## Сборка

```bash
./gradlew build
```

Jar появится в `build/libs/`. Dev-клиент: `./gradlew runClient` (Ad Astra в dev-рантайме отключается через `adastra_in_dev=false` в `gradle.properties`).

Словарь терминов проекта — в [CONTEXT.md](CONTEXT.md).

---

## English

A mod that adds one unbalanced ring.

Put the **DNA Alteration Ring** into a Curios ring slot and you are superhuman: flight that accelerates up to
150 blocks per second, cruise control, a dash, near-total damage immunity and two attacks that delete anything.
With Ad Astra installed, flying above the atmosphere opens the planet selection screen — space without a rocket.

**Features**: creative-style flight with a sprint-driven boost along the camera (150 b/s in 7 s, elytra pose, FOV grows with speed); cruise (sneak while boosting to lock the speed, sneak again to brake); dash (~8 blocks, or a speed burst in fast flight); rebound off walls at 50+ b/s; 93 % physical damage absorption and full immunity to everything else (`/kill` and the void still work); Slash (175 dmg, sword-reach arc, 0.5 s cooldown); Strike (500 dmg, single target, heavy knockback, 5 s cooldown); Ad Astra ascent above the atmosphere-leave height.

**Controls**: double-jump to fly, sprint + forward to boost, sneak to cruise, mouse 4 = Slash, mouse 5 = Strike, Caps Lock = Dash. Attack and dash keys are rebindable.

**Requires** NeoForge 21.1+ and Curios 9.x; **optionally** Ad Astra 1.16+.

**Config**: `serverconfig/rsm-server.toml` (gameplay numbers, synced to clients), `config/rsm-client.toml` (FOV, HUD), damage type tag `rsm:physical` for what counts as physical.

Build with `./gradlew build`. Licensed under MIT.
