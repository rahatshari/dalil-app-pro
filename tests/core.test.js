import test from 'node:test';
import assert from 'node:assert/strict';
import { toBanglaNum, toEnglishNum, formatAutoDate } from '../src/core/bangla.js';
import { compareSemVer, parseGitHubRelease } from '../src/core/updater.js';
import { normalizeRecord, mergeRecords } from '../src/core/importer.js';

test('Bangla numeral conversions', () => {
    assert.equal(toBanglaNum(1234567890), '১২৩৪৫৬৭৮৯০');
    assert.equal(toBanglaNum('23/12/2001'), '২৩/১২/২০০১');
    assert.equal(toBanglaNum(''), '');
    assert.equal(toBanglaNum(null), '');

    assert.equal(toEnglishNum('১২৩৪৫৬৭৮৯০'), '1234567890');
    assert.equal(toEnglishNum('২৩/১২/২০০১'), '23/12/2001');
});

test('Automatic date formatting with slashes', () => {
    assert.equal(formatAutoDate('2'), '২');
    assert.equal(formatAutoDate('23'), '২৩/');
    assert.equal(formatAutoDate('231'), '২৩/১');
    assert.equal(formatAutoDate('2312'), '২৩/১২/');
    assert.equal(formatAutoDate('23122001'), '২৩/১২/২০০১');
    assert.equal(formatAutoDate('২৩১২২০০১'), '২৩/১২/২০০১');
});

test('Semantic version comparison', () => {
    assert.equal(compareSemVer('1.0.1', '1.0.2'), true);
    assert.equal(compareSemVer('1.0.2', '1.0.1'), false);
    assert.equal(compareSemVer('1.0.1', '1.0.1'), false);
    assert.equal(compareSemVer('v1.0.1', 'v1.1.0'), true);
    assert.equal(compareSemVer('1.0.9', '1.0.10'), true);
    assert.equal(compareSemVer('1.0.10', '1.0.9'), false);
});

test('GitHub release parsing', () => {
    const mockRelease = {
        tag_name: 'v1.0.5',
        body: 'Bug fixes and performance improvements',
        assets: [
            { name: 'dalil-record-v1.0.5.apk', browser_download_url: 'https://example.com/app.apk' }
        ]
    };

    const parsed = parseGitHubRelease(mockRelease, '1.0.1');
    assert.equal(parsed.hasUpdate, true);
    assert.equal(parsed.latestVersion, '1.0.5');
    assert.equal(parsed.currentVersion, '1.0.1');
    assert.equal(parsed.downloadUrl, 'https://example.com/app.apk');
});

test('Record normalization and deduplication merge', () => {
    const existing = [
        { sl_no: 1, dalil_no: '১০০১', total_land: '৫' }
    ];

    const incoming = [
        { dalil_no: '১০০১', total_land: '৫' }, // duplicate
        { dalil_no: '১০০২', total_land: '১০' }  // new
    ];

    const result = mergeRecords(existing, incoming, true);
    assert.equal(result.duplicateCount, 1);
    assert.equal(result.addedCount, 1);
    assert.equal(result.merged.length, 2);
    assert.equal(result.merged[0].sl_no, 1);
    assert.equal(result.merged[1].sl_no, 2);
    assert.equal(result.merged[1].dalil_no, '১০০২');
});
