/**
 * Fetches USD → LKR rate from public APIs (no backend change).
 * Primary: open.er-api.com (includes LKR; CORS-friendly for browser).
 * Backup: Fawaz Ahmed currency CDN (jsDelivr).
 */

const FALLBACK = Number(process.env.REACT_APP_USD_TO_LKR || 320);
const CACHE_KEY = 'healthcare_usd_lkr_rate';
const CACHE_TS_KEY = 'healthcare_usd_lkr_rate_ts';
const TTL_MS = Number(process.env.REACT_APP_EXCHANGE_RATE_TTL_MS || 6 * 60 * 60 * 1000); // 6h default

function readCache() {
  try {
    const raw = sessionStorage.getItem(CACHE_KEY);
    const ts = sessionStorage.getItem(CACHE_TS_KEY);
    if (!raw || !ts) return null;
    const age = Date.now() - Number(ts);
    if (age > TTL_MS || age < 0) return null;
    const n = Number(raw);
    return Number.isFinite(n) && n > 0 ? n : null;
  } catch {
    return null;
  }
}

function writeCache(rate) {
  try {
    sessionStorage.setItem(CACHE_KEY, String(rate));
    sessionStorage.setItem(CACHE_TS_KEY, String(Date.now()));
  } catch {
    /* ignore */
  }
}

async function fetchOpenErApi() {
  const res = await fetch('https://open.er-api.com/v6/latest/USD');
  if (!res.ok) throw new Error('open.er-api failed');
  const data = await res.json();
  const lkr = data?.rates?.LKR;
  if (typeof lkr !== 'number' || !Number.isFinite(lkr) || lkr <= 0) throw new Error('Invalid LKR rate');
  return lkr;
}

/** Backup: CDN JSON, usd.lkr = LKR per 1 USD */
async function fetchFawazBackup() {
  const res = await fetch(
    'https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/usd.json'
  );
  if (!res.ok) throw new Error('Backup API failed');
  const data = await res.json();
  const lkr = data?.usd?.lkr;
  if (typeof lkr !== 'number' || !Number.isFinite(lkr) || lkr <= 0) throw new Error('Invalid backup rate');
  return lkr;
}

/**
 * Returns USD→LKR multiplier (how many LKR per 1 USD) and where it came from.
 */
export async function fetchUsdToLkrRate() {
  const cached = readCache();
  if (cached != null) {
    return { rate: cached, source: 'cache' };
  }

  try {
    const rate = await fetchOpenErApi();
    writeCache(rate);
    return { rate, source: 'api' };
  } catch {
    try {
      const rate = await fetchFawazBackup();
      writeCache(rate);
      return { rate, source: 'api' };
    } catch {
      return { rate: FALLBACK, source: 'fallback' };
    }
  }
}

export function getFallbackUsdToLkrRate() {
  return FALLBACK;
}

/** Call before manual refresh to force a new network request */
export function clearUsdLkrRateCache() {
  try {
    sessionStorage.removeItem(CACHE_KEY);
    sessionStorage.removeItem(CACHE_TS_KEY);
  } catch {
    /* ignore */
  }
}

export { FALLBACK as FALLBACK_USD_TO_LKR_RATE };
