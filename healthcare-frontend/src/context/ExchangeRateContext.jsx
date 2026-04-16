import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { fetchUsdToLkrRate, getFallbackUsdToLkrRate, clearUsdLkrRateCache } from '../utils/exchangeRate';

const ExchangeRateContext = createContext(null);

export function ExchangeRateProvider({ children }) {
  const [usdToLkrRate, setUsdToLkrRate] = useState(getFallbackUsdToLkrRate);
  const [loading, setLoading] = useState(true);
  const [rateSource, setRateSource] = useState('fallback'); // 'api' | 'cache' | 'fallback'

  const refresh = useCallback(async (force = false) => {
    setLoading(true);
    if (force) {
      clearUsdLkrRateCache();
    }
    try {
      const { rate, source } = await fetchUsdToLkrRate();
      setUsdToLkrRate(rate);
      setRateSource(source);
    } catch (e) {
      setUsdToLkrRate(getFallbackUsdToLkrRate());
      setRateSource('fallback');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  const value = useMemo(
    () => ({
      usdToLkrRate,
      loading,
      rateSource,
      refresh,
      usingFallback: rateSource === 'fallback',
    }),
    [usdToLkrRate, loading, rateSource, refresh]
  );

  return <ExchangeRateContext.Provider value={value}>{children}</ExchangeRateContext.Provider>;
}

export function useExchangeRate() {
  const ctx = useContext(ExchangeRateContext);
  if (!ctx) {
    throw new Error('useExchangeRate must be used within ExchangeRateProvider');
  }
  return ctx;
}
