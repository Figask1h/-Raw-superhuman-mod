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
sneak are ignored, the body goes into the elytra pose and the field of view widens with speed — quickly at first, with an extra jump as you break the Barrier (below). Gentle steering is free;
turns sharper than 70° bleed some speed. Release forward and you slow back to hover in half a second.

### Barrier
Reaching Breakthrough speed (60 blocks/s) breaks the **Barrier**: a burst like a breeze's wind charge that everyone nearby sees and hears. It pushes nothing. You get the next one only after dropping about 10 blocks/s below it.

### Incandescence
From 100 blocks/s your head, chest and the front of your arms start to glow like heated metal — dark red at first, orange, then yellow-white at top speed — fading out towards the waist. The glow shines over armor, everyone sees it, your own arms in first person included, and it cools off over about two seconds after you slow down. Purely visual; `incandescence.minSpeed` in the server config moves the threshold.

### Cruise
Sneak while boosting and your current speed is locked. Let go of every key — you keep flying, steering with the mouse,
and any speed lost in a sharp turn is regained automatically. Holding sprint + forward in cruise pushes the locked
speed higher; sneak again to brake and straighten up.

### Dash
A burst of movement in the direction you are moving (WASD relative to the camera; up/down with jump/sneak while
hovering; standing still dashes forward). About 8 blocks on foot or in hover; in fast flight it is an instant
+40 blocks/s along your current course. 1.5 s cooldown.

### Breakthrough
Hit blocks at 60 blocks/s or more (walls, hills, the ground) and you punch a ragged tunnel about three blocks wide straight through them, without slowing down. Nothing drops. **Impervious** blocks stop you dead: obsidian and anything as blast-resistant (anvils, enchanting tables, ancient debris...), bedrock, and anything holding items (chests, furnaces, shulker boxes, modded storage). Claims and spawn protection are respected, `blocks.destroyBlocks = false` in the server config turns destruction off, and datapacks can adjust the rules with the `rsm:impervious` and `rsm:breakable` block tags. Slower than 60 b/s, any block you hit stops you and drops you to hover.

### Defense
- **Physical** damage — melee, arrows, tridents, projectiles, explosions, thorns, stings, sonic boom, falling blocks, cactus — is absorbed by **99 %** (after armor).
- **Everything else does nothing**: fire, lava, fall damage, drowning, suffocation, freezing, poison, wither, magic and potions, starvation, cramming, lightning, dragon breath, Ad Astra oxygen and temperature. Harmful effects still apply, they just cannot hurt you.
- `/kill` and the void always work, so you can never get stuck.

### Slash
175 damage to every living thing in a 120° arc in front of you at sword reach (3 blocks); walls stop it. 0.5 s cooldown. Mouse 4 by default.

### Strike
500 damage to the single target under your crosshair within 4 blocks, with a heavy knockback. A trail of rings like the warden's sonic boom runs along the blow, a flash marks the hit, and your camera — and the camera of a player you hit — shakes. 5 s cooldown, spent even on a miss. Mouse 5 by default.

### Clap
A directed blast along your look, aimed slightly down (G by default, 3 s cooldown). It tears a ragged crater up to about 8 blocks deep: soft blocks give way along the whole range, stone only 2-3 layers. Impervious blocks survive and shield everything behind them; claims are respected, and blocks drop like after an explosion. Everything living in the cone takes 50 damage and is thrown back. You clap your hands (seen in first and third person), a sonic-boom trail runs along the blast with a flash where it lands, and your camera and those of the players hit shake.

### Grab
Press **R** at a living thing under your crosshair (within 4 blocks, not through walls) and you grab it by the neck and hold it in front of you — tall ones get lifted off the ground. In fast flight you carry it ahead of you like a battering ram, and Breakthrough tunnels its way too. No cooldown.
- **Throw** — press R again and hold it for 0.4 s: the captive flies straight along your look at 60 blocks/s (plus your own flight speed) for 2 seconds, then falls. Its first hit on a block costs it 4 hearts, fall damage on top.
- **Release** — the key under Esc (`` ` ``): it just drops.
- While you hold something you cannot Clap (both hands needed); Slash and Strike still hit it, and Strike does not knock it out of your hand. Running into blocks with it costs it half a heart, at most twice a second. A captive can still attack you, but cannot move.
- It ends by itself when you take the ring off, die, leave, turn spectator, mount or sleep, or when the captive dies or one of you changes dimension (portals, Ad Astra included).
- Up to 500 max health; the ender dragon and armor stands never (entity type tag `rsm:ungrabbable`). Players only with `grab.players = true` (off by default) and never in creative. A held player breaks free with 10 jumps in 3 seconds (a bearer with 3), with a clang like an anvil; a held bearer can also use any attack to throw you off about 5 blocks — nobody gets hurt, the attack goes on cooldown. Mobs never break free.

### Space (Ad Astra)
With **Ad Astra** installed, flying above the atmosphere-leave height (600 by default) opens the planet selection
screen exactly like a rocket would — every planet available, no rocket, no fuel. You arrive at the top of the target
planet's sky and fly down. In space and on airless planets you breathe without oxygen (your air never runs out) and the cold never freezes you — no space suit needed.

### Passive Effects
Haste III, Night Vision, Regeneration I and Strength II while the ring is worn, each switchable on its own (H, N, J and Y by default). Strength boosts ordinary melee only; Slash, Strike and Clap deal fixed damage. Your choice is remembered; switching off only removes the ring's effect, never a potion or beacon.

### Digging
You dig as fast in the air (flying, jumping, falling) and under water as standing on dry ground — no Aqua Affinity needed.

### HUD
Speedometer left of the hotbar with a CRUISE marker; Slash, Strike and Dash icons right of the hotbar, shaded while on cooldown, and above them a row of smaller Passive Effect icons, faded while switched off.

## Controls

| Action | Default |
| --- | --- |
| Toggle flight | double-jump |
| Boost | sprint + forward (your own key bindings) |
| Cruise | sneak while boosting; sneak again to brake |
| Slash | mouse button 4 |
| Strike | mouse button 5 |
| Clap | G |
| Dash | Caps Lock |
| Haste on/off | H |
| Night Vision on/off | N |
| Regeneration on/off | J |
| Strength on/off | Y |
| Grab (press again and hold to throw) | R |
| Release | key under Esc (`` ` ``) |

All of these except flight, Boost and Cruise are rebindable under *Controls → Raw superhuman mod*.

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

- `<world>/serverconfig/rsm-server.toml` — speeds, thresholds, damage, cooldowns, dash, Breakthrough, Incandescence, Clap, Grab (range, max health, players, throw speed and damage, jumps to break free), block destruction. Synced to every client; defaults can go into `defaultconfigs/rsm-server.toml`.
- `config/rsm-client.toml` — FOV boost (40° by default), HUD, camera shake (`screenShake`; the vanilla Distortion Effects slider scales it too).
- `config/rsm-common.toml` — `ringEnabled`: switch the ring off entirely (no recipe, no creative tab entry, existing rings do nothing). Applies to every world of the game or server and needs a restart; on a server, the server's value wins.
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
- **Барьер** — на скорости Пробивания (60 б/с) — взрыв, как у заряда ветра бриза: видят и слышат все, никого не толкает. Следующий — только после того, как скорость упадёт примерно на 10 б/с ниже. FOV в полёте растёт быстро в начале и прибавляет рывком на Барьере.
- **Накал** — с 100 б/с голова, грудь и передняя сторона рук светятся как раскалённый металл: сначала тёмно-красным, потом оранжевым, на максимальной скорости жёлто-белым; к поясу свечение сходит на нет. Видно поверх брони и всем игрокам, свою руку от первого лица тоже; после замедления остывает примерно за 2 секунды. Только визуально; порог — `incandescence.minSpeed` в серверном конфиге.
- **Круиз** — присел во время разгона: скорость зафиксирована, отпускай все клавиши и рули мышью; потерянная на повороте скорость добирается сама. Присел ещё раз — тормозишь.
- **Дэш** — рывок по направлению движения (WASD, в зависании ещё вверх/вниз, стоя — вперёд): ~8 блоков на земле, в быстром полёте — +40 б/с по курсу. Перезарядка 1,5 с.
- **Пробивание** — врезался в блоки (стену, холм, землю) на 60+ б/с — пробиваешь неровный туннель шириной около 3 блоков, не теряя скорости; предметы не выпадают. **Непробиваемые** блоки останавливают намертво: обсидиан и всё столь же взрывостойкое (наковальни, стол зачарований, древние обломки), бедрок и всё, где лежат предметы (сундуки, печи, шалкеры). Приваты и защита спавна соблюдаются; `blocks.destroyBlocks = false` в серверном конфиге выключает разрушение; теги `rsm:impervious` и `rsm:breakable` правятся датапаком. Медленнее 60 б/с любой блок останавливает и переводит в Зависание.
- **Защита** — физический урон поглощается на 99 %, всё остальное (огонь, лава, падение, удушение, холод, яд, магия, голод, кислород Ad Astra) не проходит вообще. `/kill` и бездна работают.
- **Слэш** — 175 урона всем в дуге 120° на дистанции меча (сквозь стены не бьёт), перезарядка 0,5 с.
- **Прямой** — 500 урона цели под прицелом до 4 блоков с сильным отбросом, перезарядка 5 с (уходит и при промахе). По удару бежит след колец, как у звукового удара вардена, в точке попадания вспышка, камера трясётся у тебя и у игрока, в которого попал.
- **Хлопок** — направленный взрыв по взгляду с уклоном вниз (G, перезарядка 3 с): неровная воронка до ~8 блоков вглубь — мягкое разрушается на всю дальность, камень на 2–3 слоя. Непробиваемые блоки уцелевают и закрывают всё за собой, приваты соблюдаются, блоки выпадают как при взрыве. Всё живое в конусе получает 50 урона и отлетает. Руки сходятся в хлопке (видно от первого и третьего лица), по взрыву бежит след колец со вспышкой в точке удара, камера трясётся у тебя и у задетых игроков.
- **Захват** — R по живому существу под прицелом (до 4 блоков, не сквозь стены): держишь его за шею перед собой, высоких поднимаешь над землёй; в быстром полёте несёшь впереди как таран, Пробивание работает. Перезарядки нет. Ещё раз нажать R и подержать 0,4 с — **Бросок**: летит по прямой по взгляду на 60 б/с (плюс твоя скорость) 2 секунды, потом падает; при первом ударе о блок — 4 сердца и урон от падения. Клавиша под Esc (Ё) — **отпустить**. Пока держишь, Хлопок недоступен; Слэш и Прямой по схваченному попадают, Прямой не выбивает его из рук; врезался с ним в блок — ему полсердца, не чаще двух раз в секунду. Схваченный не двигается, но может атаковать. Отпускается сам, если снял кольцо, умер, вышел, стал наблюдателем, сел в транспорт, лёг спать, схваченный умер или кто-то сменил измерение. До 500 макс. здоровья; дракон Края и стойки для брони — никогда (тег `rsm:ungrabbable`). Игроков — только при `grab.players = true` (по умолчанию выкл) и не в творческом; схваченный игрок вырывается 10 прыжками за 3 секунды (носитель — 3), со звуком наковальни; схваченный носитель может применить любую атаку — держащего отбрасывает на ~5 блоков, урона нет, атака уходит в перезарядку. Мобы не вырываются.
- **Космос** — с Ad Astra выше 600 открывается экран планет, все планеты доступны. В космосе и на планетах без воздуха дышишь без кислорода и не замерзаешь — скафандр не нужен.
- **Спешка III, Ночное зрение, Регенерация I и Сила II** — пока кольцо надето; каждый эффект включается и выключается отдельно (H, N, J и Y), выбор запоминается. Сила усиливает только обычный удар: у Слэша, Прямого и Хлопка урон фиксированный. Иконки — мелким рядом над иконками способностей, выключенный эффект — бледный.
- **Копание** — в воздухе (в полёте, в прыжке, в падении) и под водой копаешь так же быстро, как стоя на земле; «Подводник» не нужен.

### Управление

| Действие | По умолчанию |
| --- | --- |
| Полёт вкл/выкл | двойной прыжок |
| Ускорение | бег + вперёд (твои клавиши из настроек) |
| Круиз | приседание во время ускорения; ещё раз — снять |
| Слэш | кнопка мыши 4 |
| Прямой | кнопка мыши 5 |
| Хлопок | G |
| Дэш | Caps Lock |
| Спешка вкл/выкл | H |
| Ночное зрение вкл/выкл | N |
| Регенерация вкл/выкл | J |
| Сила вкл/выкл | Y |
| Захват (ещё раз зажать — бросок) | R |
| Отпустить | клавиша под Esc (Ё) |

Всё, кроме полёта, Ускорения и Круиза, перебиндивается в «Управление → Raw superhuman mod».

### Крафт и зависимости

Кольцо: 6 незеритовых блоков + 2 маяка (по бокам) + алмазный блок (в центре). Есть своя креатив-вкладка и `/give @s rsm:ring`.

Нужны NeoForge 21.1+ и Curios 9.x; Ad Astra 1.16+ — по желанию.

### Настройка

- `<мир>/serverconfig/rsm-server.toml` — скорости, пороги, урон, перезарядки, дэш, Пробивание, Накал, Хлопок, Захват, разрушение блоков (синхронизируется клиентам).
- `config/rsm-client.toml` — FOV (по умолчанию +40°), HUD, тряска камеры (`screenShake`; ещё её ослабляет ванильный ползунок «Эффекты искажения»).
- `config/rsm-common.toml` — `ringEnabled`: полностью выключить кольцо (нет рецепта и вкладки, существующие кольца ничего не дают). Действует на все миры игры или сервера, нужен перезапуск; на сервере решает его значение.
- Тег `rsm:physical` — что считать физическим уроном, правится датапаком.

Сборка: `./gradlew build`. Лицензия MIT.

Heavily inspired by [Viltrumite](https://www.curseforge.com/minecraft/mc-mods/viltrumite). THX baranhan123.
