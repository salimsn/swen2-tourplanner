import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, effect, inject } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { TourStore } from '../../services/tour-store.service';
import { UiCardComponent } from '../../shared/ui-card/ui-card.component';
import { TourFormViewModel } from '../../forms/tour-form.viewmodel';
import { TourLogFormViewModel } from '../../forms/tour-log-form.viewmodel';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, UiCardComponent],
  providers: [TourFormViewModel, TourLogFormViewModel],
  templateUrl: './dashboard.page.html',
  styleUrl: './dashboard.page.css'
})
export class DashboardPage implements OnInit {
  private readonly store = inject(TourStore);
  readonly vm = this.store.vm;
  readonly tourForm = inject(TourFormViewModel);
  readonly tourLogForm = inject(TourLogFormViewModel);

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
  }

  ngOnInit() {
    this.store.loadTours();
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

  showEditTour() {
    this.store.startEditTour();
  }

  cancelTour() {
    this.store.cancelTourEditor();
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
}
