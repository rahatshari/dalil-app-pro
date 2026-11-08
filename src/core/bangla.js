/**
 * Bangla Numerals and Date Utilities
 */
export const EN_TO_BN_DIGITS = {
    '0': '০', '1': '১', '2': '২', '3': '৩', '4': '৪',
    '5': '৫', '6': '৬', '7': '৭', '8': '৮', '9': '৯'
};

export const BN_TO_EN_DIGITS = {
    '০': '0', '১': '1', '২': '2', '৩': '3', '৪': '4',
    '৫': '5', '৬': '6', '৭': '7', '৮': '8', '৯': '9'
};

export function toBanglaNum(str) {
    if (str === null || str === undefined) return '';
    return str.toString().replace(/[0-9]/g, match => EN_TO_BN_DIGITS[match] || match);
}

export function toEnglishNum(str) {
    if (str === null || str === undefined) return '';
    return str.toString().replace(/[০-৯]/g, match => BN_TO_EN_DIGITS[match] || match);
}

/**
 * Automatically format numeric strings into dd/mm/yyyy format with slashes
 * Supports both English and Bangla numbers
 */
export function formatAutoDate(val) {
    if (!val) return '';
    const bn = toBanglaNum(val.toString());
    const digits = bn.replace(/[^\d০-৯]/g, '');
    const cleanDigits = digits.length > 8 ? digits.slice(0, 8) : digits;
    
    if (!cleanDigits) return '';
    if (cleanDigits.length < 2) return cleanDigits;
    if (cleanDigits.length === 2) return cleanDigits + '/';
    if (cleanDigits.length < 4) return cleanDigits.slice(0, 2) + '/' + cleanDigits.slice(2);
    if (cleanDigits.length === 4) return cleanDigits.slice(0, 2) + '/' + cleanDigits.slice(2, 4) + '/';
    return cleanDigits.slice(0, 2) + '/' + cleanDigits.slice(2, 4) + '/' + cleanDigits.slice(4, 8);
}

/**
 * Returns today's date formatted in Bangla numerals (dd/mm/yyyy)
 */
export function getTodayBanglaDate() {
    const today = new Date();
    const dd = String(today.getDate()).padStart(2, '0');
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const yyyy = today.getFullYear();
    return toBanglaNum(`${dd}/${mm}/${yyyy}`);
}
