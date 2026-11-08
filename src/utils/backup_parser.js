/**
 * Pure parsing and merging utilities for Dalil App backups (single or multi-file)
 */

export function parseBackupJson(rawText, filename = 'backup.json') {
    if (!rawText || typeof rawText !== 'string') {
        return { valid: false, records: [], count: 0, filename, error: 'ফাইলটি খালি বা পড়া যায়নি' };
    }

    try {
        const parsed = JSON.parse(rawText);
        let rawRecords = [];
        let header = null;
        let projectName = null;

        if (Array.isArray(parsed)) {
            rawRecords = parsed;
        } else if (parsed && typeof parsed === 'object') {
            if (Array.isArray(parsed.dalilRecords)) {
                rawRecords = parsed.dalilRecords;
                header = parsed.header || null;
                projectName = parsed.projectName || null;
            } else if (parsed.dalil_no || parsed.khatians || parsed.receiversHTML) {
                rawRecords = [parsed];
            }
        }

        // Filter and normalize valid deed items
        const validRecords = rawRecords.filter(r => r && typeof r === 'object' && (r.dalil_no || r.receiversHTML || r.khatians || r.dateVal));

        const normalized = validRecords.map((r, idx) => {
            const khatians = Array.isArray(r.khatians) && r.khatians.length > 0
                ? r.khatians.map(k => ({
                    type: k.type || '',
                    no: k.no !== undefined && k.no !== null ? String(k.no) : '-',
                    land: k.land !== undefined && k.land !== null ? String(k.land) : '-',
                    dag: k.dag !== undefined && k.dag !== null ? String(k.dag) : '-'
                }))
                : [{ type: '', no: '-', land: '-', dag: '-' }];

            return {
                sl_no: idx + 1,
                dalil_no: r.dalil_no ? String(r.dalil_no).trim() : '-',
                dateVal: r.dateVal ? String(r.dateVal).trim() : '-',
                receiversHTML: r.receiversHTML || '',
                donorsHTML: r.donorsHTML || '',
                rawReceivers: Array.isArray(r.rawReceivers) ? r.rawReceivers : [],
                rawDonors: Array.isArray(r.rawDonors) ? r.rawDonors : [],
                total_land: r.total_land !== undefined && r.total_land !== null ? String(r.total_land).trim() : '-',
                khatians
            };
        });

        if (normalized.length === 0) {
            return { valid: false, records: [], count: 0, filename, error: 'ফাইলে কোনো সঠিক দলিলের তথ্য পাওয়া যায়নি' };
        }

        return {
            valid: true,
            records: normalized,
            count: normalized.length,
            filename,
            header,
            projectName
        };
    } catch (err) {
        return {
            valid: false,
            records: [],
            count: 0,
            filename,
            error: 'ফাইলটি সঠিক JSON ফরম্যাটে নেই'
        };
    }
}

export function mergeBackupRecords(currentRecords = [], newRecords = [], { skipDuplicates = true } = {}) {
    const safeCurrent = Array.isArray(currentRecords) ? [...currentRecords] : [];
    const safeNew = Array.isArray(newRecords) ? newRecords : [];

    if (!skipDuplicates) {
        const merged = safeCurrent.concat(safeNew).map((r, idx) => ({ ...r, sl_no: idx + 1 }));
        return {
            merged,
            addedCount: safeNew.length,
            duplicateCount: 0
        };
    }

    const existingDeedNos = new Set(
        safeCurrent
            .map(r => String(r.dalil_no || '').trim().toLowerCase())
            .filter(no => no && no !== '-')
    );

    const itemsToAdd = [];
    let duplicateCount = 0;

    for (const r of safeNew) {
        const dNo = String(r.dalil_no || '').trim().toLowerCase();
        if (dNo && dNo !== '-' && existingDeedNos.has(dNo)) {
            duplicateCount++;
        } else {
            if (dNo && dNo !== '-') {
                existingDeedNos.add(dNo);
            }
            itemsToAdd.push(r);
        }
    }

    const merged = safeCurrent.concat(itemsToAdd).map((r, idx) => ({ ...r, sl_no: idx + 1 }));

    return {
        merged,
        addedCount: itemsToAdd.length,
        duplicateCount
    };
}
