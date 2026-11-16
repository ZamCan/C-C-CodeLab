import {ApiError} from './policy.mjs';
// Three explicit official-name components; Unicode letters, marks, apostrophes and hyphens.
export function officialName(value) {
 if (!Array.isArray(value) || value.length !== 3) throw new ApiError(400,'three_official_names_required');
 const parts=value.map(p=>typeof p==='string'?p.normalize('NFC').trim():'');
 if(parts.some(p=>p.length>60 || !/^[\p{L}][\p{L}\p{M}’' -]*$/u.test(p) || p.includes('  '))) throw new ApiError(400,'invalid_official_name');
 return parts.join(' ');
}
