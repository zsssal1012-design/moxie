from PIL import Image, ImageDraw, ImageFont

STOPS = [(0.00, (255, 214, 101)), (0.45, (255, 138, 31)), (1.00, (242, 97, 11))]

def ramp(t):
    t = min(max(t, 0.0), 1.0)
    for i in range(len(STOPS) - 1):
        a, ca = STOPS[i]; b, cb = STOPS[i + 1]
        if a <= t <= b:
            k = (t - a) / (b - a) if b > a else 0
            return tuple(int(ca[j] + (cb[j] - ca[j]) * k) for j in range(3))
    return STOPS[-1][1]

def sq_mask(size, r):
    m = Image.new('L', (size, size), 0)
    ImageDraw.Draw(m).rounded_rectangle([0, 0, size - 1, size - 1], radius=int(r), fill=255)
    return m

def make(size):
    g = Image.new('RGB', (size, size))
    px = g.load()
    for y in range(size):
        for x in range(size):
            px[x, y] = ramp((x / size + y / size) / 2 * 1.18 - 0.09)
    mask = sq_mask(size, size * 0.225)
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    img.paste(g, (0, 0), mask)

    hl = Image.new('L', (size, size), 0)
    ImageDraw.Draw(hl).ellipse([-size * .38, -size * .98, size * 1.38, size * .34], fill=255)
    hl = Image.composite(hl, Image.new('L', (size, size), 0), mask)
    white = Image.new('RGBA', (size, size), (255, 255, 255, 255))
    img = Image.composite(white, img, Image.eval(hl, lambda v: int(v * 0.42)))

    d = ImageDraw.Draw(img, 'RGBA')
    p = int(size * .055)
    d.rounded_rectangle([p, p, size - 1 - p, size - 1 - p], radius=int(size * .175),
                        outline=(255, 255, 255, 145), width=max(2, int(size * .016)))
    d.rounded_rectangle([0, 0, size - 1, size - 1], radius=int(size * .225),
                        outline=(255, 238, 210, 95), width=max(1, int(size * .007)))

    f = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf', int(size * .58))
    bb = d.textbbox((0, 0), 'C', font=f)
    w, h = bb[2] - bb[0], bb[3] - bb[1]
    x = (size - w) / 2 - bb[0]
    y = (size - h) / 2 - bb[1] - size * .015
    d.text((x + size * .014, y + size * .018), 'C', font=f, fill=(130, 55, 0, 120))
    d.text((x, y), 'C', font=f, fill=(255, 255, 255, 255))
    return img

big = make(512)
big.save('/root/ymx/icon_512.png')
for size, dpi in [(48,'mdpi'), (72,'hdpi'), (96,'xhdpi'), (144,'xxhdpi'), (192,'xxxhdpi')]:
    im = big.resize((size, size), Image.LANCZOS)
    im.save(f'/root/ymx/app/src/main/res/mipmap-{dpi}/ic_launcher.png')
    im.save(f'/root/ymx/app/src/main/res/mipmap-{dpi}/ic_launcher_round.png')
big.resize((240, 240), Image.LANCZOS).save('/root/ymx/preview_icon.png')
print('ICON_OK')