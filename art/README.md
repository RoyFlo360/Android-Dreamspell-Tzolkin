# App icon sources

Vector masters for the launcher icon. `res/mipmap-*` PNGs are generated from these —
edit the SVGs here, never the PNGs.

| File | What it is |
|---|---|
| `app_icon.svg` | Editable master (Inkscape). All layers: starfield, glow, wheel, glyphs. |
| `ic_launcher_background.svg` | Derived: gradient plate + starfield → adaptive **background**. |
| `ic_launcher_foreground.svg` | Derived: glow + wheel + glyphs → adaptive **foreground**. |

## Layers in `app_icon.svg`

- `layer4` — 113 star circles (background)
- `layer2` — the glow: a white disc plus a blurred `<use>` copy of it (`filter5`)
- `layer1` — the wheel: tiles, tones, green centre, face
- `layer3` — glyph line detail

The two derived SVGs are the master with layers toggled and the `viewBox` reframed so
the glow disc lands at **63dp of the 108dp** adaptive canvas — inside Android's 66dp
safe circle, which keeps the starfield visible instead of letting the mask eat it.

## Regenerating the mipmaps

Render each layer at 4x and downsample (the blur scales with the `viewBox`, so this is
consistent across densities):

```bash
for L in background foreground; do
  chromium --headless --disable-gpu --hide-scrollbars \
    --default-background-color=00000000 --window-size=1728,1728 \
    --screenshot=/tmp/master_$L.png "file://$PWD/art/ic_launcher_$L.svg"
done

# mdpi:108 hdpi:162 xhdpi:216 xxhdpi:324 xxxhdpi:432
magick /tmp/master_background.png -filter Lanczos -resize 432x432 -strip \
  app/src/main/res/mipmap-xxxhdpi/ic_launcher_background.png
```

The monochrome layer (Android 13+ themed icons) is derived from the foreground —
threshold it so the white tiles knock out and the wheel reads as a medallion. A
luminance-based line-art version was tried and turned to mush at 96px:

```bash
magick /tmp/master_foreground.png -background white -flatten +repage \
  -colorspace Gray -threshold 92% -negate /tmp/mono_alpha.png
magick -size 1728x1728 xc:white /tmp/mono_alpha.png \
  -alpha off -compose CopyOpacity -composite +repage /tmp/mono.png
```

## Gotchas

- Only the **alpha** of the monochrome layer matters — the system replaces its colour.
- `<VectorDrawable>` can't render this icon: `feGaussianBlur` is unsupported and the glow
  would silently vanish. Ship rasters.
- `minSdk` is 26, so every device gets the adaptive icon — no legacy `ic_launcher.png`
  fallbacks are needed in `mipmap-*dpi/`.
