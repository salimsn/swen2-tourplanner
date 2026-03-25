import { Injectable, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { DIFFICULTY_LEVELS, Difficulty } from '../models/difficulty.enum';
import { TourLog, TourLogPayload } from '../models/tour-log.model';

@Injectable()
export class TourLogFormViewModel {
  private readonly fb = inject(FormBuilder);
  readonly difficultyLevels = DIFFICULTY_LEVELS;

  readonly form = this.fb.nonNullable.group({
    dateTime: ['', Validators.required],
    comment: ['', [Validators.maxLength(1000)]],
    difficulty: this.fb.nonNullable.control<Difficulty>('MODERATE', Validators.required),
    totalDistanceKm: [1, [Validators.required, Validators.min(1)]],
    totalTimeMinutes: [30, [Validators.required, Validators.min(1)]],
    rating: [3, [Validators.required, Validators.min(1), Validators.max(5)]]
  });

  setLog(log?: TourLog | null) {
    if (!log) {
      this.reset();
      return;
    }

    this.form.setValue({
      dateTime: this.toDateTimeInput(log.dateTime),
      comment: log.comment ?? '',
      difficulty: log.difficulty,
      totalDistanceKm: log.totalDistanceKm,
      totalTimeMinutes: log.totalTimeMinutes,
      rating: log.rating
    });
  }

  reset() {
    const now = new Date().toISOString();
    this.form.reset({
      dateTime: this.toDateTimeInput(now),
      comment: '',
      difficulty: 'MODERATE',
      totalDistanceKm: 1,
      totalTimeMinutes: 30,
      rating: 3
    });
  }

  toPayload(): TourLogPayload {
    const value = this.form.getRawValue();
    return {
      ...value,
      dateTime: new Date(value.dateTime).toISOString()
    };
  }

  private toDateTimeInput(value: string) {
    if (!value) {
      return '';
    }
    return value.slice(0, 16);
  }
}
