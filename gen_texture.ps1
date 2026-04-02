Add-Type -AssemblyName System.Drawing
$size = 64
$bmp = New-Object System.Drawing.Bitmap($size, $size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$cx = $size / 2.0
$cy = $size / 2.0
for ($y = 0; $y -lt $size; $y++) {
    for ($x = 0; $x -lt $size; $x++) {
        $dx = $x - $cx
        $dy = $y - $cy
        $dist = [Math]::Sqrt($dx*$dx + $dy*$dy) / ($size / 2.0)
        if ($dist -gt 1.0) {
            $bmp.SetPixel($x, $y, [System.Drawing.Color]::Transparent)
            continue
        }
        $t  = 1.0 - $dist
        $ts = $t * $t * (3.0 - 2.0 * $t)
        $r  = [int](100 * $ts + 20  * (1 - $ts))
        $g  = [int](0)
        $b  = [int](220 * $ts + 40  * (1 - $ts))
        $a  = [int](255 * $ts)
        $bmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($a, $r, $g, $b))
    }
}
$dir = 'C:\Users\jasie\Desktop\clam\Java\Repositories\MCNB\src\main\resources\assets\mcnb\textures\particle'
New-Item -ItemType Directory -Force -Path $dir | Out-Null
$bmp.Save([System.IO.Path]::Combine($dir, 'end_slash.png'), [System.Drawing.Imaging.ImageFormat]::Png)
$bmp.Dispose()
Write-Host 'Texture generated successfully'

