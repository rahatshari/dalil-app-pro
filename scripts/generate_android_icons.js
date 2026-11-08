import fs from 'fs';
import path from 'path';
import sharp from 'sharp';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const rootDir = path.resolve(__dirname, '..');
const srcIcon = path.join(rootDir, 'icon-512.png');
const resDir = path.join(rootDir, 'android', 'app', 'src', 'main', 'res');

const densities = [
  { folder: 'mipmap-mdpi', size: 48 },
  { folder: 'mipmap-hdpi', size: 72 },
  { folder: 'mipmap-xhdpi', size: 96 },
  { folder: 'mipmap-xxhdpi', size: 144 },
  { folder: 'mipmap-xxxhdpi', size: 192 }
];

async function generateAndroidIcons() {
  console.log('Generating Android icons from icon-512.png...');
  
  if (!fs.existsSync(srcIcon)) {
    throw new Error('Source icon not found at ' + srcIcon);
  }

  for (const { folder, size } of densities) {
    const targetDir = path.join(resDir, folder);
    if (!fs.existsSync(targetDir)) {
      fs.mkdirSync(targetDir, { recursive: true });
    }

    // Square launcher icon
    await sharp(srcIcon)
      .resize(size, size)
      .png()
      .toFile(path.join(targetDir, 'ic_launcher.png'));

    // Round launcher icon
    // Create circular mask
    const circleSvg = Buffer.from(
      `<svg width="${size}" height="${size}"><circle cx="${size/2}" cy="${size/2}" r="${size/2}" fill="#ffffff"/></svg>`
    );
    await sharp(srcIcon)
      .resize(size, size)
      .composite([{ input: circleSvg, blend: 'dest-in' }])
      .png()
      .toFile(path.join(targetDir, 'ic_launcher_round.png'));

    console.log(`Generated ${folder}/ic_launcher.png and ic_launcher_round.png (${size}x${size})`);
  }

  // Generate adaptive icon foreground (432x432 for standard 108dp on xxxhdpi)
  const drawableDir = path.join(resDir, 'drawable');
  if (!fs.existsSync(drawableDir)) {
    fs.mkdirSync(drawableDir, { recursive: true });
  }

  await sharp(srcIcon)
    .resize(432, 432, { fit: 'contain', background: { r: 0, g: 0, b: 0, alpha: 0 } })
    .png()
    .toFile(path.join(drawableDir, 'ic_launcher_foreground.png'));

  console.log('Generated drawable/ic_launcher_foreground.png');
}

generateAndroidIcons().catch(err => {
  console.error(err);
  process.exit(1);
});
