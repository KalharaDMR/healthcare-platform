import { getFallbackUsdToLkrRate } from './exchangeRate';

/**
 * Backend stores amounts in USD. Frontend shows LKR using usdToLkrRate (live API or fallback).
 * Pass `usdToLkrRate` from useExchangeRate() everywhere for consistency.
 */
const roundToCents = (value) => Math.round((Number(value) + Number.EPSILON) * 100) / 100;

export const lkrToUsd = (lkrAmount, usdToLkrRate = getFallbackUsdToLkrRate()) => {
  if (lkrAmount === '' || lkrAmount === null || lkrAmount === undefined) return 0;
  const r = Number(usdToLkrRate) || getFallbackUsdToLkrRate();
  return roundToCents(Number(lkrAmount) / r);
};

/** Convert USD (from API) → LKR for display */
export const usdToLkr = (usdAmount, usdToLkrRate = getFallbackUsdToLkrRate()) => {
  if (usdAmount === '' || usdAmount === null || usdAmount === undefined) return 0;
  const r = Number(usdToLkrRate) || getFallbackUsdToLkrRate();
  return roundToCents(Number(usdAmount) * r);
};

export const formatLkr = (amount) =>
  new Intl.NumberFormat('en-LK', {
    style: 'currency',
    currency: 'LKR',
    minimumFractionDigits: 2,
  }).format(Number(amount || 0));

export const formatUsd = (amount) =>
  new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
  }).format(Number(amount || 0));

/**
 * Display rule: show LKR first (converted from backend USD), then USD as reference.
 * @param {number|string} usdAmount - value from backend (USD)
 * @param {number} [usdToLkrRate] - from useExchangeRate().usdToLkrRate
 */
export const formatMoneyFromUsd = (usdAmount, usdToLkrRate = getFallbackUsdToLkrRate()) => {
  const usd = Number(usdAmount || 0);
  const lkr = usdToLkr(usd, usdToLkrRate);
  return {
    lkr: formatLkr(lkr),
    usd: formatUsd(usd),
    lkrAmount: lkr,
    usdAmount: usd,
  };
};

/** Single line: "LKR … · ref USD …" */
export const formatMoneyLineFromUsd = (usdAmount, usdToLkrRate = getFallbackUsdToLkrRate()) => {
  const { lkr, usd } = formatMoneyFromUsd(usdAmount, usdToLkrRate);
  return `${lkr} · ref ${usd}`;
};

export const currencyMeta = {
  get usdToLkrRate() {
    return getFallbackUsdToLkrRate();
  },
};
