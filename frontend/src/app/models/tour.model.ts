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

export type TourPayload = Omit<
  Tour,
  | 'id'
  | 'logs'
  | 'createdAt'
  | 'updatedAt'
  | 'ownerId'
  | 'routeGeoJson'
  | 'popularity'
  | 'popularityLabel'
  | 'childFriendlinessScore'
  | 'childFriendliness'
  | 'achievementBadge'
>;
