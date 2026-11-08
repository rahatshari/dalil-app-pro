/**
 * Backup Import and Deduplication Engine
 */

export function normalizeRecord(r) {
    if (!r || typeof r !== 'object') return null;
    
    // Ensure khatians array exists and is formatted
    let khatians = Array.isArray(r.khatians) ? r.khatians : [];
    if (khatians.length === 0) {
        khatians = [{ type: '', no: '-', land: '-', dag: '-' }];
    } else {
        khatians = khatians.map(k => ({
            type: k.type || '',
            no: k.no || '-',
            land: k.land || '-',
            dag: k.dag || '-'
        }));
    }

    return {
        sl_no: r.sl_no || 1,
        dalil_no: r.dalil_no ? String(r.dalil_no).trim() : '-',
        dateVal: r.dateVal ? String(r.dateVal).trim() : '-',
        rawReceivers: Array.isArray(r.rawReceivers) ? r.rawReceivers : [],
        rawDonors: Array.isArray(r.rawDonors) ? r.rawDonors : [],
        total_land: r.total_land ? String(r.total_land).trim() : '-',
        khatians: khatians,
        receiversHTML: r.receiversHTML || '',
        donorsHTML: r.donorsHTML || ''
    };
}

/**
 * Merge existing records with incoming records, optionally skipping duplicate dalil numbers
 */
export function mergeRecords(existingRecords, incomingRecords, skipDuplicates = true) {
    const existing = Array.isArray(existingRecords) ? [...existingRecords] : [];
    const incoming = Array.isArray(incomingRecords) ? incomingRecords : [];
    
    let duplicateCount = 0;
    const addedItems = [];

    if (skipDuplicates) {
        const existingNos = new Set(
            existing
                .map(r => String(r.dalil_no || '').trim().toLowerCase())
                .filter(n => n && n !== '-')
        );

        for (const item of incoming) {
            const normalized = normalizeRecord(item);
            if (!normalized) continue;
            
            const dNo = normalized.dalil_no.toLowerCase();
            if (dNo && dNo !== '-' && existingNos.has(dNo)) {
                duplicateCount++;
            } else {
                if (dNo && dNo !== '-') {
                    existingNos.add(dNo);
                }
                addedItems.push(normalized);
            }
        }
    } else {
        for (const item of incoming) {
            const normalized = normalizeRecord(item);
            if (normalized) addedItems.push(normalized);
        }
    }

    const merged = existing.concat(addedItems);
    // Recalculate 1-based serial numbers
    merged.forEach((item, index) => {
        item.sl_no = index + 1;
    });

    return {
        merged,
        addedCount: addedItems.length,
        duplicateCount
    };
}
