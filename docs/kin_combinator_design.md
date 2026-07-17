# Kin Combinator — feature design & decisions

New destination under **More**, titled **"Kin Combinator"**. Lets the user build a
set of kins and see their combined kin. Uses the
[mayan_magic_wisdom_palette](mayan_magic_wisdom_palette.md); seal/tone art is the
real `glyphN.png` / `toneN.png`, never recolored (chrome only).

Mockups: `scratchpad/combine_kins_concepts.html` (A/B/C),
`scratchpad/combine_kins_A_detail.html` (From-Tzolkin picker + scroll behavior),
`scratchpad/kin_combinator_datepicker.html` (By-date flow).

## Decisions (locked)

1. **Layout — Concept A "Builder list".** A mode toggle picks the input; each
   added kin is a removable row; the combined result shows below the list.
   (Chosen over B chip-tray and C tzolkin-first.)
2. **Two input modes:**
   - **By date** — opens the existing `DatePickerDialog`; the chosen date is
     resolved via `Dreamspell.kinFor(date)` to a kin, previewed, then added on
     **Add kin**.
   - **From Tzolkin** — opens the existing 260-cell Tzolkin grid in a
     **multi-select** mode (tap cells to toggle, "Add (N)" confirms); adds many
     kins at once.
3. **Add as many kins as wanted**, each individually removable.
4. **Combine formula — sum of kin numbers, mod 260:**
   ```
   combinedKin = ((Σ kinNumbers − 1) mod 260) + 1
   ```
   e.g. 214 + 1 + 60 = 275 → 275 − 260 = **15**. Properties to test: single kin
   is identity; order-independent; wraps at 260 (…, 259, 260, 1, 2, …); ≥1 kin
   required (empty set shows the empty state, no result).
5. **Result placement — "no-gap", built the clean way.** The result renders
   **inline** as the last item after the list and flows with it; a **duplicate
   pinned result** at the bottom is shown *only while the inline one is scrolled
   off-screen* (toggled by a scroll listener). Short list → inline result sits
   right under the kins (no dead gap); long list → pinned result stays visible.
   No measure-and-switch hack, no transition jank.
   - Android shape: `LinearLayout` = fixed input header + `RecyclerView`
     (`height=0dp, weight=1`, kins then an inline result footer) + a pinned
     result view whose visibility follows an `OnScrollListener`.

## Reuse (no new primitives)

- `DatePickerDialog` — already in `OracleActivity.showDatePicker`.
- Tzolkin grid — already in `TzolkinFragment` (add a select mode).
- `Dreamspell.kinFor`, `tone`, `seal`, `DreamspellData.kin(...)` — existing.

## To build

- `Dreamspell.combineKins(kins: List<Int>): Int` (pure) + unit tests (identity,
  order-independence, wrap at 260, BVA around 260/261).
- New fragment + layout; new **More** row "Kin Combinator".
- Strings below wired into `values/` + all `values-*/`.

## Strings + translations

UI copy. `%d` = count. (Plural niceties deferred — a `%d kins` string is fine
for v1; can move to a `plurals` resource later if desired.)

| key | en | es | fr | nl | ru | zh-rCN | zh-rTW |
|-----|----|----|----|----|----|--------|--------|
| `kcTitle` | Kin Combinator | Combinador de Kins | Combinateur de Kins | Kin-combinator | Комбинатор кинов | Kin 组合器 | Kin 組合器 |
| `kcModeDate` | By date | Por fecha | Par date | Op datum | По дате | 按日期 | 按日期 |
| `kcModeTzolkin` | From Tzolkin | Desde el Tzolkin | Depuis le Tzolkin | Uit Tzolkin | Из Цолькина | 从卓尔金 | 從卓爾金 |
| `kcDateHint` | Pick a date to add its kin | Elige una fecha para añadir su kin | Choisissez une date pour ajouter son kin | Kies een datum om de kin toe te voegen | Выберите дату, чтобы добавить её кин | 选择日期以添加其 kin | 選擇日期以新增其 kin |
| `kcPickDate` | Pick a date | Elegir fecha | Choisir une date | Datum kiezen | Выбрать дату | 选择日期 | 選擇日期 |
| `kcResolvesTo` | Resolves to | Corresponde a | Correspond à | Komt overeen met | Соответствует | 对应 | 對應 |
| `kcAddKin` | Add kin | Añadir kin | Ajouter le kin | Kin toevoegen | Добавить кин | 添加 kin | 新增 kin |
| `kcListHeader` | Kins to combine | Kins a combinar | Kins à combiner | Te combineren kins | Кины для объединения | 要组合的 kin | 要組合的 kin |
| `kcEmpty` | No kins yet. Add some to combine them. | Aún no hay kins. Añade algunos para combinarlos. | Aucun kin pour l'instant. Ajoutez-en pour les combiner. | Nog geen kins. Voeg er enkele toe om ze te combineren. | Пока нет кинов. Добавьте несколько, чтобы объединить их. | 还没有 kin。添加一些以进行组合。 | 還沒有 kin。新增一些以進行組合。 |
| `kcResultCap` | Combined | Combinado | Combiné | Gecombineerd | Объединено | 组合结果 | 組合結果 |
| `kcResultCount` | Combined · %d kins | Combinado · %d kins | Combiné · %d kins | Gecombineerd · %d kins | Объединено · %d кинов | 已组合 · %d 个 kin | 已組合 · %d 個 kin |
| `kcPickerTitle` | Pick from Tzolkin | Elegir del Tzolkin | Choisir dans le Tzolkin | Kies uit Tzolkin | Выбрать из Цолькина | 从卓尔金选择 | 從卓爾金選擇 |
| `kcPickerHint` | Tap kins to select | Toca los kins para seleccionar | Touchez les kins pour les sélectionner | Tik op kins om te selecteren | Коснитесь кинов, чтобы выбрать | 点按 kin 进行选择 | 點按 kin 進行選擇 |
| `kcPickerSelected` | %d selected | %d seleccionados | %d sélectionnés | %d geselecteerd | Выбрано: %d | 已选择 %d 个 | 已選擇 %d 個 |
| `kcPickerAdd` | Add (%d) | Añadir (%d) | Ajouter (%d) | Toevoegen (%d) | Добавить (%d) | 添加 (%d) | 新增 (%d) |
| `kcClear` | Clear all | Borrar todo | Tout effacer | Alles wissen | Очистить всё | 全部清除 | 全部清除 |
| `kcRemove` | Remove | Quitar | Retirer | Verwijderen | Удалить | 移除 | 移除 |

"Kin" is kept untranslated everywhere (matches the rest of the app, which shows
"Kin 214" in every language).
