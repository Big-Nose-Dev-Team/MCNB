from PIL import Image, ImageDraw, ImageFilter

def create_glow_crescent():
    size = 256
    # Create transparent image
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))

    # We will draw a white crescent.
    # Approach: Draw a filled white circle, subtract a smaller offset circle.

    # Canvas for the shape
    shape_layer = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(shape_layer)

    center_x, center_y = size / 2, size / 2
    radius_outer = size * 0.4

    # Draw outer circle
    draw.ellipse((center_x - radius_outer, center_y - radius_outer,
                  center_x + radius_outer, center_y + radius_outer),
                 fill=(255, 255, 255, 255))

    # Create the "cutter" layer
    cutter_layer = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw_cutter = ImageDraw.Draw(cutter_layer)

    # Offset the cutter to the left to leave a crescent on the right
    # To make a vertical crescent ")", we cut from the left.
    radius_inner = radius_outer * 0.85
    offset = radius_outer * 0.25

    draw_cutter.ellipse((center_x - radius_inner - offset, center_y - radius_inner,
                         center_x + radius_inner - offset, center_y + radius_inner),
                        fill=(255, 255, 255, 255))

    # Subtract cutter from shape
    # We do this by iterating pixels or using composite.
    # Since we want soft edges later, let's just do pixel subtraction of alpha.

    shape_data = shape_layer.load()
    cutter_data = cutter_layer.load()

    crescent_layer = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    crescent_data = crescent_layer.load()

    for y in range(size):
        for x in range(size):
            r, g, b, a_shape = shape_data[x, y]
            _, _, _, a_cutter = cutter_data[x, y]

            if a_shape > 0:
                # If we are inside the shape
                if a_cutter > 0:
                    # And inside the cutter -> subtract
                    # Simple boolean for now since we drew hard shapes
                    pass
                else:
                    # Inside shape, outside cutter -> Keep
                    crescent_data[x, y] = (255, 255, 255, 255)

    # Now we have a hard aliased crescent.
    # Let's apply a glow.

    # 1. Base blur (Softness)
    base = crescent_layer.filter(ImageFilter.GaussianBlur(2))

    # 2. Outer Glow (Wide)
    glow = base.filter(ImageFilter.GaussianBlur(8))

    # 3. Core (Sharp)
    core = crescent_layer.filter(ImageFilter.GaussianBlur(1))

    # Composite:
    # Start with empty
    final = Image.new("RGBA", (size, size), (0, 0, 0, 0))

    # Add glow (faint) - adjust alpha
    # We can iterate to reduce alpha of glow if needed, but let's just paste.
    # To make it "White", we keep it white.

    # Paste glow
    final.paste(glow, (0, 0), glow)

    # Paste base
    final.paste(base, (0, 0), base)

    # Paste core for intensity
    final.paste(core, (0, 0), core)

    return final

if __name__ == "__main__":
    img = create_glow_crescent()
    # Save to the specific path
    path = r"C:\Users\jasie\Desktop\clam\Java\Repositories\MCNB\src\main\resources\assets\mcnb\textures\particle\end_slash.png"
    img.save(path, "PNG")
    print(f"Texture generated at {path}")
