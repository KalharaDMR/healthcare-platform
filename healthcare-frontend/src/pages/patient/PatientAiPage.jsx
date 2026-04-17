import React, { useState } from 'react';
import { Sparkles } from 'lucide-react';
import toast from 'react-hot-toast';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Input from '../../components/common/Input';
import Badge from '../../components/common/Badge';
import { aiApi } from '../../api/aiApi';

export default function PatientAiPage() {
  const [symptoms, setSymptoms] = useState('');
  const [age, setAge] = useState('');
  const [gender, setGender] = useState('male');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);

  const handleAnalyze = async () => {
    const symptomList = symptoms.split(',').map((s) => s.trim()).filter(Boolean);
    if (!age || !symptomList.length) {
      toast.error('Provide age and at least one symptom');
      return;
    }
    setLoading(true);
    try {
      const res = await aiApi.analyzeSymptoms({ age: Number(age), gender, symptoms: symptomList });
      setResult(res.data);
    } catch {
      toast.error('AI analysis failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: 720, animation: 'fadeIn 0.3s ease' }}>
      <div style={{ marginBottom: 24 }}>
        <h1 style={{ fontSize: '1.65rem', fontWeight: 700 }}>AI symptom assistant</h1>
        <p style={{ color: 'var(--text-secondary)', marginTop: 6 }}>
          Educational guidance only—not a diagnosis. Always consult a clinician for medical decisions.
        </p>
      </div>

      <Card title="Symptom check" subtitle="Comma-separated symptoms work best">
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: 12 }}>
          <Input label="Age" type="number" value={age} onChange={(e) => setAge(e.target.value)} />
          <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
            <label style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>Gender</label>
            <select
              value={gender}
              onChange={(e) => setGender(e.target.value)}
              style={{ padding: '11px 14px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)' }}
            >
              <option value="male">Male</option>
              <option value="female">Female</option>
              <option value="other">Other</option>
            </select>
          </div>
          <Input
            label="Symptoms (comma separated)"
            value={symptoms}
            onChange={(e) => setSymptoms(e.target.value)}
            placeholder="e.g. fever, cough"
          />
        </div>
        <div style={{ marginTop: 14 }}>
          <Button onClick={handleAnalyze} loading={loading} icon={<Sparkles size={15} />}>
            Analyze symptoms
          </Button>
        </div>
        {result && (
          <div style={{ marginTop: 20, border: '1px solid var(--border)', borderRadius: 10, padding: 16 }}>
            <div style={{ fontWeight: 600 }}>General advice</div>
            <p style={{ color: 'var(--text-secondary)', marginTop: 6 }}>{result.generalAdvice || 'No advice'}</p>
            <div style={{ marginTop: 14, fontWeight: 600 }}>Recommended specializations</div>
            <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', marginTop: 8 }}>
              {(result.recommendedSpecializations || []).map((r, idx) => (
                <Badge key={`${r.specialization}-${idx}`} label={r.specialization || 'General'} />
              ))}
            </div>
          </div>
        )}
      </Card>
    </div>
  );
}
