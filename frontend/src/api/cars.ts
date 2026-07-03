import apiClient from './client';
import type { Car, CarRequest } from '../types/car';
import type { Page } from '../types/page';

export async function getCars(page = 0, size = 20): Promise<Page<Car>> {
  const response = await apiClient.get<Page<Car>>('/cars', {
    params: { page, size },
  });
  return response.data;
}

export async function createCar(car: CarRequest): Promise<Car> {
  const response = await apiClient.post<Car>('/cars', car);
  return response.data;
}

export async function updateCar(id: number, car: CarRequest): Promise<Car> {
  const response = await apiClient.put<Car>(`/cars/${id}`, car);
  return response.data;
}