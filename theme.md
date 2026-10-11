# buttercup.sh light theme

Extracted from [buttercup.sh](https://buttercup.sh) on 2026-10-10.

Tokens live on `:root` as `light-dark(light, dark)`. Light values below are the first argument. Forcing light mode is `data-theme="light"` on `<html>` (`color-scheme: light`). Computed styles in that mode match these hex values.

The site itself uses square corners. Linote’s composer borrows the colors, type, and button fill, and is capsule-shaped on purpose.

## Color

| Token | Light | Use |
| --- | --- | --- |
| `--ink` | `#4a3610` | Body text, button label, icon |
| `--ink-hot` | `#1c1203` | Headings, field text, emphasis |
| `--ink-dim` | `#7d6528` | Secondary text, prompt hint |
| `--ink-faint` | `#9c8449` | Meta text, markers |
| `--paper` | `#fdf3d5` | Page and field fill |
| `--paper-2` | `#f8e9c1` | Panel fill (`.console`, `.rack`) |
| `--paper-3` | `#f2ddaa` | Button fill, prompt bar |
| `--rule` | `#d3ba82` | Borders |
| `--good` | `#2f6b26` | Ready / success |
| `--warn` | `#a35a10` | Checking |
| `--bad` | `#9c2f1f` | Error, danger button |
| `--user` | `#1d5d7d` | Links, user transcript |
| `--bloom` | `#fffbea` | Page gradient highlight |
| `--edge` | `#eddfb4` | Page gradient edge |
| `--veil-line` | `rgba(0, 0, 0, .05)` | Hairline veil |
| `--veil-edge` | `rgba(0, 0, 0, .10)` | Edge veil |
| `--sheen` | `rgba(255, 236, 180, .30)` | Highlight sheen |

## Type

`--mono`: `"Berkeley Mono", "IBM Plex Mono", "SFMono-Regular", Menlo, Consolas, "DejaVu Sans Mono", monospace`

| Role | Size | Tracking | Case |
| --- | --- | --- | --- |
| Body | `14px` | normal | as written |
| Button | `0.74rem` | `0.12em` | uppercase |
| Mini button | `0.68rem` | inherited | uppercase |
| Title | `1rem` | `0.18em` | uppercase |

## Surfaces

- Page: `radial-gradient(120% 90% at 50% 0%, var(--bloom) 0%, var(--paper) 55%, var(--edge) 100%)`, text `--ink`.
- Panel: `2px solid var(--rule)`, fill `--paper-2`.
- Prompt bar: top border `2px solid var(--rule)`, fill `--paper-3`, padding `0.6rem`, gap `0.5rem`.
- Field: fill `--paper`, `1px solid var(--rule)`, text `--ink-hot`, padding `0.45rem 0.5rem`.
- Button: fill `--paper-3`, `1px solid var(--rule)`, text `--ink`, padding `0.35rem 0.7rem`. Hover: fill `--ink`, text `--paper`. Disabled: opacity `0.4`.
- Danger button: border and text `--bad`. Hover: fill `--bad`, text `--paper`.
- Focus: `2px solid var(--ink)`, offset `-1px`.
- Spacing token `--pad`: `0.75rem`.

## Linote composer

The capsule uses button fill `--paper-3`, border `--rule`, hint `--ink-dim`, pen `--ink`, on page `--paper`. Android draws the mono stack as the platform monospace face.