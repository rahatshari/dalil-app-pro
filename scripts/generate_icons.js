import fs from 'fs';
import path from 'path';
import sharp from 'sharp';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const rootDir = path.resolve(__dirname, '..');

// High-fidelity SVG of an authentic Black & White Legal Deed (দলিল) & Registry Ledger Book
function createBookSvg(isMaskable = false) {
  const scale = isMaskable ? 0.82 : 0.88;
  const cx = 512;
  const cy = 512;

  return `
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024" width="1024" height="1024">
  <defs>
    <!-- Monochrome Background Gradient -->
    <radialGradient id="bgGrad" cx="50%" cy="40%" r="75%">
      <stop offset="0%" stop-color="#1f2937"/>
      <stop offset="60%" stop-color="#111827"/>
      <stop offset="100%" stop-color="#030712"/>
    </radialGradient>

    <!-- Deep Leather Cover Gradients (Black & Charcoal) -->
    <linearGradient id="blackCoverGrad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#262626"/>
      <stop offset="40%" stop-color="#171717"/>
      <stop offset="100%" stop-color="#0a0a0a"/>
    </linearGradient>

    <linearGradient id="spineGrad" x1="0%" y1="0%" x2="100%" y2="0%">
      <stop offset="0%" stop-color="#0a0a0a"/>
      <stop offset="45%" stop-color="#262626"/>
      <stop offset="70%" stop-color="#404040"/>
      <stop offset="100%" stop-color="#171717"/>
    </linearGradient>

    <!-- Pure White / Parchment Page Gradients -->
    <linearGradient id="pageLeftGrad" x1="100%" y1="0%" x2="0%" y2="0%">
      <stop offset="0%" stop-color="#d1d5db"/>
      <stop offset="12%" stop-color="#f3f4f6"/>
      <stop offset="100%" stop-color="#ffffff"/>
    </linearGradient>

    <linearGradient id="pageRightGrad" x1="0%" y1="0%" x2="100%" y2="0%">
      <stop offset="0%" stop-color="#d1d5db"/>
      <stop offset="12%" stop-color="#f3f4f6"/>
      <stop offset="100%" stop-color="#ffffff"/>
    </linearGradient>

    <!-- Silver & Chrome Accent Gradients -->
    <linearGradient id="silverGrad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#ffffff"/>
      <stop offset="40%" stop-color="#e2e8f0"/>
      <stop offset="70%" stop-color="#cbd5e1"/>
      <stop offset="100%" stop-color="#94a3b8"/>
    </linearGradient>

    <!-- Deep Drop Shadow -->
    <filter id="shadowFilter" x="-20%" y="-20%" width="150%" height="150%">
      <feDropShadow dx="0" dy="24" stdDeviation="30" flood-color="#000000" flood-opacity="0.8"/>
    </filter>
    <filter id="pageShadow" x="-10%" y="-10%" width="130%" height="130%">
      <feDropShadow dx="0" dy="12" stdDeviation="16" flood-color="#000000" flood-opacity="0.45"/>
    </filter>
  </defs>

  <!-- Background Base (Sleek Dark Canvas) -->
  <rect width="1024" height="1024" rx="${isMaskable ? 0 : 220}" fill="url(#bgGrad)"/>

  <!-- Subtle Radial Spotlight -->
  <circle cx="512" cy="480" r="390" fill="#ffffff" opacity="0.04" filter="blur(60px)"/>

  <!-- Main Transform Group -->
  <g transform="translate(${cx}, ${cy}) scale(${scale}) translate(-512, -512)">

    <!-- Underneath Shadow of Open Deed Register Book -->
    <ellipse cx="512" cy="850" rx="440" ry="65" fill="#000000" opacity="0.75" filter="blur(26px)"/>

    <!-- Bottom Outer Hard Cover (Black Leather Binding) -->
    <!-- Left Cover Flap -->
    <path d="M 60,780 Q 50,780 48,740 L 52,220 Q 56,180 85,180 L 490,195 L 490,810 L 85,785 Q 65,785 60,780 Z" fill="url(#blackCoverGrad)" stroke="#404040" stroke-width="3"/>
    <!-- Right Cover Flap -->
    <path d="M 964,780 Q 974,780 976,740 L 972,220 Q 968,180 939,180 L 534,195 L 534,810 L 939,785 Q 959,785 964,780 Z" fill="url(#blackCoverGrad)" stroke="#404040" stroke-width="3"/>

    <!-- Stack of Page Edges (Thickness) -->
    <path d="M 75,765 L 490,795 L 490,808 L 70,780 Z" fill="#9ca3af"/>
    <path d="M 949,765 L 534,795 L 534,808 L 954,780 Z" fill="#9ca3af"/>
    <!-- Page Edge Texture Lines -->
    <line x1="82" y1="772" x2="485" y2="801" stroke="#4b5563" stroke-width="1.5"/>
    <line x1="86" y1="776" x2="480" y2="804" stroke="#d1d5db" stroke-width="1"/>
    <line x1="942" y1="772" x2="539" y2="801" stroke="#4b5563" stroke-width="1.5"/>
    <line x1="938" y1="776" x2="544" y2="804" stroke="#d1d5db" stroke-width="1"/>

    <!-- Open Book Center Valley Shadow -->
    <rect x="495" y="195" width="34" height="615" fill="#050505"/>

    <!-- ==================== LEFT DEED PAGE ==================== -->
    <g filter="url(#pageShadow)">
      <!-- Left Page Surface with natural arched curve -->
      <path d="M 95,200 C 230,185 380,185 496,208 L 496,785 C 380,765 230,765 95,745 Z" fill="url(#pageLeftGrad)" stroke="#1f2937" stroke-width="2.5"/>

      <!-- Inner Fine Deed Border (Left Page) -->
      <path d="M 120,225 C 240,212 370,212 475,230 L 475,760 C 370,742 240,742 120,725 Z" fill="none" stroke="#111827" stroke-width="2"/>
      <path d="M 128,233 C 245,221 365,221 467,238 L 467,752 C 365,735 245,735 128,718 Z" fill="none" stroke="#6b7280" stroke-width="1" stroke-dasharray="4,3"/>

      <!-- Corner Ornaments (Left Page) -->
      <circle cx="134" cy="240" r="4" fill="#111827"/>
      <circle cx="461" cy="245" r="4" fill="#111827"/>
      <circle cx="134" cy="711" r="4" fill="#111827"/>
      <circle cx="461" cy="745" r="4" fill="#111827"/>

      <!-- Official Deed Stamp Arch (Top Left) -->
      <path d="M 175,320 L 175,260 Q 295,232 415,260 L 415,320 Z" fill="#f8fafc" stroke="#111827" stroke-width="2.5"/>
      <path d="M 183,314 L 183,266 Q 295,240 407,266 L 407,314 Z" fill="none" stroke="#374151" stroke-width="1"/>

      <!-- Scales of Justice Emblem Inside Stamp -->
      <g transform="translate(295, 282) scale(0.68)">
        <!-- Center Beam -->
        <line x1="0" y1="-28" x2="0" y2="28" stroke="#111827" stroke-width="3"/>
        <circle cx="0" cy="-30" r="4" fill="#111827"/>
        <line x1="-12" y1="28" x2="12" y2="28" stroke="#111827" stroke-width="4"/>
        <!-- Balance Crossbar -->
        <path d="M -34,-16 Q 0,-24 34,-16" fill="none" stroke="#111827" stroke-width="3.5"/>
        <!-- Left Pan -->
        <line x1="-30" y1="-14" x2="-44" y2="8" stroke="#111827" stroke-width="1.8"/>
        <line x1="-30" y1="-14" x2="-16" y2="8" stroke="#111827" stroke-width="1.8"/>
        <path d="M -46,8 Q -30,20 -14,8 Z" fill="#111827"/>
        <!-- Right Pan -->
        <line x1="30" y1="-14" x2="16" y2="8" stroke="#111827" stroke-width="1.8"/>
        <line x1="30" y1="-14" x2="44" y2="8" stroke="#111827" stroke-width="1.8"/>
        <path d="M 14,8 Q 30,20 46,8 Z" fill="#111827"/>
      </g>

      <!-- Stamp Heading Text -->
      <text x="295" y="338" text-anchor="middle" font-family="'SolaimanLipi', 'Noto Sans Bengali', serif" font-size="14" font-weight="bold" fill="#111827" letter-spacing="1">বাংলাদেশ দলিল রেকর্ড</text>
      <line x1="205" y1="344" x2="385" y2="344" stroke="#111827" stroke-width="1"/>

      <!-- Legal Deed Text Lines on Left Page -->
      <g stroke="#1f2937" stroke-linecap="round">
        <line x1="145" y1="375" x2="450" y2="375" stroke-width="3"/>
        <line x1="145" y1="402" x2="440" y2="402" stroke-width="2.2"/>
        <line x1="145" y1="428" x2="425" y2="428" stroke-width="2.2"/>
        <line x1="145" y1="454" x2="448" y2="454" stroke-width="2.2"/>
        <line x1="145" y1="480" x2="390" y2="480" stroke-width="2.2"/>
        <line x1="145" y1="506" x2="435" y2="506" stroke-width="2.2"/>
        <line x1="145" y1="532" x2="445" y2="532" stroke-width="2.2"/>
        <line x1="145" y1="558" x2="370" y2="558" stroke-width="2.2"/>
        <line x1="145" y1="584" x2="440" y2="584" stroke-width="2.2"/>
        <line x1="145" y1="610" x2="415" y2="610" stroke-width="2.2"/>
        <line x1="145" y1="636" x2="450" y2="636" stroke-width="2.2"/>
      </g>

      <!-- Sub-Registry Circular Seal on Bottom Left -->
      <g transform="translate(240, 680)">
        <circle cx="0" cy="0" r="32" fill="none" stroke="#111827" stroke-width="2"/>
        <circle cx="0" cy="0" r="26" fill="none" stroke="#111827" stroke-width="1" stroke-dasharray="3,2"/>
        <circle cx="0" cy="0" r="8" fill="#111827"/>
        <text x="0" y="4" text-anchor="middle" font-family="'SolaimanLipi', sans-serif" font-size="7" font-weight="bold" fill="#ffffff">সিল</text>
      </g>
    </g>

    <!-- ==================== RIGHT DEED PAGE ==================== -->
    <g filter="url(#pageShadow)">
      <!-- Right Page Surface with natural arched curve -->
      <path d="M 528,208 C 644,185 794,185 929,200 L 929,745 C 794,765 644,765 528,785 Z" fill="url(#pageRightGrad)" stroke="#1f2937" stroke-width="2.5"/>

      <!-- Inner Fine Deed Border (Right Page) -->
      <path d="M 549,230 C 654,212 784,212 904,225 L 904,725 C 784,742 654,742 549,760 Z" fill="none" stroke="#111827" stroke-width="2"/>
      <path d="M 557,238 C 659,221 779,221 896,233 L 896,718 C 779,735 659,735 557,752 Z" fill="none" stroke="#6b7280" stroke-width="1" stroke-dasharray="4,3"/>

      <!-- Corner Ornaments (Right Page) -->
      <circle cx="563" cy="245" r="4" fill="#111827"/>
      <circle cx="890" cy="240" r="4" fill="#111827"/>
      <circle cx="563" cy="745" r="4" fill="#111827"/>
      <circle cx="890" cy="711" r="4" fill="#111827"/>

      <!-- Prominent Title Box 'দলিল ফিগার' on Right Page -->
      <g transform="translate(726, 280)">
        <!-- Elegant Monochrome Heading Plate -->
        <rect x="-145" y="-32" width="290" height="64" rx="10" fill="#111827" stroke="#000000" stroke-width="2"/>
        <rect x="-139" y="-26" width="278" height="52" rx="7" fill="none" stroke="#ffffff" stroke-width="1.5"/>
        
        <!-- Deed Title Typography in Pure Crisp White -->
        <text x="0" y="8" text-anchor="middle" font-family="'SolaimanLipi', 'Noto Sans Bengali', 'Segoe UI', sans-serif" font-size="31" font-weight="900" fill="#ffffff" letter-spacing="2">দলিল ফিগার</text>
      </g>

      <!-- Legal Deed Text Lines on Right Page -->
      <g stroke="#1f2937" stroke-linecap="round">
        <line x1="575" y1="365" x2="875" y2="365" stroke-width="2.8"/>
        <line x1="575" y1="392" x2="865" y2="392" stroke-width="2.2"/>
        <line x1="575" y1="418" x2="840" y2="418" stroke-width="2.2"/>
        <line x1="575" y1="444" x2="870" y2="444" stroke-width="2.2"/>
        <line x1="575" y1="470" x2="820" y2="470" stroke-width="2.2"/>
        <line x1="575" y1="496" x2="860" y2="496" stroke-width="2.2"/>
        <line x1="575" y1="522" x2="845" y2="522" stroke-width="2.2"/>
        <line x1="575" y1="548" x2="875" y2="548" stroke-width="2.2"/>
        <line x1="575" y1="574" x2="810" y2="574" stroke-width="2.2"/>
        <line x1="575" y1="600" x2="865" y2="600" stroke-width="2.2"/>
      </g>

      <!-- Official Deed Signature Box & Seal on Right Page -->
      <g transform="translate(735, 665)">
        <!-- Signature line -->
        <line x1="-120" y1="0" x2="20" y2="0" stroke="#111827" stroke-width="2"/>
        <!-- Simulated Bengali Signature Ink Flow -->
        <path d="M -110,-14 Q -85,-32 -70,-8 Q -55,-26 -35,-6 Q -15,-20 5,-4" fill="none" stroke="#111827" stroke-width="2.5" stroke-linecap="round"/>
        <text x="-50" y="20" text-anchor="middle" font-family="'SolaimanLipi', sans-serif" font-size="12" font-weight="600" fill="#374151">গ্রহীতা / দাতার স্বাক্ষর</text>

        <!-- Official Notary Stamp Seal Beside Signature -->
        <g transform="translate(95, -5)">
          <circle cx="0" cy="0" r="34" fill="#111827"/>
          <circle cx="0" cy="0" r="30" fill="none" stroke="#ffffff" stroke-width="2"/>
          <circle cx="0" cy="0" r="26" fill="none" stroke="#ffffff" stroke-width="1" stroke-dasharray="3,2"/>
          <!-- Star in center -->
          <polygon points="0,-12 3,-3 12,-3 5,2 8,11 0,6 -8,11 -5,2 -12,-3 -3,-3" fill="#ffffff"/>
          <text x="0" y="20" text-anchor="middle" font-family="'SolaimanLipi', sans-serif" font-size="9" font-weight="bold" fill="#ffffff">রেজিস্টার্ড</text>
        </g>
      </g>
    </g>

    <!-- Center Binding Spine & Silk Bookmark Ribbon (Monochrome) -->
    <!-- Center Spine Ridge -->
    <path d="M 500,205 C 506,200 518,200 524,205 L 524,785 C 518,790 506,790 500,785 Z" fill="url(#spineGrad)" stroke="#111827" stroke-width="2"/>

    <!-- Black Ribbon Bookmark with Silver Edge -->
    <path d="M 506,200 L 518,200 L 526,850 L 512,830 L 498,850 Z" fill="#111827" stroke="#cbd5e1" stroke-width="1.5"/>

    <!-- Classic Metallic Fountain Pen Resting Across Open Page -->
    <g transform="translate(510, 480) rotate(-42)">
      <!-- Pen Shadow -->
      <rect x="-10" y="-170" width="22" height="340" rx="10" fill="#000000" opacity="0.3" filter="blur(6px)"/>
      <!-- Pen Barrel (Black Lacquer) -->
      <rect x="-8" y="-160" width="16" height="240" rx="7" fill="#0f172a" stroke="#475569" stroke-width="1.5"/>
      <!-- Silver Clip -->
      <rect x="-3" y="-150" width="6" height="90" rx="3" fill="url(#silverGrad)"/>
      <circle cx="0" cy="-60" r="5" fill="url(#silverGrad)"/>
      <!-- Silver Band -->
      <rect x="-9" y="70" width="18" height="12" fill="url(#silverGrad)"/>
      <!-- Silver Nib Section -->
      <path d="M -8,82 L 8,82 L 6,110 L -6,110 Z" fill="#334155"/>
      <!-- Classic Calligraphy Nib -->
      <path d="M -7,110 L 7,110 L 0,148 Z" fill="url(#silverGrad)" stroke="#64748b" stroke-width="1"/>
      <line x1="0" y1="112" x2="0" y2="138" stroke="#0f172a" stroke-width="1.5"/>
      <circle cx="0" cy="126" r="1.5" fill="#0f172a"/>
    </g>

  </g>
</svg>`;
}

async function generateSingleIcon() {
  console.log('Generating book ledger icon in all standard PWA formats...');
  const svgMaskable = createBookSvg(true);
  const svgBuffer = Buffer.from(svgMaskable);

  // 1. icon-512.png (512x512 32-bit RGBA)
  const buf512 = await sharp(svgBuffer)
    .resize(512, 512)
    .png({ palette: false, quality: 100 })
    .toBuffer();
  fs.writeFileSync(path.join(rootDir, 'icon-512.png'), buf512);
  fs.writeFileSync(path.join(rootDir, 'icon.png'), buf512);

  // 2. icon-192.png (192x192 32-bit RGBA)
  const buf192 = await sharp(svgBuffer)
    .resize(192, 192)
    .png({ palette: false, quality: 100 })
    .toBuffer();
  fs.writeFileSync(path.join(rootDir, 'icon-192.png'), buf192);

  // 3. apple-touch-icon.png (180x180 32-bit RGBA)
  const buf180 = await sharp(svgBuffer)
    .resize(180, 180)
    .png({ palette: false, quality: 100 })
    .toBuffer();
  fs.writeFileSync(path.join(rootDir, 'apple-touch-icon.png'), buf180);

  // 4. favicon.ico (32x32)
  const buf32 = await sharp(svgBuffer)
    .resize(32, 32)
    .png({ palette: false, quality: 100 })
    .toBuffer();
  fs.writeFileSync(path.join(rootDir, 'favicon.ico'), buf32);

  console.log('Successfully generated icon-192.png, icon-512.png, icon.png, apple-touch-icon.png, favicon.ico!');
}

generateSingleIcon().catch(err => {
  console.error('Icon generation failed:', err);
  process.exit(1);
});
