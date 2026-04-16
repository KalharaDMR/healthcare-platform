import React, { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { useSearchParams } from 'react-router-dom';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Input from '../../components/common/Input';
import { clinicalApi } from '../../api/clinicalApi';

export default function DoctorClinicalPage() {
  const [searchParams] = useSearchParams();
  const [patientUsername, setPatientUsername] = useState('');
  const [history, setHistory] = useState(null);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [savingPrescription, setSavingPrescription] = useState(false);
  const [form, setForm] = useState({ medications: '', notes: '' });

  useEffect(() => {
    const patient = searchParams.get('patient');
    if (patient) {
      setPatientUsername(patient);
    }
  }, [searchParams]);

  const loadHistory = async () => {
    if (!patientUsername.trim()) {
      toast.error('Enter patient username');
      return;
    }
    setLoadingHistory(true);
    try {
      const res = await clinicalApi.getPatientHistoryByUsername(patientUsername.trim());
      setHistory(res.data);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Unable to load patient history');
      setHistory(null);
    } finally {
      setLoadingHistory(false);
    }
  };

  const issuePrescription = async () => {
    if (!patientUsername.trim() || !form.medications.trim()) {
      toast.error('Patient username and medications are required');
      return;
    }
    setSavingPrescription(true);
    try {
      await clinicalApi.issuePrescription({
        patientUsername: patientUsername.trim(),
        medications: form.medications.trim(),
        notes: form.notes?.trim() || '',
      });
      toast.success('Prescription issued');
      setForm({ medications: '', notes: '' });
      loadHistory();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to issue prescription');
    } finally {
      setSavingPrescription(false);
    }
  };

  const openPatientReport = async (report) => {
    try {
      const res = await clinicalApi.getPatientReportContent(report.id);
      const contentType = res.headers?.['content-type'] || 'application/octet-stream';
      const blob = new Blob([res.data], { type: contentType });
      const url = window.URL.createObjectURL(blob);
      window.open(url, '_blank', 'noopener,noreferrer');
      setTimeout(() => window.URL.revokeObjectURL(url), 60_000);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Unable to open report');
    }
  };

  return (
    <div style={{ maxWidth: 980, animation: 'fadeIn 0.3s ease', display: 'grid', gap: 18 }}>
      <div>
        <h1 style={{ fontSize: '1.65rem', fontWeight: 700 }}>Clinical workspace</h1>
        <p style={{ color: 'var(--text-secondary)', marginTop: 6 }}>
          View patient reports/history and issue digital prescriptions.
        </p>
      </div>

      <Card title="Find patient">
        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
          <Input
            label="Patient username"
            value={patientUsername}
            onChange={(e) => setPatientUsername(e.target.value)}
            placeholder="e.g. patient123"
          />
          <div style={{ alignSelf: 'end' }}>
            <Button onClick={loadHistory} loading={loadingHistory}>Load history</Button>
          </div>
        </div>
      </Card>

      <Card title="Issue digital prescription">
        <div style={{ display: 'grid', gap: 10 }}>
          <Input
            label="Medications"
            value={form.medications}
            onChange={(e) => setForm((prev) => ({ ...prev, medications: e.target.value }))}
            placeholder="Medicine name, dosage, frequency"
          />
          <Input
            label="Notes"
            value={form.notes}
            onChange={(e) => setForm((prev) => ({ ...prev, notes: e.target.value }))}
            placeholder="Instructions for patient"
          />
          <div>
            <Button onClick={issuePrescription} loading={savingPrescription}>
              Issue prescription
            </Button>
          </div>
        </div>
      </Card>

      <Card title="Patient medical history">
        {!history ? (
          <p style={{ margin: 0, color: 'var(--text-secondary)' }}>Load a patient to view reports and prescriptions.</p>
        ) : (
          <div style={{ display: 'grid', gap: 16 }}>
            <div>
              <h3 style={{ fontSize: '1rem', marginBottom: 8 }}>Uploaded reports</h3>
              {history.reports?.length ? (
                <div style={{ display: 'grid', gap: 8 }}>
                  {history.reports.map((r) => (
                    <div key={r.id} style={{ border: '1px solid var(--border)', borderRadius: 8, padding: 10 }}>
                      <div style={{ fontWeight: 600 }}>{r.fileName}</div>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{r.filePath}</div>
                      <div style={{ marginTop: 8 }}>
                        <Button variant="ghost" onClick={() => openPatientReport(r)}>Open</Button>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p style={{ margin: 0, color: 'var(--text-secondary)' }}>No reports uploaded yet.</p>
              )}
            </div>

            <div>
              <h3 style={{ fontSize: '1rem', marginBottom: 8 }}>Past prescriptions</h3>
              {history.prescriptions?.length ? (
                <div style={{ display: 'grid', gap: 8 }}>
                  {history.prescriptions.map((p) => (
                    <div key={p.id} style={{ border: '1px solid var(--border)', borderRadius: 8, padding: 10 }}>
                      <div style={{ fontWeight: 600 }}>{p.medications}</div>
                      {p.notes ? <div style={{ fontSize: '0.875rem', marginTop: 4 }}>{p.notes}</div> : null}
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: 4 }}>
                        Issued by: {p.doctorId}
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p style={{ margin: 0, color: 'var(--text-secondary)' }}>No prescriptions found.</p>
              )}
            </div>
          </div>
        )}
      </Card>
    </div>
  );
}
