import { useEffect, useState } from 'react';
import { getCars } from '../api/cars';
import type { Car } from '../types/car';

interface CarListProps {
  refreshTrigger: number;
  selectedCarId: number | null;
  onSelectCar: (car: Car) => void;
}

export default function CarList({ refreshTrigger, selectedCarId, onSelectCar }: CarListProps) {
  const [cars, setCars] = useState<Car[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    setError(null);

    getCars()
      .then((page) => setCars(page.content))
      .catch(() => setError('Failed to load cars.'))
      .finally(() => setLoading(false));
  }, [refreshTrigger]);

  if (loading) return <p>Loading cars...</p>;
  if (error) return <p style={{ color: 'red' }}>{error}</p>;

  return (
    <div>
      <h3>Cars</h3>
      <table border={1} cellPadding={6}>
        <thead>
          <tr>
            <th>License plate</th>
            <th>Brand</th>
            <th>Model</th>
          </tr>
        </thead>
        <tbody>
          {cars.map((car) => (
            <tr
              key={car.id}
              onClick={() => onSelectCar(car)}
              style={{
                cursor: 'pointer',
                backgroundColor: car.id === selectedCarId ? '#e0f0ff' : undefined,
              }}
            >
              <td>{car.licensePlate}</td>
              <td>{car.brand}</td>
              <td>{car.model}</td>
            </tr>
          ))}
        </tbody>
      </table>
      {cars.length === 0 && <p>No cars yet. Add one above.</p>}
    </div>
  );
}