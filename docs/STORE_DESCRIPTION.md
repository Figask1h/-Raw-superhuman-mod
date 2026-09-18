# Описание для Modrinth / CurseForge

Ниже — два готовых текста: английский (основной для страницы) и русский. Краткое описание (summary) — первая строка каждого.

---

## English

**One ring. Zero balance. Fly at 150 blocks per second, shrug off lava, delete mobs.**

**Raw superhuman mod** adds a single item: the **DNA Alteration Ring**. Slip it into a Curios ring slot and the game stops being fair.

### Flight
- Creative-style flight (double-jump), plus a **Boost**: hold sprint + forward and you accelerate along your camera up to **150 blocks/s** in seven seconds. Elytra pose, wind, growing field of view — the works.
- **Cruise control**: sneak while boosting to lock your speed, then let go of everything and just steer with the mouse. Sneak again to brake.
- **Dash** (Caps Lock): an 8-block burst on foot, an instant speed kick in the air.
- Hit a wall at 50+ b/s and you bounce off it instead of splatting.

### Defense
- Physical damage — melee, arrows, explosions, falling anvils — is absorbed by **93 %**.
- Everything else does **nothing**: fire, lava, fall damage, drowning, suffocation, freezing, poison, wither, magic, starvation, lightning, dragon breath. Harmful potions still apply their effects, they just cannot hurt you.
- `/kill` and the void still work, so you cannot get stuck.

### Attacks
- **Slash** (mouse 4): 175 damage to everything in a sword-reach arc in front of you, 0.5 s cooldown.
- **Strike** (mouse 5): 500 damage to the target under your crosshair with a brutal knockback, 5 s cooldown.

### Space (Ad Astra)
With **Ad Astra** installed, fly above the atmosphere and the planet selection screen opens — every planet reachable, no rocket, no fuel. Oxygen and temperature cannot touch you either.

### Details
- Every number — speeds, thresholds, damage, cooldowns, dash — lives in `serverconfig/rsm-server.toml` and syncs to clients. FOV boost and HUD are client options.
- What counts as "physical" damage is a datapack tag (`rsm:physical`).
- Works on dedicated servers; movement is validated the same way vanilla elytra flight is.
- The ring is creative-only / `/give`. This is a sandbox toy, not progression content.

**Requires:** NeoForge 1.21.1, Curios API. **Optional:** Ad Astra.

---

## Русский

**Одно кольцо. Ноль баланса. 150 блоков в секунду, лава не жжёт, мобы испаряются.**

**Raw superhuman mod** добавляет один предмет — **Кольцо изменения ДНК**. Надел в слот кольца Curios — и игра перестаёт быть честной.

### Полёт
- Полёт как в креативе (двойной прыжок) плюс **Ускорение**: зажми бег + вперёд и разгоняйся по направлению взгляда до **150 блоков/с** за семь секунд. Поза элитры, свист ветра, растущий FOV.
- **Круиз**: присядь во время разгона — скорость зафиксирована, отпускай всё и рули мышью. Присел ещё раз — тормозишь.
- **Дэш** (Caps Lock): рывок на 8 блоков на земле, мгновенная прибавка скорости в воздухе.
- Влетел в стену на 50+ б/с — отскакиваешь, а не размазываешься.

### Защита
- Физический урон — удары, стрелы, взрывы, наковальни — поглощается на **93 %**.
- Всё остальное **не работает**: огонь, лава, падение, утопление, удушение, холод, яд, иссушение, магия, голод, молния, дыхание дракона. Вредные зелья накладываются, но урона не наносят.
- `/kill` и бездна работают — застрять невозможно.

### Атаки
- **Слэш** (кнопка мыши 4): 175 урона всем в дуге перед собой на дистанции меча, перезарядка 0,5 с.
- **Прямой** (кнопка мыши 5): 500 урона цели под прицелом с жёстким отбросом, перезарядка 5 с.

### Космос (Ad Astra)
С установленным **Ad Astra** поднимись выше атмосферы — откроется экран выбора планеты. Все планеты, без ракеты и топлива. Кислород и температура тоже не страшны.

### Детали
- Все цифры — скорости, пороги, урон, перезарядки, дэш — в `serverconfig/rsm-server.toml`, синхронизируются клиентам. FOV и HUD — клиентские настройки.
- Что считать физическим уроном — тег датапака `rsm:physical`.
- Работает на выделенных серверах.
- Кольцо только из креатива / `/give`. Это игрушка для песочницы, не прогрессия.

**Требует:** NeoForge 1.21.1, Curios API. **По желанию:** Ad Astra.
