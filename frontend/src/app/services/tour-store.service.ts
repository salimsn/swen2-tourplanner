import { Injectable, computed, inject, signal } from '@angular/core';
import { Tour, TourPayload } from '../models/tour.model';
import { TourLog, TourLogPayload } from '../models/tour-log.model';
import { TourService } from './tour.service';

type EditorMode = 'view' | 'create' | 'edit';

export interface TourViewModel {
  tours: Tour[];
  selectedTour: Tour | null;
  logs: TourLog[];
  loading: boolean;
  error: string | null;
  editorMode: EditorMode;
  logEditorVisible: boolean;
  editingLogId: string | null;
  searchTerm: string;
}

@Injectable({
  providedIn: 'root'
})
export class TourStore {
  private readonly tourService = inject(TourService);

  private readonly tours = signal<Tour[]>([]);
  private readonly loading = signal(false);
  private readonly error = signal<string | null>(null);
  private readonly selectedTourId = signal<string | null>(null);
  private readonly editorMode = signal<EditorMode>('view');
  private readonly logEditorVisible = signal(false);
  private readonly editingLogId = signal<string | null>(null);
  private readonly searchTerm = signal('');

  readonly vm = computed<TourViewModel>(() => {
    const tours = this.tours();
    const selected = tours.find((tour) => tour.id === this.selectedTourId()) ?? null;
    return {
      tours,
      selectedTour: selected,
      logs: selected?.logs ?? [],
      loading: this.loading(),
      error: this.error(),
      editorMode: this.editorMode(),
      logEditorVisible: this.logEditorVisible(),
      editingLogId: this.editingLogId(),
      searchTerm: this.searchTerm()
    };
  });

  loadTours(searchTerm = this.searchTerm()) {
    this.loading.set(true);
    this.error.set(null);
    this.searchTerm.set(searchTerm);
    this.tourService.getTours(searchTerm).subscribe({
      next: (tours) => {
        this.tours.set(tours);
        const selectedExists = tours.some((tour) => tour.id === this.selectedTourId());
        if ((!this.selectedTourId() || !selectedExists) && tours.length > 0) {
          this.selectedTourId.set(tours[0].id);
        } else if (tours.length === 0) {
          this.selectedTourId.set(null);
        }
        this.loading.set(false);
      },
      error: (err) => this.handleError('Die Touren konnten nicht geladen werden.', err)
    });
  }

  clear() {
    this.tours.set([]);
    this.selectedTourId.set(null);
    this.editorMode.set('view');
    this.logEditorVisible.set(false);
    this.editingLogId.set(null);
    this.searchTerm.set('');
    this.error.set(null);
    this.loading.set(false);
  }

  selectTour(id: string) {
    this.selectedTourId.set(id);
    this.editorMode.set('view');
    this.logEditorVisible.set(false);
  }

  startCreateTour() {
    this.editorMode.set('create');
  }

  startEditTour() {
    if (this.selectedTourId()) {
      this.editorMode.set('edit');
    }
  }

  cancelTourEditor() {
    this.editorMode.set('view');
  }

  saveTour(payload: TourPayload, tourId?: string) {
    this.loading.set(true);
    this.error.set(null);
    const request$ = tourId
      ? this.tourService.updateTour(tourId, payload)
      : this.tourService.createTour(payload);

    request$.subscribe({
      next: (tour) => {
        if (tourId) {
          this.tours.update((list) => list.map((item) => (item.id === tour.id ? tour : item)));
        } else {
          this.tours.update((list) => [tour, ...list]);
          this.selectedTourId.set(tour.id);
        }
        this.editorMode.set('view');
        this.loading.set(false);
      },
      error: (err) => this.handleError('Die Tour konnte nicht gespeichert werden.', err)
    });
  }

  deleteTour(id: string) {
    this.loading.set(true);
    this.error.set(null);
    this.tourService.deleteTour(id).subscribe({
      next: () => {
        this.tours.update((list) => list.filter((tour) => tour.id !== id));
        const nextTour = this.tours()[0] ?? null;
        this.selectedTourId.set(nextTour?.id ?? null);
        this.editorMode.set('view');
        this.logEditorVisible.set(false);
        this.loading.set(false);
      },
      error: (err) => this.handleError('Die Tour konnte nicht gelöscht werden.', err)
    });
  }

  openLogEditor(logId: string | null = null) {
    this.logEditorVisible.set(true);
    this.editingLogId.set(logId);
  }

  closeLogEditor() {
    this.logEditorVisible.set(false);
    this.editingLogId.set(null);
  }

  saveLog(tourId: string, payload: TourLogPayload, logId?: string) {
    this.loading.set(true);
    this.error.set(null);
    const request$ = logId
      ? this.tourService.updateLog(tourId, logId, payload)
      : this.tourService.createLog(tourId, payload);

    request$.subscribe({
      next: (log) => {
        this.tours.update((list) =>
          list.map((tour) => {
            if (tour.id !== tourId) {
              return tour;
            }
            const logs = logId
              ? tour.logs.map((existing) => (existing.id === log.id ? log : existing))
              : [log, ...tour.logs];
            return { ...tour, logs };
          })
        );
        this.closeLogEditor();
        this.loading.set(false);
        this.loadTours(this.searchTerm());
      },
      error: (err) => this.handleError('Der Tour-Log konnte nicht gespeichert werden.', err)
    });
  }

  deleteLog(tourId: string, logId: string) {
    this.loading.set(true);
    this.error.set(null);
    this.tourService.deleteLog(tourId, logId).subscribe({
      next: () => {
        this.tours.update((list) =>
          list.map((tour) => {
            if (tour.id !== tourId) {
              return tour;
            }
            return { ...tour, logs: tour.logs.filter((log) => log.id !== logId) };
          })
        );
        this.loading.set(false);
        this.loadTours(this.searchTerm());
      },
      error: (err) => this.handleError('Der Tour-Log konnte nicht entfernt werden.', err)
    });
  }

  replaceTours(tours: Tour[]) {
    this.tours.set(tours);
    this.selectedTourId.set(tours[0]?.id ?? null);
    this.editorMode.set('view');
    this.logEditorVisible.set(false);
  }

  private handleError(message: string, error: unknown) {
    console.error(error);
    this.error.set(message);
    this.loading.set(false);
  }
}
