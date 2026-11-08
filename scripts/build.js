import { execSync } from 'child_process';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const rootDir = path.resolve(__dirname, '..');

console.log('🚀 Building modern Dalil Record assets...');

// 1. Bundle src/js/app.js with esbuild
try {
    execSync('npx --yes esbuild src/js/app.js --bundle --minify --format=iife --outfile=app.js', {
        cwd: rootDir,
        stdio: 'inherit'
    });
    console.log('✅ Bundled app.js successfully');
} catch (err) {
    console.error('❌ Failed to bundle app.js with esbuild', err);
    process.exit(1);
}

// 2. Copy styles.css to root
fs.copyFileSync(
    path.join(rootDir, 'src', 'css', 'styles.css'),
    path.join(rootDir, 'styles.css')
);
console.log('✅ Copied styles.css to root');

// 3. Sync to Android Assets
const androidWwwDir = path.join(rootDir, 'android', 'app', 'src', 'main', 'assets', 'www');
if (fs.existsSync(androidWwwDir)) {
    fs.copyFileSync(path.join(rootDir, 'index.html'), path.join(androidWwwDir, 'index.html'));
    fs.copyFileSync(path.join(rootDir, 'app.js'), path.join(androidWwwDir, 'app.js'));
    fs.copyFileSync(path.join(rootDir, 'styles.css'), path.join(androidWwwDir, 'styles.css'));
    if (fs.existsSync(path.join(rootDir, 'manifest.json'))) {
        fs.copyFileSync(path.join(rootDir, 'manifest.json'), path.join(androidWwwDir, 'manifest.json'));
    }
    if (fs.existsSync(path.join(rootDir, 'icon-192.png'))) {
        fs.copyFileSync(path.join(rootDir, 'icon-192.png'), path.join(androidWwwDir, 'icon-192.png'));
    }
    if (fs.existsSync(path.join(rootDir, 'icon-512.png'))) {
        fs.copyFileSync(path.join(rootDir, 'icon-512.png'), path.join(androidWwwDir, 'icon-512.png'));
    }
    console.log('✅ Synchronized all assets to Android www directory');
}

console.log('🎉 Modern build completed successfully!');
