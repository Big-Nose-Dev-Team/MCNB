from PIL import Image, ImageDraw, ImageFilter

def create_simple_crescent():
    size = 128
    # 1. Background: Transparent
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # 2. Draw outer white circle
    margin = 10
    bbox = (margin, margin, size-margin, size-margin)
    draw.ellipse(bbox, fill=(255, 255, 255, 255))

    # 3. Cut inner circle (offset) to make crescent
    # We want a crescent that points forward?
    # Usually a sword slash is `)`
    # Let's cut from the left.

    cutter_offset = 20
    cutter_bbox = (margin - cutter_offset, margin, size-margin - cutter_offset, size-margin)

    # We can't easy subract in PIL Draw.
    # Use mask.

    base = Image.new("L", (size, size), 0)
    d_base = ImageDraw.Draw(base)
    d_base.ellipse(bbox, fill=255)

    cutter = Image.new("L", (size, size), 0)
    d_cutter = ImageDraw.Draw(cutter)
    d_cutter.ellipse(cutter_bbox, fill=255)

    # Result alpha = base - cutter
    # Iterate
    final = Image.new("RGBA", (size, size), (0,0,0,0))
    pixels = final.load()

    base_pixels = base.load()
    cutter_pixels = cutter.load()

    for y in range(size):
        for x in range(size):
            b = base_pixels[x, y]
            c = cutter_pixels[x, y]

            # Simple boolean subtraction
            if b > 100 and c < 100:
                # Inside base, outside cutter
                pixels[x, y] = (255, 255, 255, 255)

    # 4. Glow
    # Blur the result
    glow = final.filter(ImageFilter.GaussianBlur(3))

    # Composite
    # We want the sharp core on top of glow

    combined = Image.new("RGBA", (size, size), (0,0,0,0))
    combined.paste(glow, (0,0), glow)
    combined.paste(final, (0,0), final)

    return combined

if __name__ == "__main__":
    try:
        img = create_simple_crescent()
        path = r"C:\Users\jasie\Desktop\clam\Java\Repositories\MCNB\src\main\resources\assets\mcnb\textures\particle\end_slash.png"
        img.save(path, format="PNG")
        print(f"Texture saved to {path}")
    except Exception as e:
        print(f"Error: {e}")

