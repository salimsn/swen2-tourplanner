import { Injectable, computed, signal } from '@angular/core';
import { AuthenticatedUser } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthSessionService {
  private readonly storageKey = 'tourplanner.user';
  private readonly currentUser = signal<AuthenticatedUser | null>(this.readStoredUser());

  readonly user = computed(() => this.currentUser());
  readonly authenticated = computed(() => this.currentUser() !== null);

  setUser(user: AuthenticatedUser) {
    this.currentUser.set(user);
    localStorage.setItem(this.storageKey, JSON.stringify(user));
  }

  clear() {
    this.currentUser.set(null);
    localStorage.removeItem(this.storageKey);
  }

  userId() {
    return this.currentUser()?.id ?? null;
  }

  private readStoredUser(): AuthenticatedUser | null {
    const raw = localStorage.getItem(this.storageKey);
    if (!raw) {
      return null;
    }

    try {
      return JSON.parse(raw) as AuthenticatedUser;
    } catch {
      localStorage.removeItem(this.storageKey);
      return null;
    }
  }
}
