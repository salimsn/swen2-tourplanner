import { Injectable, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Tour, TourPayload } from '../models/tour.model';
import { TRANSPORT_TYPES, TransportType } from '../models/transport-type.enum';

@Injectable()
export class TourFormViewModel {
  private readonly fb = inject(FormBuilder);
  readonly transportTypes = TRANSPORT_TYPES;

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(3)]],
    description: ['', [Validators.required, Validators.maxLength(1500)]],
    fromLocation: ['', Validators.required],
    toLocation: ['', Validators.required],
    transportType: this.fb.nonNullable.control<TransportType>('BIKE', Validators.required),
    distanceKm: [5, [Validators.required, Validators.min(1)]],
    estimatedTimeMinutes: [60, [Validators.required, Validators.min(1)]],
    routeInformation: [''],
    imagePath: ['']
  });

  setTour(tour?: Tour | null) {
    if (!tour) {
      this.reset();
      return;
    }

    this.form.setValue({
      name: tour.name,
      description: tour.description,
      fromLocation: tour.fromLocation,
      toLocation: tour.toLocation,
      transportType: tour.transportType,
      distanceKm: tour.distanceKm,
      estimatedTimeMinutes: tour.estimatedTimeMinutes,
      routeInformation: tour.routeInformation ?? '',
      imagePath: tour.imagePath ?? ''
    });
  }

  reset() {
    this.form.reset({
      name: '',
      description: '',
      fromLocation: '',
      toLocation: '',
      transportType: 'BIKE',
      distanceKm: 5,
      estimatedTimeMinutes: 60,
      routeInformation: '',
      imagePath: ''
    });
  }

  toPayload(): TourPayload {
    return this.form.getRawValue();
  }
}
