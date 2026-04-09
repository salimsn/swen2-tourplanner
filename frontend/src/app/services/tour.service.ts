import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Tour, TourPayload } from '../models/tour.model';
import { TourLog, TourLogPayload } from '../models/tour-log.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TourService {
  private readonly apiUrl = `${environment.apiUrl}/tours`;

  constructor(private readonly http: HttpClient) {}

  getTours() {
    return this.http.get<Tour[]>(this.apiUrl);
  }

  getTour(id: string) {
    return this.http.get<Tour>(`${this.apiUrl}/${id}`);
  }

  createTour(payload: TourPayload) {
    return this.http.post<Tour>(this.apiUrl, payload);
  }

  updateTour(id: string, payload: TourPayload) {
    return this.http.put<Tour>(`${this.apiUrl}/${id}`, payload);
  }

  deleteTour(id: string) {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  createLog(tourId: string, payload: TourLogPayload) {
    return this.http.post<TourLog>(`${this.apiUrl}/${tourId}/logs`, payload);
  }

  updateLog(tourId: string, logId: string, payload: TourLogPayload) {
    return this.http.put<TourLog>(`${this.apiUrl}/${tourId}/logs/${logId}`, payload);
  }

  deleteLog(tourId: string, logId: string) {
    return this.http.delete<void>(`${this.apiUrl}/${tourId}/logs/${logId}`);
  }
}
