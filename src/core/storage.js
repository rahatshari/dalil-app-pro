/**
 * LocalStorage & Draft Persistence Engine
 */
import { toBanglaNum } from './bangla.js';

export const STORAGE_KEY = 'myDalilDataFinal';
export const HEADER_KEY = 'myDalilHeaderFinal';
export const DRAFT_KEY = 'myDalilDraftData';
export const SAVED_PROJECTS_KEY = 'mySavedDeedProjects';
export const MOTHER_PREF_KEY = 'motherNamePref';
export const AUTO_UPDATE_KEY = 'autoUpdateCheckEnabled';
export const ORIENTATION_KEY = 'printOrientation';

export const DEFAULT_HEADER = {
    title: 'দলিলে জমির হিসাব',
    zila: 'বাগেরহাট',
    upazila: 'মোড়েলগঞ্জ',
    mouza: '৯৩ পাঠামারা',
    creator: 'মোঃ আব্দুল্লাহ আল রাহাত'
};

export function loadStoredRecords() {
    try {
        const raw = localStorage.getItem(STORAGE_KEY);
        return raw ? JSON.parse(raw) : [];
    } catch (e) {
        console.error('Failed to load records from storage', e);
        return [];
    }
}

export function saveStoredRecords(records) {
    try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(records));
        return true;
    } catch (e) {
        console.error('Failed to save records to storage', e);
        return false;
    }
}

export function loadSavedProjects() {
    try {
        const raw = localStorage.getItem(SAVED_PROJECTS_KEY);
        return raw ? JSON.parse(raw) : [];
    } catch (e) {
        console.error('Failed to load saved projects', e);
        return [];
    }
}

export function saveSavedProjects(projects) {
    try {
        localStorage.setItem(SAVED_PROJECTS_KEY, JSON.stringify(projects));
        return true;
    } catch (e) {
        console.error('Failed to save deed projects', e);
        return false;
    }
}

export function loadStoredHeader() {
    try {
        const raw = localStorage.getItem(HEADER_KEY);
        return raw ? JSON.parse(raw) : {};
    } catch (e) {
        return {};
    }
}

export function saveStoredHeader(headerData) {
    try {
        localStorage.setItem(HEADER_KEY, JSON.stringify(headerData));
        return true;
    } catch (e) {
        return false;
    }
}

export function getResolvedHeader() {
    const saved = loadStoredHeader();
    const today = new Date();
    const dd = String(today.getDate()).padStart(2, '0');
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const yyyy = today.getFullYear();
    const defaultDate = toBanglaNum(`${dd}/${mm}/${yyyy}`);

    return {
        title: saved.title || DEFAULT_HEADER.title,
        zila: saved.zila || DEFAULT_HEADER.zila,
        upazila: saved.upazila || DEFAULT_HEADER.upazila,
        mouza: saved.mouza || DEFAULT_HEADER.mouza,
        creator: saved.creator || DEFAULT_HEADER.creator,
        date: saved.date || defaultDate,
        rawSaved: saved
    };
}
