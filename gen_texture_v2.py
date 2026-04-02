from PIL import Image, ImageDraw, ImageFilter

def create_crescent():
    size = 256
    # Transparent background
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))

    # We want a vertical crescent: )
    # Center coordinates
    cx, cy = size / 2, size / 2

    # Radii
    r_outer = size * 0.4
    r_inner = size * 0.35 # Closer to outer for thinner crescent

    # Offset inner circle to the left
    offset_x = size * 0.1

    # Create mask for outer
    outer_mask = Image.new("L", (size, size), 0)
    d_outer = ImageDraw.Draw(outer_mask)
    d_outer.ellipse((cx - r_outer, cy - r_outer, cx + r_outer, cy + r_outer), fill=255)

    # Create mask for inner
    inner_mask = Image.new("L", (size, size), 0)
    d_inner = ImageDraw.Draw(inner_mask)
    d_inner.ellipse((cx - r_inner - offset_x, cy - r_inner, cx + r_inner - offset_x, cy + r_inner), fill=255)

    # Combine masks: Result = Outer - Inner
    # BUT, we want soft edges for the "Energy" look.

    # Let's use distance fields or just blurred masks.

    # Blur the inner mask slightly (so the cut is soft)
    inner_mask_blur = inner_mask.filter(ImageFilter.GaussianBlur(5))

    # Blur the outer mask slightly (so the edge is soft)
    outer_mask_blur = outer_mask.filter(ImageFilter.GaussianBlur(5))

    # Final Alpha = Outer - Inner
    final_img = Image.new("RGBA", (size, size))
    pixels = final_img.load()

    outer_pixels = outer_mask_blur.load()
    inner_pixels = inner_mask_blur.load()

    for y in range(size):
        for x in range(size):
            o = outer_pixels[x, y]
            i = inner_pixels[x, y]

            # Simple subtraction with clamping
            val = o - i
            if val < 0: val = 0

            # Boost the core?
            # If val is high, it's white.
            # We want "White Energy".

            if val > 5:
                # Color: White
                pixels[x, y] = (255, 255, 255, val)

    # Now we have a soft crescent.
    # Let's add an extra "Glow" layer.

    glow = final_img.filter(ImageFilter.GaussianBlur(10))

    # Composite: Glow + Sharp Core
    # Make a sharper core
    core = final_img # The one we just made is already semi-soft.

    # Super sharp core
    # Re-calculate hard cut?
    # Let's just use the current one as core.

    # Create a new canvas
    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))

    # Add glow (maybe dim it a bit?)
    # Paste glow
    canvas.paste(glow, (0, 0), glow)

    # Paste core
    canvas.paste(core, (0, 0), core)

    return canvas

if __name__ == "__main__":
    img = create_crescent()
    path = r"C:\Users\jasie\Desktop\clam\Java\Repositories\MCNB\src\main\resources\assets\mcnb\textures\particle\end_slash.png"
    img.save(path, "PNG")
    print(f"Saved to {path}")

