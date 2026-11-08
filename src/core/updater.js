/**
 * Application Version and Update Checker Utilities
 */

/**
 * Compare two semantic version strings (e.g. "1.0.1" and "1.0.2")
 * Returns true if remote is strictly newer than current
 */
export function compareSemVer(current, remote) {
    if (!current || !remote) return false;
    const cleanCurrent = current.toString().replace(/[^0-9.]/g, '');
    const cleanRemote = remote.toString().replace(/[^0-9.]/g, '');

    const cParts = cleanCurrent.split('.').map(n => parseInt(n, 10) || 0);
    const rParts = cleanRemote.split('.').map(n => parseInt(n, 10) || 0);
    const max = Math.max(cParts.length, rParts.length);

    for (let i = 0; i < max; i++) {
        const c = cParts[i] || 0;
        const r = rParts[i] || 0;
        if (r > c) return true;
        if (r < c) return false;
    }
    return false;
}

/**
 * Parses GitHub Releases API response for dalil-app-pro
 */
export function parseGitHubRelease(data, currentVersion) {
    if (!data || !data.tag_name) return null;
    const remoteTag = data.tag_name;
    const cleanRemote = remoteTag.replace(/[^0-9.]/g, '');
    const cleanCurrent = currentVersion.replace(/[^0-9.]/g, '');

    const hasUpdate = compareSemVer(cleanCurrent, cleanRemote);
    let apkDownloadUrl = '';
    if (Array.isArray(data.assets)) {
        const apkAsset = data.assets.find(a => a.name && a.name.endsWith('.apk'));
        if (apkAsset) {
            apkDownloadUrl = apkAsset.browser_download_url;
        }
    }

    return {
        hasUpdate,
        currentVersion: cleanCurrent,
        latestVersion: cleanRemote,
        releaseNotes: data.body || '',
        publishedAt: data.published_at || '',
        downloadUrl: apkDownloadUrl || data.html_url || 'https://github.com/rahatshari/dalil-app-pro/releases/latest',
        tagName: remoteTag
    };
}
