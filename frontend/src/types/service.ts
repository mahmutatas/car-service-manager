export type ServiceStatus = 'PENDING' | 'IN_PROGRESS' | 'DONE';

export interface Service {
  id: number;
  title: string;
  description: string;
  status: ServiceStatus;
  createdAt: string;
  carId: number;
  carLicensePlate: string;
  version: number;
}

export interface CreateServiceRequest {
  title: string;
  description: string;
  carId: number;
}

export interface UpdateServiceRequest {
  title?: string;
  description?: string;
  status?: ServiceStatus;
}