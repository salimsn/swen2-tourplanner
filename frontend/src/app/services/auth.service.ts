import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { AuthenticatedUser, LoginPayload, RegisterPayload } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly apiUrl = `${environment.apiUrl}/auth`;

  constructor(private readonly http: HttpClient) {}

  login(payload: LoginPayload) {
    return this.http.post<AuthenticatedUser>(`${this.apiUrl}/login`, payload);
  }

  register(payload: RegisterPayload) {
    return this.http.post<AuthenticatedUser>(`${this.apiUrl}/register`, payload);
  }
}
