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
  routeWaypoints: string;
  routeStops: string[];
  routeInformation: string;
  routeGeoJson: string;
  imagePath: string;
  createdAt: string;
  updatedAt: string;
  logs: TourLog[];
  popularity: number;
  popularityLabel: string;
  childFriendlinessScore: number;
  childFriendliness: string;
  achievementBadge: string;
}

export interface TourPayload {
  name: string;
  description: string;
  fromLocation: string;
  toLocation: string;
  transportType: TransportType;
  routeWaypoints: string;
  routeStops: string[];
  routeInformation: string;
  imagePath: string;
}
