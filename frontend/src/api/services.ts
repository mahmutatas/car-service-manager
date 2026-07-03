import apiClient from './client';
import type { Service, CreateServiceRequest, UpdateServiceRequest } from '../types/service';
import type { Page } from '../types/page';

export async function getServices(
  carId?: number,
  status?: string,
  page = 0,
  size = 20
): Promise<Page<Service>> {
  const response = await apiClient.get<Page<Service>>('/services', {
    params: { carId, status, page, size },
  });
  return response.data;
}

export async function createService(service: CreateServiceRequest): Promise<Service> {
  const response = await apiClient.post<Service>('/services', service);
  return response.data;
}

export async function updateService(id: number, service: UpdateServiceRequest): Promise<Service> {
  const response = await apiClient.put<Service>(`/services/${id}`, service);
  return response.data;
}