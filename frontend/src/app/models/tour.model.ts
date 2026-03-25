import { TourLog } from './tour-log.model';
import { TransportType } from './transport-type.enum';

export interface Tour {
  id: string;
  ownerId: string;
  name: string;
  description: string;
  fromLocation: string;
  toLocation: string;
  transportType: TransportType;
  distanceKm: number;
  estimatedTimeMinutes: number;
  routeInformation: string;
  imagePath: string;
  createdAt: string;
  updatedAt: string;
  logs: TourLog[];
}

export type TourPayload = Omit<Tour, 'id' | 'logs' | 'createdAt' | 'updatedAt' | 'ownerId'>;
