import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronRight, Stethoscope } from 'lucide-react';
import toast from 'react-hot-toast';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Input from '../../components/common/Input';
import Badge from '../../components/common/Badge';
import PaginationBar from '../../components/common/PaginationBar';
import { doctorApi } from '../../api/doctorApi';
import { adminApi } from '../../api/adminApi';

const PAGE_SIZE = 8;

export default function PatientDoctorsBrowse() {
  const navigate = useNavigate();
  const [filters, setFilters] = useState({ name: '', specialization: '' });
  const [specializationOptions, setSpecializationOptions] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);

  const loadDoctorsWithFilters = useCallback(async (f) => {
    setLoading(true);
    try {
      const params = {};
      Object.entries(f).forEach(([key, value]) => {
        if (value) params[key] = value;
      });
      const res = await doctorApi.searchDoctors(params);
      setDoctors(res.data || []);
      setPage(1);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to load doctors');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const res = await adminApi.getSpecializations();
        if (!cancelled) setSpecializationOptions(Array.isArray(res.data) ? res.data : []);
      } catch {
        if (!cancelled) setSpecializationOptions([]);
      }
      await loadDoctorsWithFilters({ name: '', specialization: '' });
    })();
    return () => { cancelled = true; };
  }, [loadDoctorsWithFilters]);

  const totalPages = Math.max(1, Math.ceil(doctors.length / PAGE_SIZE));
  const pageDoctors = useMemo(() => {
    const start = (page - 1) * PAGE_SIZE;
    return doctors.slice(start, start + PAGE_SIZE);
  }, [doctors, page]);

  const goToDoctor = (doc) => {
    navigate(`/patient/doctors/${encodeURIComponent(doc.username)}/slots`, {
      state: {
        doctorName: doc.doctorName || doc.username,
        specialization: doc.specialization,
      },
    });
  };

  return (
    <div style={{ maxWidth: 1100, animation: 'fadeIn 0.3s ease' }}>
      <div style={{ marginBottom: 24 }}>
        <h1 style={{ fontSize: '1.65rem', fontWeight: 700 }}>Browse doctors</h1>
        <p style={{ color: 'var(--text-secondary)', marginTop: 6, maxWidth: 640 }}>
          Search verified doctors by name and/or specialization. Open a doctor to see available slots, times, and book—including date filters on the next step.
        </p>
      </div>

      <Card
        title="Search doctors"
        subtitle="Use doctor name, specialization, or both. Leave fields empty to list everyone."
      >
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: 12 }}>
          <Input
            label="Doctor name"
            value={filters.name}
            onChange={(e) => setFilters((prev) => ({ ...prev, name: e.target.value }))}
            placeholder="e.g. Silva or partial name"
          />
          <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
            <label style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>Specialization</label>
            <select
              value={filters.specialization}
              onChange={(e) => setFilters((prev) => ({ ...prev, specialization: e.target.value }))}
              style={{ padding: '11px 14px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', background: 'var(--bg-card)' }}
            >
              <option value="">Any specialization</option>
              {specializationOptions.map((s) => (
                <option key={s} value={s}>{s}</option>
              ))}
            </select>
          </div>
        </div>
        <div style={{ marginTop: 14, display: 'flex', flexWrap: 'wrap', gap: 10 }}>
          <Button onClick={() => loadDoctorsWithFilters(filters)} loading={loading}>Search</Button>
          <Button
            variant="ghost"
            onClick={() => {
              const empty = { name: '', specialization: '' };
              setFilters(empty);
              loadDoctorsWithFilters(empty);
            }}
          >
            Clear
          </Button>
        </div>
      </Card>

      <div style={{ marginTop: 22 }}>
        <h2 style={{ fontSize: '1.125rem', fontWeight: 700, marginBottom: 14 }}>Doctors</h2>
        {loading ? (
          <p style={{ color: 'var(--text-secondary)' }}>Loading doctors…</p>
        ) : doctors.length === 0 ? (
          <div
            style={{
              border: '1px dashed var(--border)',
              borderRadius: 'var(--radius-md)',
              padding: 40,
              textAlign: 'center',
              color: 'var(--text-secondary)',
            }}
          >
            No doctors match your filters. Try clearing filters or check back later.
          </div>
        ) : (
          <>
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))',
                gap: 16,
              }}
            >
              {pageDoctors.map((doc) => (
                <button
                  key={doc.username}
                  type="button"
                  onClick={() => goToDoctor(doc)}
                  style={{
                    textAlign: 'left',
                    border: '1px solid var(--border)',
                    borderRadius: 'var(--radius-md)',
                    padding: 18,
                    background: 'var(--bg-card)',
                    cursor: 'pointer',
                    transition: 'box-shadow 0.2s, transform 0.2s, border-color 0.2s',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: 10,
                    minHeight: 160,
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.boxShadow = 'var(--shadow-md)';
                    e.currentTarget.style.transform = 'translateY(-2px)';
                    e.currentTarget.style.borderColor = 'var(--primary)';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.boxShadow = '';
                    e.currentTarget.style.transform = '';
                    e.currentTarget.style.borderColor = 'var(--border)';
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 8 }}>
                    <div style={{
                      width: 44,
                      height: 44,
                      borderRadius: 12,
                      background: 'rgba(10,110,97,0.12)',
                      color: 'var(--primary)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontWeight: 700,
                      fontSize: '1.1rem',
                      flexShrink: 0,
                    }}
                    >
                      {(doc.doctorName || doc.username || '?')[0].toUpperCase()}
                    </div>
                    {(doc.available ?? doc.isAvailable) ? (
                      <Badge label="Slots open" />
                    ) : (
                      <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>No open slots</span>
                    )}
                  </div>
                  <div>
                    <div style={{ fontWeight: 700, fontSize: '1.05rem' }}>{doc.doctorName || doc.username}</div>
                    <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginTop: 4, display: 'flex', alignItems: 'center', gap: 6 }}>
                      <Stethoscope size={14} style={{ flexShrink: 0 }} />
                      {doc.specialization || '—'}
                    </div>
                  </div>
                  <div style={{ marginTop: 'auto', display: 'flex', alignItems: 'center', justifyContent: 'space-between', paddingTop: 8, borderTop: '1px solid var(--border)' }}>
                    <span style={{ fontSize: '0.8125rem', color: 'var(--primary)', fontWeight: 600 }}>View availability</span>
                    <ChevronRight size={18} color="var(--primary)" />
                  </div>
                </button>
              ))}
            </div>
            <PaginationBar
              page={page}
              totalPages={totalPages}
              totalItems={doctors.length}
              pageSize={PAGE_SIZE}
              onPageChange={setPage}
              itemLabel="doctors"
            />
          </>
        )}
      </div>
    </div>
  );
}
