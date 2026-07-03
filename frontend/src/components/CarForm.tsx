import { useState } from 'react';
import axios from 'axios';
import { createCar } from '../api/cars';
import type { ApiError } from '../types/apiError';

interface CarFormProps {
  onCarCreated: () => void;
}

export default function CarForm({ onCarCreated }: CarFormProps) {
  const [licensePlate, setLicensePlate] = useState('');
  const [model, setModel] = useState('');
  const [brand, setBrand] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);

    try {
      await createCar({ licensePlate, model, brand });
      setLicensePlate('');
      setModel('');
      setBrand('');
      onCarCreated();
    } catch (err) {
      if (axios.isAxiosError<ApiError>(err) && err.response) {
        setError(err.response.data.error ?? 'Failed to create car.');
      } else {
        setError('Failed to create car. Please try again.');
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <h3>Add a new car</h3>

      {error && <p style={{ color: 'red' }}>{error}</p>}

      <div>
        <label>
          License plate
          <input
            value={licensePlate}
            onChange={(e) => setLicensePlate(e.target.value.toUpperCase())}
            placeholder="34ABC123"
            required
          />
        </label>
      </div>

      <div>
        <label>
          Brand
          <input value={brand} onChange={(e) => setBrand(e.target.value)} required />
        </label>
      </div>

      <div>
        <label>
          Model
          <input value={model} onChange={(e) => setModel(e.target.value)} required />
        </label>
      </div>

      <button type="submit" disabled={submitting}>
        {submitting ? 'Adding...' : 'Add car'}
      </button>
    </form>
  );
}