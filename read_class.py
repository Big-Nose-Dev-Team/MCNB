import re, os, subprocess

files = [
    r'C:\Users\jasie\Desktop\clam\Java\Repositories\MCNB\team\lodestar\lodestone\systems\particle\data\color\ColorParticleData.class',
]

for path in files:
    if os.path.exists(path):
        print(f"File: {path}")
        # Use javap to print details
        subprocess.run(['javap', '-p', path], shell=True)
