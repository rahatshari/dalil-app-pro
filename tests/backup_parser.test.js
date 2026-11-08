import test from 'node:test';
import assert from 'node:assert/strict';
import { parseBackupJson, mergeBackupRecords } from '../src/utils/backup_parser.js';

test('parseBackupJson handles standard array JSON', () => {
    const raw = JSON.stringify([
        { dalil_no: '১২৩৪', total_land: '১০', dateVal: '০১/০১/২০২৪', khatians: [{ no: '৫০', land: '১০', dag: '১০০' }] },
        { dalil_no: '৫৬৭৮', total_land: '২০', dateVal: '০২/০১/২০২৪' }
    ]);
    const res = parseBackupJson(raw, 'test1.json');
    assert.equal(res.valid, true);
    assert.equal(res.count, 2);
    assert.equal(res.records[0].dalil_no, '১২৩৪');
    assert.equal(res.records[0].sl_no, 1);
    assert.equal(res.records[1].dalil_no, '৫৬৭৮');
});

test('parseBackupJson handles Dalil project object format with header', () => {
    const raw = JSON.stringify({
        projectName: 'পাঠামারা প্রজেক্ট',
        header: { mouza: '৯৩ পাঠামারা', creator: 'রাহাত' },
        dalilRecords: [
            { dalil_no: '৯৯৯', total_land: '১৫', dateVal: '০৫/০২/২০২৪' }
        ]
    });
    const res = parseBackupJson(raw, 'project.json');
    assert.equal(res.valid, true);
    assert.equal(res.count, 1);
    assert.equal(res.projectName, 'পাঠামারা প্রজেক্ট');
    assert.equal(res.header.mouza, '৯৩ পাঠামারা');
});

test('parseBackupJson gracefully catches invalid JSON syntax', () => {
    const res = parseBackupJson('{ broken json', 'bad.json');
    assert.equal(res.valid, false);
    assert.equal(res.count, 0);
    assert.match(res.error, /JSON/);
});

test('mergeBackupRecords skips duplicate deed numbers when requested', () => {
    const existing = [
        { sl_no: 1, dalil_no: '১০০', total_land: '৫' },
        { sl_no: 2, dalil_no: '২০০', total_land: '১০' }
    ];
    const incoming = [
        { dalil_no: '২০০', total_land: '১০' }, // duplicate
        { dalil_no: '৩০০', total_land: '১৫' }  // new
    ];

    const result = mergeBackupRecords(existing, incoming, { skipDuplicates: true });
    assert.equal(result.merged.length, 3);
    assert.equal(result.addedCount, 1);
    assert.equal(result.duplicateCount, 1);
    assert.equal(result.merged[2].dalil_no, '৩০০');
    assert.equal(result.merged[2].sl_no, 3);
});

test('mergeBackupRecords keeps duplicates if skipDuplicates is false', () => {
    const existing = [{ sl_no: 1, dalil_no: '১০০' }];
    const incoming = [{ dalil_no: '১০০' }];
    const result = mergeBackupRecords(existing, incoming, { skipDuplicates: false });
    assert.equal(result.merged.length, 2);
    assert.equal(result.addedCount, 1);
    assert.equal(result.duplicateCount, 0);
});
