/**
 * Dalil Record Application - Main Controller
 * Modernized ES6+ Architecture
 */

import { toBanglaNum, toEnglishNum, formatAutoDate, getTodayBanglaDate } from '../core/bangla.js';
import {
    loadStoredRecords, saveStoredRecords,
    loadSavedProjects, saveSavedProjects,
    loadStoredHeader, saveStoredHeader, getResolvedHeader,
    DRAFT_KEY, MOTHER_PREF_KEY, AUTO_UPDATE_KEY, ORIENTATION_KEY
} from '../core/storage.js';
import { compareSemVer, parseGitHubRelease } from '../core/updater.js';
import { normalizeRecord, mergeRecords } from '../core/importer.js';

// Application State
export const state = {
    records: [],
    editIndex: -1,
    isMotherNameEnabled: false,
    autoUpdateEnabled: true,
    orientation: 'portrait',
    targetLoadProjectId: null,
    stagedImportData: null,
    isLoadingDraft: false,
    draftTimeout: null,
    appVersion: '1.0.1',
    latestReleaseData: null
};

// DOM References Cache
const dom = {};

export function initApp() {
    cacheDomElements();
    loadPreferences();
    loadHeaderData();
    loadRecordsData();
    loadDraftData();
    setupEventListeners();
    setupDateInputs();
    initAppUpdateSystem();
    initOfflineDetection();
    registerServiceWorker();
    updateSavedProjectsBadge();
}

function cacheDomElements() {
    dom.settingsMenu = document.getElementById('settings-menu');
    dom.toggleMother = document.getElementById('toggle-mother-name');
    dom.toggleAutoUpdate = document.getElementById('toggle-auto-update');
    dom.printOrientation = document.getElementById('print-orientation');
    dom.currentVerBadge = document.getElementById('current-app-version-badge');
    dom.offlineBadge = document.getElementById('header-offline-badge');
    dom.appUpdateBtn = document.getElementById('app-update-btn');
    dom.appUpdateIcon = document.getElementById('app-update-icon');
    dom.appUpdateText = document.getElementById('app-update-text');

    // Forms
    dom.displaySlNo = document.getElementById('display_sl_no');
    dom.dalilNo = document.getElementById('dalil_no');
    dom.dateText = document.getElementById('date_text');
    dom.datePicker = document.getElementById('date_picker');
    dom.receiversContainer = document.getElementById('receivers-container');
    dom.donorsContainer = document.getElementById('donors-container');
    dom.totalLand = document.getElementById('total_land');
    dom.khatianContainer = document.getElementById('khatian-container');

    dom.btnAddRecord = document.getElementById('btn-add-record');
    dom.editActionContainer = document.getElementById('edit-action-container');
    dom.btnUpdateRecord = document.getElementById('btn-update-record');
    dom.btnCancelEdit = document.getElementById('btn-cancel-edit');

    // Header Inputs
    dom.gTitle = document.getElementById('g_main_title');
    dom.gZila = document.getElementById('g_zila');
    dom.gUpazila = document.getElementById('g_upazila');
    dom.gMouza = document.getElementById('g_mouza');
    dom.gCreator = document.getElementById('g_creator');
    dom.gDate = document.getElementById('g_date');
    dom.headerContent = document.getElementById('global-header-content');
    dom.headerToggleIcon = document.getElementById('header-toggle-icon');

    // Print Header Output
    dom.outTitle = document.getElementById('out_main_title');
    dom.outZila = document.getElementById('out_zila');
    dom.outUpazila = document.getElementById('out_upazila');
    dom.outMouza = document.getElementById('out_mouza');
    dom.outCreator = document.getElementById('out_creator');
    dom.outDate = document.getElementById('out_g_date');

    // Table
    dom.mainTable = document.getElementById('main-table');

    // Modals
    dom.toast = document.getElementById('toast');
    dom.saveModal = document.getElementById('save-collection-modal');
    dom.savedCollectionsModal = document.getElementById('saved-collections-modal');
    dom.loadStrategyModal = document.getElementById('load-strategy-modal');
    dom.importModal = document.getElementById('import-modal');
    dom.updateModal = document.getElementById('app-update-modal');
    dom.directFileInput = document.getElementById('directImportFile');
    dom.dynamicPrintCss = document.getElementById('dynamic-print-css');
}

function loadPreferences() {
    state.isMotherNameEnabled = localStorage.getItem(MOTHER_PREF_KEY) === 'true';
    if (dom.toggleMother) dom.toggleMother.checked = state.isMotherNameEnabled;
    toggleMotherSettings(state.isMotherNameEnabled);

    state.autoUpdateEnabled = localStorage.getItem(AUTO_UPDATE_KEY) !== 'false';
    if (dom.toggleAutoUpdate) dom.toggleAutoUpdate.checked = state.autoUpdateEnabled;

    state.orientation = localStorage.getItem(ORIENTATION_KEY) || 'portrait';
    if (dom.printOrientation) dom.printOrientation.value = state.orientation;
    applyOrientation(state.orientation);
}

function loadHeaderData() {
    const resolved = getResolvedHeader();
    if (dom.gTitle) dom.gTitle.value = resolved.rawSaved.title || '';
    if (dom.gZila) dom.gZila.value = resolved.rawSaved.zila || '';
    if (dom.gUpazila) dom.gUpazila.value = resolved.rawSaved.upazila || '';
    if (dom.gMouza) dom.gMouza.value = resolved.rawSaved.mouza || '';
    if (dom.gCreator) dom.gCreator.value = resolved.rawSaved.creator || '';
    if (dom.gDate) dom.gDate.value = resolved.rawSaved.date || '';

    applyHeaderToUI(resolved);
}

function applyHeaderToUI(data) {
    if (dom.outTitle) dom.outTitle.innerText = data.title;
    if (dom.outZila) dom.outZila.innerText = data.zila;
    if (dom.outUpazila) dom.outUpazila.innerText = data.upazila;
    if (dom.outMouza) dom.outMouza.innerText = data.mouza;
    if (dom.outCreator) dom.outCreator.innerText = data.creator;
    if (dom.outDate) dom.outDate.innerText = data.date;
}

function loadRecordsData() {
    state.records = loadStoredRecords();
    recalculateSerialNumbers();
    renderTable();
}

function recalculateSerialNumbers() {
    state.records.forEach((r, idx) => {
        r.sl_no = idx + 1;
    });
    if (dom.displaySlNo) {
        dom.displaySlNo.innerText = toBanglaNum(state.records.length + 1);
    }
}

export function showToast(message) {
    if (!dom.toast) return;
    dom.toast.innerText = message;
    dom.toast.classList.add('show');
    setTimeout(() => {
        dom.toast.classList.remove('show');
    }, 3000);
}

// ==========================================================================
// Settings & Layout Handlers
// ==========================================================================
export function toggleSettings() {
    if (!dom.settingsMenu) return;
    const isVisible = dom.settingsMenu.style.display === 'block';
    dom.settingsMenu.style.display = isVisible ? 'none' : 'block';
}

export function toggleMotherSettings(checked) {
    state.isMotherNameEnabled = checked;
    localStorage.setItem(MOTHER_PREF_KEY, checked ? 'true' : 'false');
    if (checked) {
        document.body.classList.add('show-mother-name');
    } else {
        document.body.classList.remove('show-mother-name');
    }
}

export function changeOrientation(orient) {
    state.orientation = orient;
    localStorage.setItem(ORIENTATION_KEY, orient);
    applyOrientation(orient);
    showToast(orient === 'portrait' ? 'পোর্ট্রেট লেআউট সেট হয়েছে!' : 'ল্যান্ডস্কেপ লেআউট সেট হয়েছে!');
    if (dom.settingsMenu) dom.settingsMenu.style.display = 'none';
}

function applyOrientation(orient) {
    if (!dom.dynamicPrintCss) return;
    if (orient === 'landscape') {
        dom.dynamicPrintCss.innerHTML = '@page { margin: 12mm 14mm 12mm 14mm; size: landscape; }';
    } else {
        dom.dynamicPrintCss.innerHTML = '@page { margin: 14mm 12mm 14mm 12mm; size: portrait; }';
    }
}

export function toggleHeaderContent() {
    if (!dom.headerContent || !dom.headerToggleIcon) return;
    if (dom.headerContent.style.display === 'block') {
        dom.headerContent.style.display = 'none';
        dom.headerToggleIcon.innerText = 'expand_more';
    } else {
        dom.headerContent.style.display = 'block';
        dom.headerToggleIcon.innerText = 'expand_less';
    }
}

export function setTodayHeaderDate() {
    if (dom.gDate) {
        dom.gDate.value = getTodayBanglaDate();
    }
}

export function saveGlobalHeader() {
    const data = {
        title: dom.gTitle ? dom.gTitle.value.trim() : '',
        zila: dom.gZila ? dom.gZila.value.trim() : '',
        upazila: dom.gUpazila ? dom.gUpazila.value.trim() : '',
        mouza: dom.gMouza ? dom.gMouza.value.trim() : '',
        creator: dom.gCreator ? dom.gCreator.value.trim() : '',
        date: dom.gDate ? dom.gDate.value.trim() : ''
    };
    saveStoredHeader(data);
    applyHeaderToUI(getResolvedHeader());
    showToast('হেডার তথ্য সেভ হয়েছে!');
    toggleHeaderContent();
}

export function clearGlobalHeader() {
    if (confirm('প্রিন্ট পেজের হেডার তথ্য ডিফল্টে রিসেট করতে চান?')) {
        if (dom.gTitle) dom.gTitle.value = '';
        if (dom.gZila) dom.gZila.value = '';
        if (dom.gUpazila) dom.gUpazila.value = '';
        if (dom.gMouza) dom.gMouza.value = '';
        if (dom.gCreator) dom.gCreator.value = '';
        if (dom.gDate) dom.gDate.value = '';
        localStorage.removeItem('myDalilHeaderFinal');
        applyHeaderToUI(getResolvedHeader());
        toggleHeaderContent();
        showToast('হেডার তথ্য ডিফল্টে রিসেট হয়েছে!');
    }
}

// ==========================================================================
// Form Date Sync and Auto-Formatting
// ==========================================================================
function setupDateInputs() {
    if (dom.dateText) {
        dom.dateText.addEventListener('input', function() {
            const formatted = formatAutoDate(this.value);
            if (formatted !== this.value) {
                this.value = formatted;
            }
            syncPickerFromText();
            scheduleSaveDraft();
        });

        dom.dateText.addEventListener('keydown', function(e) {
            if (e.key === 'Backspace') {
                const val = this.value;
                const start = this.selectionStart;
                const end = this.selectionEnd;
                if (start === end && start > 0 && val[start - 1] === '/') {
                    e.preventDefault();
                    this.value = val.slice(0, start - 2) + val.slice(start);
                    const newPos = Math.max(0, start - 2);
                    this.setSelectionRange(newPos, newPos);
                    syncPickerFromText();
                    scheduleSaveDraft();
                }
            }
        });
    }

    if (dom.datePicker) {
        dom.datePicker.addEventListener('change', syncDateFromPicker);
    }
}

export function setTodayRecordDate() {
    if (dom.dateText) {
        dom.dateText.value = getTodayBanglaDate();
        syncPickerFromText();
        scheduleSaveDraft();
    }
}

function syncDateFromPicker() {
    if (!dom.datePicker || !dom.datePicker.value) return;
    const parts = dom.datePicker.value.split('-');
    if (parts.length === 3) {
        dom.dateText.value = toBanglaNum(`${parts[2]}/${parts[1]}/${parts[0]}`);
        scheduleSaveDraft();
    }
}

function syncPickerFromText() {
    if (!dom.dateText || !dom.datePicker) return;
    const val = toEnglishNum(dom.dateText.value.trim());
    const match = val.match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
    if (match) {
        dom.datePicker.value = `${match[3]}-${match[2]}-${match[1]}`;
    }
}

// ==========================================================================
// Dynamic Repeaters (Receivers, Donors, Khatians)
// ==========================================================================
export function addReceiverField(data = null) {
    if (!dom.receiversContainer) return;
    const div = document.createElement('div');
    div.className = 'repeater-box receiver-item';
    div.innerHTML = `
        <button type="button" class="btn-remove-row" onclick="window.removeRepeaterRow(this)" title="মুছুন">
            <span class="material-icons" style="font-size:18px;">close</span>
        </button>
        <div class="repeater-grid">
            <div class="form-group">
                <label>নাম</label>
                <input type="text" class="rec_name" placeholder="নাম লিখুন" value="${data ? escapeAttr(data.name) : ''}">
            </div>
            <div class="form-group">
                <label>পিতা/স্বামীর নাম</label>
                <input type="text" class="rec_father" placeholder="পিতা/স্বামীর নাম" value="${data ? escapeAttr(data.father) : ''}">
            </div>
            <div class="form-group mother-field-container">
                <label>মাতার নাম</label>
                <input type="text" class="rec_mother" placeholder="মাতার নাম" value="${data ? escapeAttr(data.mother) : ''}">
            </div>
            <div class="form-group">
                <label>গ্রাম/ঠিকানা</label>
                <input type="text" class="rec_village" placeholder="গ্রাম বা ঠিকানা" value="${data ? escapeAttr(data.village) : ''}">
            </div>
        </div>
    `;
    dom.receiversContainer.appendChild(div);
    attachDraftListeners(div);
}

export function addDonorField(data = null) {
    if (!dom.donorsContainer) return;
    const div = document.createElement('div');
    div.className = 'repeater-box donor-item';
    div.innerHTML = `
        <button type="button" class="btn-remove-row" onclick="window.removeRepeaterRow(this)" title="মুছুন">
            <span class="material-icons" style="font-size:18px;">close</span>
        </button>
        <div class="repeater-grid">
            <div class="form-group">
                <label>নাম</label>
                <input type="text" class="don_name" placeholder="নাম লিখুন" value="${data ? escapeAttr(data.name) : ''}">
            </div>
            <div class="form-group">
                <label>পিতা/স্বামীর নাম</label>
                <input type="text" class="don_father" placeholder="পিতা/স্বামীর নাম" value="${data ? escapeAttr(data.father) : ''}">
            </div>
            <div class="form-group mother-field-container">
                <label>মাতার নাম</label>
                <input type="text" class="don_mother" placeholder="মাতার নাম" value="${data ? escapeAttr(data.mother) : ''}">
            </div>
            <div class="form-group">
                <label>গ্রাম/ঠিকানা</label>
                <input type="text" class="don_village" placeholder="গ্রাম বা ঠিকানা" value="${data ? escapeAttr(data.village) : ''}">
            </div>
        </div>
    `;
    dom.donorsContainer.appendChild(div);
    attachDraftListeners(div);
}

export function addKhatianField(data = null) {
    if (!dom.khatianContainer) return;
    const div = document.createElement('div');
    div.className = 'repeater-box khatian-item';
    const selectedType = data ? (data.type || '') : '';

    div.innerHTML = `
        <button type="button" class="btn-remove-row" onclick="window.removeRepeaterRow(this)" title="মুছুন">
            <span class="material-icons" style="font-size:18px;">close</span>
        </button>
        <div class="repeater-grid">
            <div class="form-group">
                <label>খতিয়ানের ধরন</label>
                <select class="kh_type">
                    <option value="" ${selectedType === '' ? 'selected' : ''}>সিলেক্ট করুন</option>
                    <option value="এস.এ" ${selectedType === 'এস.এ' ? 'selected' : ''}>এস.এ</option>
                    <option value="সি.এস" ${selectedType === 'সি.এস' ? 'selected' : ''}>সি.এস</option>
                    <option value="আর.এস" ${selectedType === 'আর.এস' ? 'selected' : ''}>আর.এস</option>
                    <option value="বি.এস" ${selectedType === 'বি.এস' ? 'selected' : ''}>বি.এস</option>
                    <option value="নামজারি" ${selectedType === 'নামজারি' ? 'selected' : ''}>নামজারি</option>
                    <option value="অন্যান্য" ${selectedType === 'অন্যান্য' ? 'selected' : ''}>অন্যান্য</option>
                </select>
            </div>
            <div class="form-group">
                <label>খতিয়ান নং</label>
                <input type="text" class="kh_no" placeholder="যেমন: ১০০" value="${data ? escapeAttr(data.no) : ''}">
            </div>
            <div class="form-group">
                <label>খতিয়ানের জমি</label>
                <input type="text" class="kh_land" placeholder="যেমন: ০.০৫" value="${data ? escapeAttr(data.land) : ''}">
            </div>
            <div class="form-group">
                <label>দাগ নং</label>
                <input type="text" class="kh_dag" placeholder="যেমন: ৫০, ৫২" value="${data ? escapeAttr(data.dag) : ''}">
            </div>
        </div>
    `;
    dom.khatianContainer.appendChild(div);
    attachDraftListeners(div);
}

function attachDraftListeners(container) {
    const inputs = container.querySelectorAll('input, select');
    inputs.forEach(el => {
        el.addEventListener('input', scheduleSaveDraft);
        el.addEventListener('change', scheduleSaveDraft);
    });
}

function escapeAttr(str) {
    if (!str) return '';
    return String(str).replace(/"/g, '&quot;');
}

function escapeHTML(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

// ==========================================================================
// Draft Auto-Save Engine
// ==========================================================================
function scheduleSaveDraft() {
    if (state.isLoadingDraft) return;
    clearTimeout(state.draftTimeout);
    state.draftTimeout = setTimeout(saveFormDraft, 400);
}

function saveFormDraft() {
    if (state.isLoadingDraft) return;
    const draft = {
        dalil_no: dom.dalilNo ? dom.dalilNo.value : '',
        date_text: dom.dateText ? dom.dateText.value : '',
        total_land: dom.totalLand ? dom.totalLand.value : '',
        receivers: collectReceiversData().rawList,
        donors: collectDonorsData().rawList,
        khatians: collectKhatiansData()
    };
    try {
        localStorage.setItem(DRAFT_KEY, JSON.stringify(draft));
    } catch (e) {}
}

function loadDraftData() {
    state.isLoadingDraft = true;
    try {
        const raw = localStorage.getItem(DRAFT_KEY);
        if (raw) {
            const draft = JSON.parse(raw);
            if (dom.dalilNo) dom.dalilNo.value = draft.dalil_no || '';
            if (dom.dateText) dom.dateText.value = draft.date_text || '';
            if (dom.totalLand) dom.totalLand.value = draft.total_land || '';
            syncPickerFromText();

            if (Array.isArray(draft.receivers) && draft.receivers.length > 0) {
                draft.receivers.forEach(r => addReceiverField(r));
            } else {
                addReceiverField();
            }

            if (Array.isArray(draft.donors) && draft.donors.length > 0) {
                draft.donors.forEach(d => addDonorField(d));
            } else {
                addDonorField();
            }

            if (Array.isArray(draft.khatians) && draft.khatians.length > 0) {
                draft.khatians.forEach(k => addKhatianField(k));
            } else {
                addKhatianField();
            }
        } else {
            addReceiverField();
            addDonorField();
            addKhatianField();
        }
    } catch (e) {
        addReceiverField();
        addDonorField();
        addKhatianField();
    } finally {
        state.isLoadingDraft = false;
    }
}

function clearFormDraft() {
    localStorage.removeItem(DRAFT_KEY);
}

// ==========================================================================
// Data Collection & Record Serialization
// ==========================================================================
function collectReceiversData() {
    const rawList = [];
    const htmlParts = [];
    const items = dom.receiversContainer.querySelectorAll('.receiver-item');

    items.forEach(item => {
        const name = item.querySelector('.rec_name')?.value?.trim() || '';
        const father = item.querySelector('.rec_father')?.value?.trim() || '';
        const mother = item.querySelector('.rec_mother')?.value?.trim() || '';
        const village = item.querySelector('.rec_village')?.value?.trim() || '';

        rawList.push({ name, father, mother, village });

        if (name || father || village) {
            let line = `<b>${escapeHTML(name || '-')}</b>`;
            if (father) line += `<br><span style="font-size:11.5px; color:#3c4043;">পিতা/স্বামী: ${escapeHTML(father)}</span>`;
            if (state.isMotherNameEnabled && mother) {
                line += `<br><span style="font-size:11.5px; color:#3c4043;">মাতা: ${escapeHTML(mother)}</span>`;
            }
            if (village) line += `<br><span style="font-size:11px; color:#5f6368;">গ্রাম: ${escapeHTML(village)}</span>`;
            htmlParts.push(line);
        }
    });

    return {
        rawList,
        html: htmlParts.length > 0 ? htmlParts.join('<hr style="border:none; border-top:1px dashed #dadce0; margin:4px 0;">') : '-'
    };
}

function collectDonorsData() {
    const rawList = [];
    const htmlParts = [];
    const items = dom.donorsContainer.querySelectorAll('.donor-item');

    items.forEach(item => {
        const name = item.querySelector('.don_name')?.value?.trim() || '';
        const father = item.querySelector('.don_father')?.value?.trim() || '';
        const mother = item.querySelector('.don_mother')?.value?.trim() || '';
        const village = item.querySelector('.don_village')?.value?.trim() || '';

        rawList.push({ name, father, mother, village });

        if (name || father || village) {
            let line = `<b>${escapeHTML(name || '-')}</b>`;
            if (father) line += `<br><span style="font-size:11.5px; color:#3c4043;">পিতা/স্বামী: ${escapeHTML(father)}</span>`;
            if (state.isMotherNameEnabled && mother) {
                line += `<br><span style="font-size:11.5px; color:#3c4043;">মাতা: ${escapeHTML(mother)}</span>`;
            }
            if (village) line += `<br><span style="font-size:11px; color:#5f6368;">গ্রাম: ${escapeHTML(village)}</span>`;
            htmlParts.push(line);
        }
    });

    return {
        rawList,
        html: htmlParts.length > 0 ? htmlParts.join('<hr style="border:none; border-top:1px dashed #dadce0; margin:4px 0;">') : '-'
    };
}

function collectKhatiansData() {
    const list = [];
    const items = dom.khatianContainer.querySelectorAll('.khatian-item');
    items.forEach(item => {
        const type = item.querySelector('.kh_type')?.value || '';
        const no = item.querySelector('.kh_no')?.value?.trim() || '';
        const land = item.querySelector('.kh_land')?.value?.trim() || '';
        const dag = item.querySelector('.kh_dag')?.value?.trim() || '';
        list.push({ type, no: no || '-', land: land || '-', dag: dag || '-' });
    });
    return list.length > 0 ? list : [{ type: '', no: '-', land: '-', dag: '-' }];
}

// ==========================================================================
// CRUD Operations
// ==========================================================================
export function addRecord() {
    const dalilNo = dom.dalilNo ? dom.dalilNo.value.trim() : '';
    const dateVal = dom.dateText ? dom.dateText.value.trim() : '';
    const totalLand = dom.totalLand ? dom.totalLand.value.trim() : '';

    const receivers = collectReceiversData();
    const donors = collectDonorsData();
    const khatians = collectKhatiansData();

    if (!dalilNo && receivers.rawList.every(r => !r.name) && donors.rawList.every(d => !d.name)) {
        showToast('অনুগ্রহ করে অন্তত দলিল নম্বর অথবা গ্রহীতা/দাতার তথ্য দিন!');
        return;
    }

    const newRecord = {
        sl_no: state.records.length + 1,
        dalil_no: dalilNo ? toBanglaNum(dalilNo) : '-',
        dateVal: dateVal ? toBanglaNum(dateVal) : '-',
        rawReceivers: receivers.rawList,
        rawDonors: donors.rawList,
        receiversHTML: receivers.html,
        donorsHTML: donors.html,
        total_land: totalLand ? toBanglaNum(totalLand) : '-',
        khatians: khatians
    };

    state.records.push(newRecord);
    recalculateSerialNumbers();
    saveStoredRecords(state.records);
    renderTable();
    resetForm();
    clearFormDraft();
    showToast('দলিলটি সফলভাবে তালিকায় যোগ করা হয়েছে!');

    // Smooth scroll to table
    const tableSec = document.getElementById('pdf-section');
    if (tableSec) {
        tableSec.scrollIntoView({ behavior: 'smooth' });
    }
}

export function editRecord(index) {
    const r = state.records[index];
    if (!r) return;
    state.editIndex = index;

    if (dom.dalilNo) dom.dalilNo.value = r.dalil_no !== '-' ? r.dalil_no : '';
    if (dom.dateText) dom.dateText.value = r.dateVal !== '-' ? r.dateVal : '';
    if (dom.totalLand) dom.totalLand.value = r.total_land !== '-' ? r.total_land : '';
    syncPickerFromText();

    // Populate Receivers
    dom.receiversContainer.innerHTML = '';
    if (Array.isArray(r.rawReceivers) && r.rawReceivers.length > 0) {
        r.rawReceivers.forEach(item => addReceiverField(item));
    } else {
        addReceiverField();
    }

    // Populate Donors
    dom.donorsContainer.innerHTML = '';
    if (Array.isArray(r.rawDonors) && r.rawDonors.length > 0) {
        r.rawDonors.forEach(item => addDonorField(item));
    } else {
        addDonorField();
    }

    // Populate Khatians
    dom.khatianContainer.innerHTML = '';
    if (Array.isArray(r.khatians) && r.khatians.length > 0) {
        r.khatians.forEach(item => addKhatianField(item));
    } else {
        addKhatianField();
    }

    if (dom.btnAddRecord) dom.btnAddRecord.style.display = 'none';
    if (dom.editActionContainer) dom.editActionContainer.style.display = 'flex';

    window.scrollTo({ top: 0, behavior: 'smooth' });
}

export function updateRecord() {
    if (state.editIndex < 0 || state.editIndex >= state.records.length) return;

    const dalilNo = dom.dalilNo ? dom.dalilNo.value.trim() : '';
    const dateVal = dom.dateText ? dom.dateText.value.trim() : '';
    const totalLand = dom.totalLand ? dom.totalLand.value.trim() : '';

    const receivers = collectReceiversData();
    const donors = collectDonorsData();
    const khatians = collectKhatiansData();

    state.records[state.editIndex] = {
        sl_no: state.editIndex + 1,
        dalil_no: dalilNo ? toBanglaNum(dalilNo) : '-',
        dateVal: dateVal ? toBanglaNum(dateVal) : '-',
        rawReceivers: receivers.rawList,
        rawDonors: donors.rawList,
        receiversHTML: receivers.html,
        donorsHTML: donors.html,
        total_land: totalLand ? toBanglaNum(totalLand) : '-',
        khatians: khatians
    };

    saveStoredRecords(state.records);
    renderTable();
    cancelEdit();
    showToast('দলিল তথ্য সফলভাবে আপডেট হয়েছে!');
}

export function cancelEdit() {
    state.editIndex = -1;
    resetForm();
    if (dom.btnAddRecord) dom.btnAddRecord.style.display = 'block';
    if (dom.editActionContainer) dom.editActionContainer.style.display = 'none';
    recalculateSerialNumbers();
}

export function deleteRecord(index) {
    if (confirm('আপনি কি নিশ্চিত যে এই দলিলটি মুছে ফেলতে চান?')) {
        if (state.editIndex === index) {
            cancelEdit();
        } else if (state.editIndex > index) {
            state.editIndex--;
        }
        state.records.splice(index, 1);
        recalculateSerialNumbers();
        saveStoredRecords(state.records);
        renderTable();
        showToast('দলিলটি মুছে ফেলা হয়েছে!');
    }
}

export function clearAllRecords() {
    if (state.records.length === 0) {
        showToast('মোছার মতো কোনো তথ্য নেই!');
        return;
    }
    if (confirm('সতর্কতা: আপনি কি সম্পূর্ণ তালিকা মুছে ফেলতে চান?')) {
        state.records = [];
        localStorage.removeItem('myDalilDataFinal');
        cancelEdit();
        renderTable();
        recalculateSerialNumbers();
        showToast('সম্পূর্ণ তালিকা মুছে ফেলা হয়েছে!');
    }
}

function resetForm() {
    if (dom.dalilNo) dom.dalilNo.value = '';
    if (dom.dateText) dom.dateText.value = '';
    if (dom.datePicker) dom.datePicker.value = '';
    if (dom.totalLand) dom.totalLand.value = '';

    dom.receiversContainer.innerHTML = '';
    dom.donorsContainer.innerHTML = '';
    dom.khatianContainer.innerHTML = '';

    addReceiverField();
    addDonorField();
    addKhatianField();
}

// ==========================================================================
// Table Renderer
// ==========================================================================
function renderTable() {
    if (!dom.mainTable) return;

    // Clear previous rows except thead
    const tbody = dom.mainTable.querySelector('tbody') || document.createElement('tbody');
    tbody.innerHTML = '';

    if (state.records.length === 0) {
        const emptyRow = document.createElement('tr');
        emptyRow.innerHTML = `
            <td colspan="10" style="padding: 30px; text-align: center; color: #5f6368; background: #ffffff;">
                <span class="material-icons" style="font-size: 36px; color: #bdc1c6; display: block; margin: 0 auto 6px auto;">description</span>
                <b>এখনও কোনো দলিল যোগ করা হয়নি</b>
                <div style="font-size: 12px; margin-top: 4px;">উপরের ফরম পূরণ করে "তালিকায় যোগ করুন" বাটনে চাপুন।</div>
            </td>
        `;
        tbody.appendChild(emptyRow);
        dom.mainTable.appendChild(tbody);
        updateSavedProjectsBadge();
        return;
    }

    state.records.forEach((r, idx) => {
        const kList = Array.isArray(r.khatians) && r.khatians.length > 0 ? r.khatians : [{ type: '', no: '-', land: '-', dag: '-' }];
        const rowSpan = kList.length;

        kList.forEach((kh, kIdx) => {
            const tr = document.createElement('tr');
            if (kIdx === 0) {
                // First sub-row has all span cells
                tr.innerHTML = `
                    <td rowspan="${rowSpan}"><b>${toBanglaNum(idx + 1)}</b></td>
                    <td rowspan="${rowSpan}">${r.dalil_no || '-'}</td>
                    <td rowspan="${rowSpan}">${r.dateVal || '-'}</td>
                    <td rowspan="${rowSpan}" style="text-align: left; padding: 6px 10px;">${r.receiversHTML || '-'}</td>
                    <td rowspan="${rowSpan}" style="text-align: left; padding: 6px 10px;">${r.donorsHTML || '-'}</td>
                    <td rowspan="${rowSpan}"><b>${r.total_land || '-'}</b></td>
                    <td>${kh.type ? `${escapeHTML(kh.type)} (${toBanglaNum(kh.no)})` : toBanglaNum(kh.no)}</td>
                    <td>${toBanglaNum(kh.land)}</td>
                    <td>${toBanglaNum(kh.dag)}</td>
                    <td rowspan="${rowSpan}" class="no-print">
                        <button type="button" class="row-action-btn row-action-edit" onclick="window.editDeedRecord(${idx})" title="এডিট">
                            <span class="material-icons" style="font-size: 18px;">edit</span>
                        </button>
                        <button type="button" class="row-action-btn row-action-delete" onclick="window.deleteDeedRecord(${idx})" title="মুছুন">
                            <span class="material-icons" style="font-size: 18px;">delete</span>
                        </button>
                    </td>
                `;
            } else {
                // Continuation rows for additional khatians
                tr.innerHTML = `
                    <td>${kh.type ? `${escapeHTML(kh.type)} (${toBanglaNum(kh.no)})` : toBanglaNum(kh.no)}</td>
                    <td>${toBanglaNum(kh.land)}</td>
                    <td>${toBanglaNum(kh.dag)}</td>
                `;
            }
            tbody.appendChild(tr);
        });
    });

    dom.mainTable.appendChild(tbody);
    updateSavedProjectsBadge();
}

// ==========================================================================
// Deed Projects & Archive Manager
// ==========================================================================
export function openSaveCurrentCollectionModal() {
    if (state.records.length === 0) {
        showToast('সংরক্ষণ করার মতো কোনো দলিল তথ্য নেই! আগে তালিকায় দলিল যোগ করুন।');
        return;
    }
    const countEl = document.getElementById('save-modal-record-count');
    if (countEl) countEl.innerText = toBanglaNum(state.records.length);

    const mouzaVal = dom.gMouza?.value?.trim() || 'মৌজা';
    const zilaVal = dom.gZila?.value?.trim() || '';
    const creatorVal = dom.gCreator?.value?.trim() || 'প্রস্তুতকারক';
    const dateVal = dom.gDate?.value?.trim() || getTodayBanglaDate();

    const mouzaDisplay = document.getElementById('save-modal-mouza-info');
    if (mouzaDisplay) mouzaDisplay.innerText = `${mouzaVal} ${zilaVal ? '(' + zilaVal + ')' : ''}`;

    const creatorDisplay = document.getElementById('save-modal-creator-info');
    if (creatorDisplay) creatorDisplay.innerText = creatorVal;

    const dateDisplay = document.getElementById('save-modal-date-info');
    if (dateDisplay) dateDisplay.innerText = dateVal;

    const nameInput = document.getElementById('save-collection-name');
    if (nameInput) {
        nameInput.value = `${mouzaVal} - ${toBanglaNum(state.records.length)}টি দলিল (${dateVal})`;
        setTimeout(() => { nameInput.focus(); nameInput.select(); }, 100);
    }

    if (dom.saveModal) dom.saveModal.style.display = 'flex';
}

export function closeSaveCollectionModal() {
    if (dom.saveModal) dom.saveModal.style.display = 'none';
}

export function saveCurrentCollection(andDownload = false) {
    if (state.records.length === 0) {
        showToast('সংরক্ষণ করার মতো কোনো দলিল তথ্য নেই!');
        return;
    }
    const nameInput = document.getElementById('save-collection-name');
    const title = nameInput ? nameInput.value.trim() : '';
    if (!title) {
        alert('অনুগ্রহ করে একটি নাম বা টাইটেল লিখুন!');
        nameInput?.focus();
        return;
    }

    const note = document.getElementById('save-collection-note')?.value?.trim() || '';
    const now = new Date();
    const dateStr = now.toLocaleDateString('bn-BD') + ' ' + now.toLocaleTimeString('bn-BD', { hour: '2-digit', minute: '2-digit' });

    const headerSnapshot = {
        main_title: dom.gTitle?.value || '',
        zila: dom.gZila?.value || '',
        upazila: dom.gUpazila?.value || '',
        mouza: dom.gMouza?.value || '',
        creator: dom.gCreator?.value || '',
        date: dom.gDate?.value || ''
    };

    const newProject = {
        id: 'proj_' + Date.now(),
        name: title,
        note: note,
        createdAt: now.toISOString(),
        dateDisplay: dateStr,
        recordsCount: state.records.length,
        header: headerSnapshot,
        records: JSON.parse(JSON.stringify(state.records))
    };

    const list = loadSavedProjects();
    list.unshift(newProject);
    saveSavedProjects(list);
    closeSaveCollectionModal();

    if (andDownload) {
        downloadProjectJSON(newProject);
        showToast(`"${title}" সেভ হয়েছে এবং ফাইল ডাউনলোড শুরু হয়েছে!`);
    } else {
        showToast(`"${title}" সফলভাবে সেভ হয়েছে!`);
    }
    updateSavedProjectsBadge();
}

function downloadProjectJSON(proj) {
    const payload = {
        projectName: proj.name,
        savedAt: proj.createdAt,
        totalRecords: proj.records.length,
        header: proj.header,
        dalilRecords: proj.records
    };
    const safeName = proj.name.replace(/[/\\?%*:|"<>]/g, '_').replace(/\s+/g, '_');
    const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = `${safeName}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(a.href);
}

export function openSavedCollectionsModal() {
    if (!dom.savedCollectionsModal) return;
    dom.savedCollectionsModal.style.display = 'flex';
    const searchInput = document.getElementById('saved-collection-search');
    if (searchInput) searchInput.value = '';
    renderSavedCollectionsList();
}

export function closeSavedCollectionsModal() {
    if (dom.savedCollectionsModal) dom.savedCollectionsModal.style.display = 'none';
}

export function filterSavedCollections() {
    const q = document.getElementById('saved-collection-search')?.value?.trim().toLowerCase() || '';
    renderSavedCollectionsList(q);
}

function renderSavedCollectionsList(query = '') {
    const listContainer = document.getElementById('saved-collections-list');
    const totalCountEl = document.getElementById('modal-saved-total-count');
    if (!listContainer) return;

    const allProjects = loadSavedProjects();
    if (totalCountEl) totalCountEl.innerText = toBanglaNum(allProjects.length);

    const filtered = query
        ? allProjects.filter(p => (p.name && p.name.toLowerCase().includes(query)) ||
                                 (p.note && p.note.toLowerCase().includes(query)) ||
                                 (p.header?.mouza && p.header.mouza.toLowerCase().includes(query)))
        : allProjects;

    if (filtered.length === 0) {
        listContainer.innerHTML = `
            <div style="text-align:center; padding:35px 20px; background:#f8f9fa; border-radius:12px; border:1px dashed #dadce0;">
                <span class="material-icons" style="font-size:48px; color:#9aa0a6;">inventory_2</span>
                <div style="font-weight:600; font-size:15px; color:#3c4043; margin-top:8px;">এখনও কোনো দলিল প্রজেক্ট সেভ করা নেই</div>
                <button type="button" class="action-btn" style="background:#1e8e3e; color:#fff; width:auto; margin:14px auto 0 auto; padding:8px 16px; border-radius:8px; font-size:13px;" onclick="window.openSaveCurrentCollectionModal(); window.closeSavedCollectionsModal();">
                    <span class="material-icons" style="font-size:16px; margin-right:4px;">bookmark_add</span> বর্তমান তালিকা সেভ করুন
                </button>
            </div>
        `;
        return;
    }

    listContainer.innerHTML = '';
    filtered.forEach(p => {
        const card = document.createElement('div');
        card.style.cssText = 'background:#ffffff; border:1px solid #dadce0; border-radius:10px; padding:12px 14px; margin-bottom:8px; box-shadow:0 1px 3px rgba(0,0,0,0.05);';
        const mouzaTag = p.header?.mouza ? `মৌজা: ${escapeHTML(p.header.mouza)}` : '';
        const noteTag = p.note ? `<div style="font-size:12px; color:#1e8e3e; background:#e6f4ea; display:inline-block; padding:2px 8px; border-radius:4px; margin-top:4px;">নোট: ${escapeHTML(p.note)}</div>` : '';

        card.innerHTML = `
            <div style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:8px;">
                <div>
                    <div style="font-weight:700; font-size:14.5px; color:#202124; display:flex; align-items:center; gap:6px;">
                        <span class="material-icons" style="font-size:18px; color:#1a73e8;">folder</span>
                        ${escapeHTML(p.name)}
                    </div>
                    <div style="font-size:12px; color:#5f6368; margin-top:3px; display:flex; gap:10px; flex-wrap:wrap;">
                        <span><span class="material-icons" style="font-size:13px; vertical-align:middle;">schedule</span> ${p.dateDisplay || ''}</span>
                        <span style="font-weight:600; color:#1a73e8;"><span class="material-icons" style="font-size:13px; vertical-align:middle;">description</span> ${toBanglaNum(p.recordsCount || (p.records ? p.records.length : 0))} টি দলিল</span>
                        ${mouzaTag ? `<span>${mouzaTag}</span>` : ''}
                    </div>
                    ${noteTag}
                </div>
            </div>
            <div style="display:flex; gap:6px; flex-wrap:wrap; border-top:1px dashed #e8eaed; padding-top:8px;">
                <button type="button" class="action-btn" style="background:#1a73e8; color:#fff; padding:6px 10px; font-size:12px; border-radius:6px;" onclick="window.promptLoadCollection('${p.id}')">
                    <span class="material-icons" style="font-size:14px;">file_download</span> টেবিলে আনুন
                </button>
                <button type="button" class="action-btn" style="background:#f1f3f4; color:#3c4043; padding:6px 10px; font-size:12px; border-radius:6px;" onclick="window.downloadSavedCollectionById('${p.id}')">
                    <span class="material-icons" style="font-size:14px;">download</span> ডাউনলোড
                </button>
                <button type="button" class="action-btn" style="background:#f1f3f4; color:#3c4043; padding:6px 10px; font-size:12px; border-radius:6px;" onclick="window.renameSavedCollection('${p.id}')">
                    <span class="material-icons" style="font-size:14px;">edit</span> রিনেম
                </button>
                <button type="button" class="action-btn" style="background:#fce8e6; color:#c5221f; padding:6px 10px; font-size:12px; border-radius:6px;" onclick="window.deleteSavedCollection('${p.id}')">
                    <span class="material-icons" style="font-size:14px;">delete</span> মুছুন
                </button>
            </div>
        `;
        listContainer.appendChild(card);
    });
}

export function promptLoadCollection(id) {
    const list = loadSavedProjects();
    const target = list.find(p => p.id === id);
    if (!target) {
        showToast('প্রজেক্টটি পাওয়া যায়নি!');
        return;
    }
    state.targetLoadProjectId = id;

    if (state.records.length === 0) {
        executeLoadCollection(id, 'replace');
        return;
    }

    const currCount = document.getElementById('load-strategy-current-count');
    if (currCount) currCount.innerText = toBanglaNum(state.records.length);

    const targetName = document.getElementById('load-strategy-target-name');
    if (targetName) targetName.innerText = target.name;

    const targetCount = document.getElementById('load-strategy-target-count');
    if (targetCount) targetCount.innerText = toBanglaNum(target.recordsCount || (target.records ? target.records.length : 0));

    if (dom.loadStrategyModal) dom.loadStrategyModal.style.display = 'flex';
}

export function closeLoadStrategyModal() {
    if (dom.loadStrategyModal) dom.loadStrategyModal.style.display = 'none';
    state.targetLoadProjectId = null;
}

export function confirmExecuteLoadStrategy(strategy) {
    if (!state.targetLoadProjectId) return;
    const id = state.targetLoadProjectId;
    closeLoadStrategyModal();
    executeLoadCollection(id, strategy);
}

function executeLoadCollection(id, strategy = 'replace') {
    const list = loadSavedProjects();
    const target = list.find(p => p.id === id);
    if (!target) return;

    const incoming = JSON.parse(JSON.stringify(target.records || []));
    if (incoming.length === 0) {
        showToast('এই প্রজেক্টে কোনো দলিল তথ্য নেই!');
        return;
    }

    if (strategy === 'replace') {
        state.records = incoming;
        if (target.header && typeof target.header === 'object') {
            if (dom.gTitle && target.header.main_title !== undefined) dom.gTitle.value = target.header.main_title;
            if (dom.gZila && target.header.zila !== undefined) dom.gZila.value = target.header.zila;
            if (dom.gUpazila && target.header.upazila !== undefined) dom.gUpazila.value = target.header.upazila;
            if (dom.gMouza && target.header.mouza !== undefined) dom.gMouza.value = target.header.mouza;
            if (dom.gCreator && target.header.creator !== undefined) dom.gCreator.value = target.header.creator;
            if (dom.gDate && target.header.date !== undefined) dom.gDate.value = target.header.date;
            saveGlobalHeader();
        }
    } else {
        state.records = state.records.concat(incoming);
    }

    recalculateSerialNumbers();
    saveStoredRecords(state.records);
    renderTable();
    closeSavedCollectionsModal();

    const tableSec = document.getElementById('pdf-section');
    if (tableSec) tableSec.scrollIntoView({ behavior: 'smooth' });

    showToast(`"${target.name}" সফলভাবে টেবিলে আনা হয়েছে (${toBanglaNum(incoming.length)}টি দলিল)!`);
}

export function downloadSavedCollectionById(id) {
    const list = loadSavedProjects();
    const target = list.find(p => p.id === id);
    if (target) {
        downloadProjectJSON(target);
        showToast(`"${target.name}" ডাউনলোড শুরু হয়েছে...`);
    }
}

export function renameSavedCollection(id) {
    const list = loadSavedProjects();
    const target = list.find(p => p.id === id);
    if (!target) return;

    const newName = prompt('প্রজেক্টটির নতুন নাম লিখুন:', target.name);
    if (newName && newName.trim() && newName.trim() !== target.name) {
        target.name = newName.trim();
        saveSavedProjects(list);
        filterSavedCollections();
        showToast('নাম পরিবর্তন করা হয়েছে!');
    }
}

export function deleteSavedCollection(id) {
    const list = loadSavedProjects();
    const target = list.find(p => p.id === id);
    if (!target) return;

    if (confirm(`আপনি কি নিশ্চিত যে "${target.name}" প্রজেক্টটি মুছে ফেলতে চান?`)) {
        const updated = list.filter(p => p.id !== id);
        saveSavedProjects(updated);
        filterSavedCollections();
        updateSavedProjectsBadge();
        showToast('প্রজেক্টটি মুছে ফেলা হয়েছে!');
    }
}

function updateSavedProjectsBadge() {
    const list = loadSavedProjects();
    const badge = document.getElementById('btn-saved-count-badge');
    if (badge) badge.innerText = toBanglaNum(list.length);

    const menuBadge = document.getElementById('menu-saved-count-badge');
    if (menuBadge) menuBadge.innerText = `${toBanglaNum(list.length)} টি`;

    // Render quick chips
    const quickContainer = document.getElementById('middle-quick-saved-container');
    const quickChips = document.getElementById('middle-quick-chips');
    const quickTotal = document.getElementById('quick-total-count');

    if (quickContainer && quickChips) {
        if (list.length > 0) {
            quickContainer.style.display = 'block';
            if (quickTotal) quickTotal.innerText = toBanglaNum(list.length);
            quickChips.innerHTML = '';
            list.slice(0, 4).forEach(proj => {
                const chip = document.createElement('div');
                chip.className = 'collection-chip';
                chip.title = `${proj.name} (${proj.recordsCount}টি দলিল) - টেবিলে আনতে ক্লিক করুন`;
                chip.onclick = () => promptLoadCollection(proj.id);
                chip.innerHTML = `
                    <span class="material-icons" style="font-size:15px; color:#1a73e8;">folder</span>
                    <span style="font-weight:500;">${escapeHTML(proj.name)}</span>
                    <span class="chip-count">${toBanglaNum(proj.recordsCount)}টি</span>
                    <span class="material-icons" style="font-size:14px; color:#5f6368;" title="আনুন">file_download</span>
                `;
                quickChips.appendChild(chip);
            });
        } else {
            quickContainer.style.display = 'none';
        }
    }
}

// ==========================================================================
// Backup Multi-File Import Engine
// ==========================================================================
export function openDirectFileInput() {
    if (dom.directFileInput) dom.directFileInput.click();
}

export async function handleBackupFileSelect(event) {
    const fileList = event.target.files;
    if (!fileList || fileList.length === 0) return;

    const files = Array.from(fileList);
    let allRecords = [];
    let fileSummaries = [];

    for (const file of files) {
        try {
            const content = await file.text();
            const parsed = JSON.parse(content);
            let records = [];

            if (Array.isArray(parsed)) {
                records = parsed;
            } else if (parsed && Array.isArray(parsed.dalilRecords)) {
                records = parsed.dalilRecords;
            } else if (parsed && typeof parsed === 'object' && (parsed.dalil_no || parsed.khatians)) {
                records = [parsed];
            }

            const valid = records.map(normalizeRecord).filter(Boolean);
            if (valid.length > 0) {
                allRecords.push(...valid);
                fileSummaries.push({ name: file.name, count: valid.length, valid: true });
            } else {
                fileSummaries.push({ name: file.name, count: 0, valid: false, reason: 'কোনো বৈধ দলিল নেই' });
            }
        } catch (err) {
            fileSummaries.push({ name: file.name, count: 0, valid: false, reason: 'ভুল JSON' });
        }
    }

    event.target.value = '';

    if (allRecords.length === 0) {
        showToast('নির্বাচিত ফাইলে কোনো সঠিক দলিল রেকর্ড পাওয়া যায়নি!');
        return;
    }

    state.stagedImportData = {
        records: allRecords,
        summaries: fileSummaries,
        totalFiles: files.length
    };

    showImportModal();
}

function showImportModal() {
    if (!state.stagedImportData || !dom.importModal) return;

    document.getElementById('modal-file-count').innerText = toBanglaNum(state.stagedImportData.totalFiles);
    document.getElementById('modal-record-count').innerText = toBanglaNum(state.stagedImportData.records.length);
    document.getElementById('modal-current-count').innerText = toBanglaNum(state.records.length);

    const listContainer = document.getElementById('modal-files-list');
    listContainer.innerHTML = '';
    state.stagedImportData.summaries.forEach(f => {
        const row = document.createElement('div');
        row.style.cssText = 'display:flex; justify-content:space-between; align-items:center; padding:6px 0; border-bottom:1px solid #f1f3f4; font-size:12.5px;';
        row.innerHTML = `
            <span style="overflow:hidden; text-overflow:ellipsis; white-space:nowrap; max-width:240px; display:inline-flex; align-items:center; gap:5px;">
                <span class="material-icons" style="font-size:16px; color:${f.valid ? '#1a73e8' : '#d93025'};">description</span>
                ${escapeHTML(f.name)}
            </span>
            <span style="padding:2px 8px; border-radius:12px; font-size:11px; font-weight:600; background:${f.valid ? '#e8f0fe' : '#fce8e6'}; color:${f.valid ? '#1a73e8' : '#c5221f'};">
                ${f.valid ? `${toBanglaNum(f.count)}টি দলিল` : 'অকার্যকর'}
            </span>
        `;
        listContainer.appendChild(row);
    });

    document.getElementById('strategy-merge').checked = true;
    document.getElementById('import-skip-duplicates').checked = true;
    toggleDedupCheckbox(true);

    dom.importModal.style.display = 'flex';
}

export function closeImportModal() {
    if (dom.importModal) dom.importModal.style.display = 'none';
    state.stagedImportData = null;
}

export function toggleDedupCheckbox(enable) {
    const container = document.getElementById('dedup-option-container');
    if (container) {
        container.style.opacity = enable ? '1' : '0.4';
        container.style.pointerEvents = enable ? 'auto' : 'none';
    }
}

export function confirmMultiImport() {
    if (!state.stagedImportData || !state.stagedImportData.records.length) {
        closeImportModal();
        return;
    }

    const isMerge = document.getElementById('strategy-merge').checked;
    const skipDuplicates = document.getElementById('import-skip-duplicates').checked;

    if (isMerge) {
        const result = mergeRecords(state.records, state.stagedImportData.records, skipDuplicates);
        state.records = result.merged;
        closeImportModal();
        saveStoredRecords(state.records);
        renderTable();
        if (result.duplicateCount > 0) {
            showToast(`${toBanglaNum(result.addedCount)}টি দলিল যোগ হয়েছে (${toBanglaNum(result.duplicateCount)}টি ডুপ্লিকেট বাদ দেওয়া হয়েছে)!`);
        } else {
            showToast(`সফলভাবে ${toBanglaNum(result.addedCount)}টি দলিল রেকর্ড সংরক্ষিত হয়েছে!`);
        }
    } else {
        // Replace strategy
        state.records = state.stagedImportData.records.map((r, idx) => ({ ...r, sl_no: idx + 1 }));
        closeImportModal();
        saveStoredRecords(state.records);
        renderTable();
        showToast(`সফলভাবে ${toBanglaNum(state.records.length)}টি দলিল রেকর্ড প্রতিস্থাপন করা হয়েছে!`);
    }
}

// ==========================================================================
// Update System & Android Native Bridge
// ==========================================================================
function initAppUpdateSystem() {
    const currentVer = (window.AndroidNative && window.AndroidNative.getAppVersion)
        ? window.AndroidNative.getAppVersion()
        : state.appVersion;

    if (dom.currentVerBadge) {
        dom.currentVerBadge.innerText = 'v' + toBanglaNum(currentVer);
    }

    if (state.autoUpdateEnabled) {
        setTimeout(() => {
            triggerAppUpdateCheck(false);
        }, 2000);
    }
}

export function toggleAutoUpdateSetting(enabled) {
    state.autoUpdateEnabled = enabled;
    localStorage.setItem(AUTO_UPDATE_KEY, enabled ? 'true' : 'false');
    showToast(enabled ? 'অটোমেটিক আপডেট চেক চালু করা হয়েছে!' : 'অটোমেটিক আপডেট চেক বন্ধ করা হয়েছে!');
    if (enabled) {
        triggerAppUpdateCheck(false);
    }
}

export async function triggerAppUpdateCheck(manual = false) {
    if (dom.appUpdateIcon) dom.appUpdateIcon.style.animation = 'spin 1s linear infinite';
    if (dom.appUpdateText && manual) dom.appUpdateText.innerText = 'চেক হচ্ছে...';

    // If running inside Android WebView
    if (window.AndroidNative && window.AndroidNative.checkUpdate) {
        window.AndroidNative.checkUpdate();
        setTimeout(() => {
            if (dom.appUpdateIcon) dom.appUpdateIcon.style.animation = 'none';
            if (dom.appUpdateText) dom.appUpdateText.innerText = 'আপডেট চেক';
        }, 1200);
        return;
    }

    try {
        const res = await fetch('https://api.github.com/repos/rahatshari/dalil-app-pro/releases/latest', {
            headers: { 'Accept': 'application/vnd.github.v3+json' }
        });

        if (!res.ok) {
            if (manual) showToast('আপডেট সার্ভারে সংযোগ করা যায়নি। পরে চেষ্টা করুন।');
            return;
        }

        const data = await res.json();
        const parsed = parseGitHubRelease(data, state.appVersion);

        if (parsed && parsed.hasUpdate) {
            state.latestReleaseData = parsed;
            if (dom.appUpdateBtn) dom.appUpdateBtn.classList.add('has-update');
            if (dom.appUpdateText) dom.appUpdateText.innerText = 'নতুন আপডেট!';
            openAppUpdateModal(parsed);
        } else {
            if (manual) showToast(`আপনার অ্যাপটি আপ-টু-ডেট আছে (v${toBanglaNum(state.appVersion)})!`);
            if (dom.appUpdateText) dom.appUpdateText.innerText = 'আপডেট চেক';
        }
    } catch (err) {
        if (manual) showToast('ইন্টারনেট সংযোগ চেক করুন');
    } finally {
        if (dom.appUpdateIcon) dom.appUpdateIcon.style.animation = 'none';
        if (dom.appUpdateBtn && !dom.appUpdateBtn.classList.contains('has-update') && dom.appUpdateText) {
            dom.appUpdateText.innerText = 'আপডেট চেক';
        }
    }
}

function openAppUpdateModal(info) {
    if (!dom.updateModal) return;
    const verInfo = document.getElementById('update-modal-ver-info');
    if (verInfo) {
        verInfo.innerText = `বর্তমান: v${toBanglaNum(info.currentVersion)} ➜ নতুন: v${toBanglaNum(info.latestVersion)}`;
    }

    const notes = document.getElementById('update-modal-notes');
    if (notes) {
        notes.innerText = info.releaseNotes || 'নতুন সংস্করণ ও পারফরম্যান্স আপডেট যুক্ত করা হয়েছে।';
    }

    const downloadBtn = document.getElementById('update-modal-download-btn');
    if (downloadBtn) {
        downloadBtn.onclick = function() {
            if (window.AndroidNative && window.AndroidNative.checkUpdate) {
                window.AndroidNative.checkUpdate();
            } else {
                window.open(info.downloadUrl, '_blank');
            }
            closeAppUpdateModal();
        };
    }

    dom.updateModal.style.display = 'flex';
}

export function closeAppUpdateModal() {
    if (dom.updateModal) dom.updateModal.style.display = 'none';
}

// ==========================================================================
// Offline & Service Worker
// ==========================================================================
function initOfflineDetection() {
    function updateOnlineStatus() {
        if (!navigator.onLine) {
            if (dom.offlineBadge) dom.offlineBadge.style.display = 'inline-flex';
        } else {
            if (dom.offlineBadge) dom.offlineBadge.style.display = 'none';
        }
    }
    window.addEventListener('online', updateOnlineStatus);
    window.addEventListener('offline', updateOnlineStatus);
    updateOnlineStatus();
}

function registerServiceWorker() {
    if ('serviceWorker' in navigator) {
        navigator.serviceWorker.register('./service-worker.js')
            .then(reg => console.log('SW active:', reg.scope))
            .catch(err => console.log('SW failed:', err));
    }
}

// ==========================================================================
// Event Listeners Setup
// ==========================================================================
function setupEventListeners() {
    // Close dropdown menu when clicking outside
    window.addEventListener('click', (e) => {
        if (!e.target.closest('.app-bar') && !e.target.closest('.settings-menu')) {
            if (dom.settingsMenu) dom.settingsMenu.style.display = 'none';
        }
    });

    // Close modals on ESC key
    window.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            closeSaveCollectionModal();
            closeSavedCollectionsModal();
            closeLoadStrategyModal();
            closeImportModal();
            closeAppUpdateModal();
        }
    });

    // Hook window.print for native bridge
    const originalPrint = window.print;
    window.print = function() {
        if (window.AndroidNative && window.AndroidNative.printPage) {
            window.AndroidNative.printPage();
        } else {
            originalPrint.call(window);
        }
    };
}

// Global Exports on Window for Inline Handlers Compatibility
window.removeRepeaterRow = function(btn) {
    const box = btn.closest('.repeater-box');
    if (box) {
        box.remove();
        scheduleSaveDraft();
    }
};

export function exportData() {
    if (state.records.length === 0) {
        showToast('ব্যাকআপ নেওয়ার মতো কোনো তথ্য নেই!');
        return;
    }
    const a = document.createElement('a');
    a.href = URL.createObjectURL(new Blob([JSON.stringify(state.records)], { type: 'application/json' }));
    a.download = `Dalil_Backup_${new Date().toISOString().slice(0, 10)}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    if (dom.settingsMenu) dom.settingsMenu.style.display = 'none';
    showToast('ব্যাকআপ ডাউনলোড শুরু হয়েছে!');
}

export function openMultiFileInput() {
    if (dom.settingsMenu) dom.settingsMenu.style.display = 'none';
    const input = document.getElementById('importFile');
    if (input) input.click();
}

export function importData(event) {
    handleBackupFileSelect(event);
}

// Global Exports on Window for 100% Inline Handlers Compatibility
window.removeRepeaterRow = function(btn) {
    const box = btn.closest('.repeater-box');
    if (box) {
        box.remove();
        scheduleSaveDraft();
    }
};

window.exportData = exportData;
window.openMultiFileInput = openMultiFileInput;
window.importData = importData;
window.editDeedRecord = editRecord;
window.deleteDeedRecord = deleteRecord;
window.editRecord = editRecord;
window.deleteRecord = deleteRecord;
window.addRecord = addRecord;
window.updateRecord = updateRecord;
window.cancelEdit = cancelEdit;
window.clearAllRecords = clearAllRecords;
window.addReceiverField = addReceiverField;
window.addDonorField = addDonorField;
window.addKhatianField = addKhatianField;
window.setTodayRecordDate = setTodayRecordDate;
window.setTodayHeaderDate = setTodayHeaderDate;
window.toggleSettings = toggleSettings;
window.toggleMotherSettings = toggleMotherSettings;
window.toggleAutoUpdateSetting = toggleAutoUpdateSetting;
window.changeOrientation = changeOrientation;
window.toggleHeaderContent = toggleHeaderContent;
window.saveGlobalHeader = saveGlobalHeader;
window.clearGlobalHeader = clearGlobalHeader;
window.openSaveCurrentCollectionModal = openSaveCurrentCollectionModal;
window.closeSaveCollectionModal = closeSaveCollectionModal;
window.saveCurrentCollection = saveCurrentCollection;
window.openSavedCollectionsModal = openSavedCollectionsModal;
window.closeSavedCollectionsModal = closeSavedCollectionsModal;
window.filterSavedCollections = filterSavedCollections;
window.promptLoadCollection = promptLoadCollection;
window.closeLoadStrategyModal = closeLoadStrategyModal;
window.confirmExecuteLoadStrategy = confirmExecuteLoadStrategy;
window.downloadSavedCollectionById = downloadSavedCollectionById;
window.renameSavedCollection = renameSavedCollection;
window.deleteSavedCollection = deleteSavedCollection;
window.openDirectFileInput = openDirectFileInput;
window.handleBackupFileSelect = handleBackupFileSelect;
window.closeImportModal = closeImportModal;
window.confirmMultiImport = confirmMultiImport;
window.toggleDedupCheckbox = toggleDedupCheckbox;
window.triggerAppUpdateCheck = triggerAppUpdateCheck;
window.closeAppUpdateModal = closeAppUpdateModal;

window.DalilApp = {
    initApp,
    toggleSettings,
    toggleMotherSettings,
    toggleAutoUpdateSetting,
    changeOrientation,
    toggleHeaderContent,
    setTodayHeaderDate,
    saveGlobalHeader,
    clearGlobalHeader,
    setTodayRecordDate,
    addReceiverField,
    addDonorField,
    addKhatianField,
    addRecord,
    editRecord,
    updateRecord,
    cancelEdit,
    deleteRecord,
    clearAllRecords,
    exportData,
    openMultiFileInput,
    importData,
    openSaveCurrentCollectionModal,
    closeSaveCollectionModal,
    saveCurrentCollection,
    openSavedCollectionsModal,
    closeSavedCollectionsModal,
    filterSavedCollections,
    promptLoadCollection,
    closeLoadStrategyModal,
    confirmExecuteLoadStrategy,
    downloadSavedCollectionById,
    renameSavedCollection,
    deleteSavedCollection,
    openDirectFileInput,
    handleBackupFileSelect,
    closeImportModal,
    toggleDedupCheckbox,
    confirmMultiImport,
    triggerAppUpdateCheck,
    closeAppUpdateModal
};

// Auto-boot on load
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initApp);
} else {
    initApp();
}
