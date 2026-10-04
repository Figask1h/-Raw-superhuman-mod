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
- **Breakthrough**: hit blocks at 60+ b/s and you tunnel straight through them. Obsidian-grade blocks, bedrock and chests stop you; claims are respected.
- **Barrier**: reaching 60 b/s goes off like a breeze's wind charge, and your view jumps wider.
- **Incandescence**: from 100 b/s your head, chest and arms glow like heated metal — dark red, orange, white-hot at top speed — over armor, for everyone to see.
- Digging is as fast in the air and under water as on solid ground.

### Defense
- Physical damage — melee, arrows, explosions, falling anvils — is absorbed by **99 %**.
- Everything else does **nothing**: fire, lava, fall damage, drowning, suffocation, freezing, poison, wither, magic, starvation, lightning, dragon breath. Harmful potions still apply their effects, they just cannot hurt you.
- `/kill` and the void still work, so you cannot get stuck.

### Attacks
- **Haste III**, **Night Vision**, **Regeneration I** and **Strength II** while the ring is on, each switchable with its own key.
- **Slash** (mouse 4): 175 damage to everything in a sword-reach arc in front of you (walls stop it), 0.5 s cooldown.
- **Strike** (mouse 5): 500 damage to the target under your crosshair with a brutal knockback, 5 s cooldown. A warden-style sonic trail, a flash, a shaking camera.
- **Clap** (G): a directed blast that tears a ragged crater through soft ground and a few layers of stone, 50 damage and a big shove to everything in front, 3 s cooldown. Obsidian-grade blocks shield what is behind them. You literally clap your hands.
- **Grab** (R): pick up anything living up to 500 health by the neck, carry it as a battering ram through mountains, hold R to **throw** it at 60 b/s into the nearest wall. Grabbed players (if the server allows it) break free by mashing jump; a grabbed bearer can throw you off with an attack.

### Space (Ad Astra)
With **Ad Astra** installed, fly above the atmosphere and the planet selection screen opens — every planet reachable, no rocket, no fuel. You breathe without oxygen and never freeze in space — no suit needed.

### Details
- Every number — speeds, thresholds, damage, cooldowns, dash, Grab — lives in `serverconfig/rsm-server.toml` and syncs to clients. FOV boost, HUD and camera shake are client options. The ring itself can be switched off in `config/rsm-common.toml`.
- What counts as "physical" damage is a datapack tag (`rsm:physical`); what can never be grabbed is another (`rsm:ungrabbable`).
- Works on dedicated servers; movement is validated the same way vanilla elytra flight is.
- Crafted from 6 netherite blocks, 2 beacons and a diamond block: an end-game price for an end-of-balance item. Also in its own creative tab.

**Requires:** NeoForge 1.21.1, Curios API. **Optional:** Ad Astra.

Heavily inspired by [Viltrumite](https://www.curseforge.com/minecraft/mc-mods/viltrumite). THX baranhan123.

---

## Русский

**Одно кольцо. Ноль баланса. 150 блоков в секунду, лава не жжёт, мобы испаряются.**

**Raw superhuman mod** добавляет один предмет — **Кольцо изменения ДНК**. Надел в слот кольца Curios — и игра перестаёт быть честной.

### Полёт
- Полёт как в креативе (двойной прыжок) плюс **Ускорение**: зажми бег + вперёд и разгоняйся по направлению взгляда до **150 блоков/с** за семь секунд. Поза элитры, свист ветра, растущий FOV.
- **Круиз**: присядь во время разгона — скорость зафиксирована, отпускай всё и рули мышью. Присел ещё раз — тормозишь.
- **Дэш** (Caps Lock): рывок на 8 блоков на земле, мгновенная прибавка скорости в воздухе.
- **Пробивание**: врезался в блоки на 60+ б/с — пробиваешь туннель насквозь. Обсидиан и всё столь же прочное, бедрок и сундуки останавливают; приваты соблюдаются.
- **Барьер**: на 60 б/с — взрыв, как у заряда ветра бриза, и обзор рывком становится шире.
- **Накал**: с 100 б/с голова, грудь и руки светятся как раскалённый металл — тёмно-красный, оранжевый, на максимуме добела — поверх брони, видно всем.
- Копаешь в воздухе и под водой так же быстро, как стоя на земле.

### Защита
- Физический урон — удары, стрелы, взрывы, наковальни — поглощается на **99 %**.
- Всё остальное **не работает**: огонь, лава, падение, утопление, удушение, холод, яд, иссушение, магия, голод, молния, дыхание дракона. Вредные зелья накладываются, но урона не наносят.
- `/kill` и бездна работают — застрять невозможно.

### Атаки
- **Спешка III**, **Ночное зрение**, **Регенерация I** и **Сила II**, пока кольцо надето; каждый выключается своей клавишей.
- **Слэш** (кнопка мыши 4): 175 урона всем в дуге перед собой на дистанции меча (сквозь стены не бьёт), перезарядка 0,5 с.
- **Прямой** (кнопка мыши 5): 500 урона цели под прицелом с жёстким отбросом, перезарядка 5 с. След как у звукового удара вардена, вспышка, тряска камеры.
- **Хлопок** (G): направленный взрыв — неровная воронка в мягком грунте и паре слоёв камня, 50 урона и сильный отброс всему впереди, перезарядка 3 с. Обсидиан и всё столь же прочное закрывает то, что за ним. Руки сходятся в настоящем хлопке.
- **Захват** (R): хватаешь за шею любое живое существо до 500 здоровья, несёшь его тараном сквозь горы, зажал R — **бросок** на 60 б/с в ближайшую стену. Схваченные игроки (если сервер разрешил) вырываются, долбя прыжок; схваченный носитель сбрасывает тебя атакой.

### Космос (Ad Astra)
С установленным **Ad Astra** поднимись выше атмосферы — откроется экран выбора планеты. Все планеты, без ракеты и топлива. Без кислорода дышишь, в космосе не замерзаешь — скафандр не нужен.

### Детали
- Все цифры — скорости, пороги, урон, перезарядки, дэш, Захват — в `serverconfig/rsm-server.toml`, синхронизируются клиентам. FOV, HUD и тряска камеры — клиентские настройки. Само кольцо можно выключить в `config/rsm-common.toml`.
- Что считать физическим уроном — тег датапака `rsm:physical`; кого нельзя схватить — тег `rsm:ungrabbable`.
- Работает на выделенных серверах.
- Крафт: 6 незеритовых блоков, 2 маяка и алмазный блок — эндгеймовая цена за конец баланса. Есть и своя креатив-вкладка.

**Требует:** NeoForge 1.21.1, Curios API. **По желанию:** Ad Astra.

Heavily inspired by [Viltrumite](https://www.curseforge.com/minecraft/mc-mods/viltrumite). THX baranhan123.
