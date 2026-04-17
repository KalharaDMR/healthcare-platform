import React, { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import { clinicalApi } from '../../api/clinicalApi';

export default function PatientRecordsPage() {
  const [reports, setReports] = useState([]);
  const [prescriptions, setPrescriptions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [uploading, setUploading] = useState(false);

  const loadData = async () => {
    setLoading(true);
    try {
      const [reportsRes, prescriptionsRes] = await Promise.all([
        clinicalApi.getMyReports(),
        clinicalApi.getMyPrescriptions(),
      ]);
      setReports(reportsRes.data || []);
      setPrescriptions(prescriptionsRes.data || []);
    } catch {
      toast.error('Unable to load medical records');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleUpload = async (event) => {
    const file = event.target.files?.[0];
    if (!file) return;
    setUploading(true);
    try {
      await clinicalApi.uploadMyReport(file);
      toast.success('Report uploaded');
      loadData();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Upload failed');
    } finally {
      setUploading(false);
      event.target.value = '';
    }
  };

  const openReport = async (report) => {
    try {
      const res = await clinicalApi.getMyReportContent(report.id);
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
        <h1 style={{ fontSize: '1.65rem', fontWeight: 700 }}>Medical records</h1>
        <p style={{ color: 'var(--text-secondary)', marginTop: 6 }}>
          Upload reports and view doctor-issued prescriptions and medical history.
        </p>
      </div>

      <Card title="Upload new report" action={<Button variant="ghost" onClick={loadData} loading={loading}>Refresh</Button>}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10, flexWrap: 'wrap' }}>
          <input type="file" onChange={handleUpload} disabled={uploading} />
          {uploading ? <span style={{ color: 'var(--text-secondary)' }}>Uploading...</span> : null}
        </div>
      </Card>

      <Card title="Uploaded reports">
        {reports.length === 0 ? (
          <p style={{ margin: 0, color: 'var(--text-secondary)' }}>No reports uploaded yet.</p>
        ) : (
          <div style={{ display: 'grid', gap: 8 }}>
            {reports.map((r) => (
              <div key={r.id} style={{ border: '1px solid var(--border)', borderRadius: 8, padding: 10 }}>
                <div style={{ fontWeight: 600 }}>{r.fileName}</div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{r.filePath}</div>
                <div style={{ marginTop: 8 }}>
                  <Button variant="ghost" onClick={() => openReport(r)}>Open</Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </Card>

      <Card title="Past prescriptions">
        {prescriptions.length === 0 ? (
          <p style={{ margin: 0, color: 'var(--text-secondary)' }}>No prescriptions yet.</p>
        ) : (
          <div style={{ display: 'grid', gap: 8 }}>
            {prescriptions.map((p) => (
              <div key={p.id} style={{ border: '1px solid var(--border)', borderRadius: 8, padding: 10 }}>
                <div style={{ fontWeight: 600 }}>{p.medications}</div>
                {p.notes ? <div style={{ marginTop: 4, fontSize: '0.875rem' }}>{p.notes}</div> : null}
                <div style={{ marginTop: 4, fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                  Issued by doctor: {p.doctorId}
                </div>
              </div>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
}
