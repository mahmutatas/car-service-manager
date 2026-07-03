import { useEffect, useState } from 'react';
import axios from 'axios';
import { getCars } from '../api/cars';
import { createService } from '../api/services';
import type { Car } from '../types/car';
import type { ApiError } from '../types/apiError';

interface ServiceFormProps {
  onServiceCreated: () => void;
  defaultCarId?: number | null;
}

export default function ServiceForm({ onServiceCreated, defaultCarId }: ServiceFormProps) {
  const [cars, setCars] = useState<Car[]>([]);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [carId, setCarId] = useState<number | ''>('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    getCars().then((page) => setCars(page.content));
  }, []);

  useEffect(() => {
    if (defaultCarId) setCarId(defaultCarId);
  }, [defaultCarId]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);

    if (carId === '') {
      setError('Please select a car.');
      return;
    }

    setSubmitting(true);
    try {
      await createService({ title, description, carId });
      setTitle('');
      setDescription('');
      onServiceCreated();
    } catch (err) {
      if (axios.isAxiosError<ApiError>(err) && err.response) {
        setError(err.response.data.error ?? 'Failed to create service.');
      } else {
        setError('Failed to create service. Please try again.');
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <h3>Add a new service</h3>

      {error && <p style={{ color: 'red' }}>{error}</p>}

      <div>
        <label>
          Car
          <select value={carId} onChange={(e) => setCarId(Number(e.target.value))} required>
            <option value="" disabled>
              Select a car
            </option>
            {cars.map((car) => (
              <option key={car.id} value={car.id}>
                {car.licensePlate} — {car.brand} {car.model}
              </option>
            ))}
          </select>
        </label>
      </div>

      <div>
        <label>
          Title
          <input value={title} onChange={(e) => setTitle(e.target.value)} required />
        </label>
      </div>

      <div>
        <label>
          Description
          <input value={description} onChange={(e) => setDescription(e.target.value)} />
        </label>
      </div>

      <button type="submit" disabled={submitting}>
        {submitting ? 'Adding...' : 'Add service'}
      </button>
    </form>
  );
}