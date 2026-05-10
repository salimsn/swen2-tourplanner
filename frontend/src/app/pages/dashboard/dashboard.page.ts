import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, effect, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { TourStore } from '../../services/tour-store.service';
import { UiCardComponent } from '../../shared/ui-card/ui-card.component';
import { TourFormViewModel } from '../../forms/tour-form.viewmodel';
import { TourLogFormViewModel } from '../../forms/tour-log-form.viewmodel';
import { AuthFormViewModel } from '../../forms/auth-form.viewmodel';
import { AuthService } from '../../services/auth.service';
import { AuthSessionService } from '../../services/auth-session.service';
import { TourService } from '../../services/tour.service';
import { RouteMapComponent } from '../../shared/route-map/route-map.component';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, UiCardComponent, RouteMapComponent],
  providers: [TourFormViewModel, TourLogFormViewModel, AuthFormViewModel],
  templateUrl: './dashboard.page.html',
  styleUrl: './dashboard.page.css'
})
export class DashboardPage implements OnInit {
  private readonly store = inject(TourStore);
  private readonly authApi = inject(AuthService);
  private readonly tourApi = inject(TourService);
  readonly authSession = inject(AuthSessionService);
  readonly vm = this.store.vm;
  readonly authForm = inject(AuthFormViewModel);
  readonly tourForm = inject(TourFormViewModel);
  readonly tourLogForm = inject(TourLogFormViewModel);
  readonly authMode = signal<'login' | 'register'>('login');
  readonly authLoading = signal(false);
  readonly authError = signal<string | null>(null);
  readonly importError = signal<string | null>(null);
  imageLoadFailed = false;
  currentImagePath = '';

  readonly tourFormDisabled = computed(() => this.tourForm.form.invalid || this.vm().loading);
  readonly logFormDisabled = computed(() => this.tourLogForm.form.invalid || this.vm().loading);

  constructor() {
    effect(() => {
      const vm = this.vm();
      if (vm.editorMode === 'create') {
        this.tourForm.reset();
      } else if (vm.editorMode === 'edit') {
        this.tourForm.setTour(vm.selectedTour);
      }

      if (vm.logEditorVisible) {
        const log = vm.logs.find((item) => item.id === vm.editingLogId) ?? null;
        this.tourLogForm.setLog(log);
      } else {
        this.tourLogForm.reset();
      }
    });

    effect(() => {
      this.currentImagePath = this.vm().selectedTour?.imagePath ?? '';
      this.imageLoadFailed = false;
    });
  }

  ngOnInit() {
    if (this.authSession.authenticated()) {
      this.store.loadTours();
    }
  }

  trackById(_: number, item: { id: string }) {
    return item.id;
  }

  selectTour(id: string) {
    this.store.selectTour(id);
  }

  showCreateTour() {
    this.store.startCreateTour();
  }

  setAuthMode(mode: 'login' | 'register') {
    this.authMode.set(mode);
    this.authError.set(null);
  }

  login() {
    if (this.authForm.loginForm.invalid) {
      return;
    }

    this.authLoading.set(true);
    this.authError.set(null);
    this.authApi.login(this.authForm.loginPayload()).subscribe({
      next: (user) => {
        this.authSession.setUser(user);
        this.authLoading.set(false);
        this.store.loadTours();
      },
      error: () => {
        this.authError.set('Login fehlgeschlagen. Bitte Zugangsdaten prüfen.');
        this.authLoading.set(false);
      }
    });
  }

  register() {
    if (this.authForm.registerForm.invalid) {
      return;
    }

    this.authLoading.set(true);
    this.authError.set(null);
    this.authApi.register(this.authForm.registerPayload()).subscribe({
      next: (user) => {
        this.authSession.setUser(user);
        this.authLoading.set(false);
        this.store.loadTours();
      },
      error: () => {
        this.authError.set('Registrierung fehlgeschlagen. Benutzername ist eventuell vergeben.');
        this.authLoading.set(false);
      }
    });
  }

  logout() {
    this.authSession.clear();
    this.store.clear();
  }

  searchTours(value: string) {
    this.store.loadTours(value);
  }

  exportTours() {
    this.tourApi.exportTours().subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = `tourplanner-export-${new Date().toISOString().slice(0, 10)}.json`;
        anchor.click();
        URL.revokeObjectURL(url);
      },
      error: () => this.importError.set('Export konnte nicht erstellt werden.')
    });
  }

  importTours(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }

    file.text()
      .then((content) => JSON.parse(content))
      .then((payload) => {
        this.importError.set(null);
        this.tourApi.importTours(payload).subscribe({
          next: (tours) => this.store.replaceTours(tours),
          error: () => this.importError.set('Import fehlgeschlagen. Bitte JSON-Datei prüfen.')
        });
      })
      .catch(() => this.importError.set('Import fehlgeschlagen. Datei ist kein gültiges JSON.'))
      .finally(() => {
        input.value = '';
      });
  }

  showEditTour() {
    this.store.startEditTour();
  }

  cancelTour() {
    this.store.cancelTourEditor();
  }

  addRouteStop() {
    this.tourForm.addRouteStop();
  }

  removeRouteStop(index: number) {
    this.tourForm.removeRouteStop(index);
  }

  saveTour() {
    const vm = this.vm();
    const payload = this.tourForm.toPayload();
    const tourId = vm.editorMode === 'edit' ? vm.selectedTour?.id : undefined;
    this.store.saveTour(payload, tourId);
  }

  removeTour(id: string) {
    this.store.deleteTour(id);
  }

  showCreateLog() {
    this.store.openLogEditor(null);
  }

  showEditLog(logId: string) {
    this.store.openLogEditor(logId);
  }

  cancelLog() {
    this.store.closeLogEditor();
  }

  saveLog() {
    const vm = this.vm();
    if (!vm.selectedTour) {
      return;
    }
    const payload = this.tourLogForm.toPayload();
    this.store.saveLog(vm.selectedTour.id, payload, vm.editingLogId ?? undefined);
  }

  removeLog(logId: string) {
    const vm = this.vm();
    if (!vm.selectedTour) {
      return;
    }
    this.store.deleteLog(vm.selectedTour.id, logId);
  }

  onImageError() {
    this.imageLoadFailed = true;
  }

  onImageLoad() {
    this.imageLoadFailed = false;
  }
}
