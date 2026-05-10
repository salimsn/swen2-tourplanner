import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Tour, TourPayload } from '../models/tour.model';
import { TourLog, TourLogPayload } from '../models/tour-log.model';
import { environment } from '../../environments/environment';
import { AuthSessionService } from './auth-session.service';

@Injectable({
  providedIn: 'root'
})
export class TourService {
  private readonly apiUrl = `${environment.apiUrl}/tours`;

  constructor(
    private readonly http: HttpClient,
    private readonly authSession: AuthSessionService
  ) {}

  getTours(search = '') {
    return this.http.get<Tour[]>(this.apiUrl, {
      ...this.authOptions(),
      params: search ? { search } : {}
    });
  }

  getTour(id: string) {
    return this.http.get<Tour>(`${this.apiUrl}/${id}`, this.authOptions());
  }

  createTour(payload: TourPayload) {
    return this.http.post<Tour>(this.apiUrl, payload, this.authOptions());
  }

  updateTour(id: string, payload: TourPayload) {
    return this.http.put<Tour>(`${this.apiUrl}/${id}`, payload, this.authOptions());
  }

  deleteTour(id: string) {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, this.authOptions());
  }

  createLog(tourId: string, payload: TourLogPayload) {
    return this.http.post<TourLog>(`${this.apiUrl}/${tourId}/logs`, payload, this.authOptions());
  }

  updateLog(tourId: string, logId: string, payload: TourLogPayload) {
    return this.http.put<TourLog>(`${this.apiUrl}/${tourId}/logs/${logId}`, payload, this.authOptions());
  }

  deleteLog(tourId: string, logId: string) {
    return this.http.delete<void>(`${this.apiUrl}/${tourId}/logs/${logId}`, this.authOptions());
  }

  exportTours() {
    return this.http.get(`${this.apiUrl}/export`, {
      ...this.authOptions(),
      responseType: 'blob'
    });
  }

  importTours(payload: unknown) {
    return this.http.post<Tour[]>(`${this.apiUrl}/import`, payload, this.authOptions());
  }

  private authOptions() {
    const userId = this.authSession.userId();
    return {
      headers: new HttpHeaders(userId ? { 'X-User-Id': userId } : {})
    };
  }
}
