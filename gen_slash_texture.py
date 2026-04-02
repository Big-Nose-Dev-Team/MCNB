from PIL import Image, ImageDraw, ImageFilter
import random
import math

def create_sharp_slash():
    size = 256
    cx, cy = size / 2, size / 2

    # Create black transparent image
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # We want a vertical slightly curved line (Vertical Slash)
    # Shape: )
    # But very thin and sharp.

    # Let's draw a few concentric curves to simulate "energy"

    # Function to draw a tapered curve
    def draw_curve(draw_obj, radius, width, alpha_mult, offset_x=0):
        # We simulate a curve by drawing a large circle arc, but we only want a segment
        # Bounding box for the circle
        bb = (cx - radius + offset_x, cy - radius, cx + radius + offset_x, cy + radius)

        # Angles: -30 to +30 degrees (Vertical roughly)
        # 0 is right. -90 is up.
        # We want a vertical arc on the RIGHT side or LEFT side?
        # Let's make it on the RIGHT side `)`
        # Angles: -45 to 45

        start_angle = -40
        end_angle = 40

        draw_obj.arc(bb, start=start_angle, end=end_angle, fill=(255, 255, 255, int(255 * alpha_mult)), width=width)

    # We will manually draw pixels for better control over tapering

    slash_layer = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    pixels = slash_layer.load()

    # Arc parameters
    radius = size * 0.4
    thickness = 4.0 # Base thickness

    for y in range(size):
        for x in range(size):
            # Calculate distance from center, but we want an ARC
            # Distance from (cx - radius_offset, cy)
            # We shift the center of the defining circle to the LEFT, so the arc passes through ~center

            circle_cx = -size * 0.5 # Far left
            # We want the arc to be at x ~ size/2
            # So radius should be roughly size
            r_main = size * 1.0

            # Re-calculate circle center to ensure arc passes through (cx, cy)
            # circle_cx = cx - r_main

            dx = x - (cx - r_main)
            dy = y - cy
            dist = math.sqrt(dx*dx + dy*dy)

            # Difference from ideal radius
            diff = abs(dist - r_main)

            # Vertical fade (tapering at ends)
            # height ratio (-1 to 1)
            h_ratio = (y - cy) / (size * 0.45) # 0.45 height span

            if abs(h_ratio) > 1:
                continue # Clip top and bottom

            # Taper thickness based on height
            # 1 at center, 0 at ends
            taper = 1.0 - (abs(h_ratio) ** 2)
            if taper < 0: taper = 0

            current_thickness = thickness * taper * 4.0 # Scale up thickness

            if diff < current_thickness:
                # Core opacity
                alpha = 255

                # Edge softness
                edge_dist = diff
                softness = 0.5 * current_thickness
                if edge_dist > (current_thickness - softness):
                   alpha_ratio = (current_thickness - edge_dist) / softness
                   alpha = int(255 * alpha_ratio)

                # Add "noise" or streaks
                # break up the alpha slightly
                noise = random.uniform(0.8, 1.0)
                alpha = int(alpha * noise * taper) # also fade alpha by taper

                if alpha > 0:
                    # Color: White
                    pixels[x, y] = (255, 255, 255, alpha)

    # Add a "Glow" behind it
    glow = slash_layer.filter(ImageFilter.GaussianBlur(6))

    # Add a sharpening core
    core = slash_layer.filter(ImageFilter.GaussianBlur(1))

    # Combine
    final = Image.new("RGBA", (size, size), (0, 0, 0, 0))

    # Strong Glow
    final.paste(glow, (0, 0), glow)
    final.paste(glow, (0, 0), glow) # Double glow

    # Core
    final.paste(core, (0, 0), core)

    # Extra sharp white line in very center
    # Create simple line
    sharp_layer = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d_sharp = ImageDraw.Draw(sharp_layer)
    # Draw a simple curved line or just straight line?
    # Simple curve matching previous logic is hard.
    # Let's simple filter the core to be sharper?
    # Already done.

    return final

if __name__ == "__main__":
    img = create_sharp_slash()
    path = r"C:\Users\jasie\Desktop\clam\Java\Repositories\MCNB\src\main\resources\assets\mcnb\textures\particle\end_slash.png"
    img.save(path, "PNG")
    print(f"Saved Sharp Slash to {path}")

