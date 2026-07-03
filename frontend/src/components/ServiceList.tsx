import { useEffect, useState } from 'react';
import axios from 'axios';
import { getCars } from '../api/cars';
import { getServices, updateService } from '../api/services';
import type { Car } from '../types/car';
import type { Service, ServiceStatus } from '../types/service';
import type { ApiError } from '../types/apiError';

interface ServiceListProps {
  selectedCarId: number | null;
  refreshTrigger: number;
}

function getValidNextStatuses(current: ServiceStatus): ServiceStatus[] {
  switch (current) {
    case 'PENDING':
      return ['IN_PROGRESS'];
    case 'IN_PROGRESS':
      return ['DONE'];
    case 'DONE':
      return [];
  }
}

export default function ServiceList({ selectedCarId, refreshTrigger }: ServiceListProps) {
  const [cars, setCars] = useState<Car[]>([]);
  const [services, setServices] = useState<Service[]>([]);
  const [carFilter, setCarFilter] = useState<number | ''>('');
  const [statusFilter, setStatusFilter] = useState<ServiceStatus | ''>('');
  const [loading, setLoading] = useState(true);
  const [rowError, setRowError] = useState<{ id: number; message: string } | null>(null);

  const [editingId, setEditingId] = useState<number | null>(null);
  const [editTitle, setEditTitle] = useState('');
  const [editDescription, setEditDescription] = useState('');

  useEffect(() => {
    getCars().then((page) => setCars(page.content));
  }, []);

  useEffect(() => {
    setCarFilter(selectedCarId ?? '');
  }, [selectedCarId]);

  function fetchServices() {
    setLoading(true);
    getServices(carFilter === '' ? undefined : carFilter, statusFilter === '' ? undefined : statusFilter)
      .then((page) => setServices(page.content))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    fetchServices();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [carFilter, statusFilter, refreshTrigger]);

  async function handleStatusChange(service: Service, newStatus: ServiceStatus) {
    setRowError(null);
    try {
      await updateService(service.id, { status: newStatus });
      fetchServices();
    } catch (err) {
      if (axios.isAxiosError<ApiError>(err) && err.response?.status === 409) {
        setRowError({ id: service.id, message: 'This service was updated by someone else. Refreshing...' });
      } else if (axios.isAxiosError<ApiError>(err) && err.response) {
        setRowError({ id: service.id, message: err.response.data.error });
      } else {
        setRowError({ id: service.id, message: 'Failed to update status.' });
      }
      fetchServices(); // refresh row data instead of leaving stale/crashed UI
    }
  }

  function startEdit(service: Service) {
    setEditingId(service.id);
    setEditTitle(service.title);
    setEditDescription(service.description);
  }

  async function saveEdit(service: Service) {
    setRowError(null);
    try {
      await updateService(service.id, { title: editTitle, description: editDescription });
      setEditingId(null);
      fetchServices();
    } catch (err) {
      if (axios.isAxiosError<ApiError>(err) && err.response) {
        setRowError({ id: service.id, message: err.response.data.error });
      } else {
        setRowError({ id: service.id, message: 'Failed to save changes.' });
      }
      fetchServices();
    }
  }

  return (
    <div>
      <h3>Services</h3>

      <div style={{ display: 'flex', gap: '1rem', marginBottom: '0.75rem' }}>
        <label>
          Car:{' '}
          <select value={carFilter} onChange={(e) => setCarFilter(e.target.value === '' ? '' : Number(e.target.value))}>
            <option value="">All cars</option>
            {cars.map((car) => (
              <option key={car.id} value={car.id}>
                {car.licensePlate}
              </option>
            ))}
          </select>
        </label>

        <label>
          Status:{' '}
          <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value as ServiceStatus | '')}>
            <option value="">All statuses</option>
            <option value="PENDING">PENDING</option>
            <option value="IN_PROGRESS">IN_PROGRESS</option>
            <option value="DONE">DONE</option>
          </select>
        </label>
      </div>

      {loading ? (
        <p>Loading services...</p>
      ) : (
        <table border={1} cellPadding={6}>
          <thead>
            <tr>
              <th>Title</th>
              <th>Description</th>
              <th>Car</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {services.map((service) => {
              const nextStatuses = getValidNextStatuses(service.status);
              const isEditing = editingId === service.id;

              return (
                <tr key={service.id}>
                  <td>
                    {isEditing ? (
                      <input value={editTitle} onChange={(e) => setEditTitle(e.target.value)} />
                    ) : (
                      service.title
                    )}
                  </td>
                  <td>
                    {isEditing ? (
                      <input value={editDescription} onChange={(e) => setEditDescription(e.target.value)} />
                    ) : (
                      service.description
                    )}
                  </td>
                  <td>{service.carLicensePlate}</td>
                  <td>
                    {service.status}
                    {nextStatuses.length > 0 && (
                      <>
                        {' '}
                        <select
                          value=""
                          onChange={(e) => handleStatusChange(service, e.target.value as ServiceStatus)}
                        >
                          <option value="" disabled>
                            Change to...
                          </option>
                          {nextStatuses.map((status) => (
                            <option key={status} value={status}>
                              {status}
                            </option>
                          ))}
                        </select>
                      </>
                    )}
                    {rowError?.id === service.id && (
                      <div style={{ color: 'red', fontSize: '0.8rem' }}>{rowError.message}</div>
                    )}
                  </td>
                  <td>
                    {isEditing ? (
                      <>
                        <button onClick={() => saveEdit(service)}>Save</button>{' '}
                        <button onClick={() => setEditingId(null)}>Cancel</button>
                      </>
                    ) : (
                      <button onClick={() => startEdit(service)}>Edit</button>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      )}
      {!loading && services.length === 0 && <p>No services found.</p>}
    </div>
  );
}