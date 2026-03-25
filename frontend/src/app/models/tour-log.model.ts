import { Difficulty } from './difficulty.enum';

export interface TourLog {
  id: string;
  dateTime: string;
  comment: string;
  difficulty: Difficulty;
  totalDistanceKm: number;
  totalTimeMinutes: number;
  rating: number;
}

export type TourLogPayload = Omit<TourLog, 'id'>;
