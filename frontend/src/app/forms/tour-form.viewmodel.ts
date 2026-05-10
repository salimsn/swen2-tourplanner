import { Injectable, inject } from '@angular/core';
import { FormBuilder, FormControl, Validators } from '@angular/forms';
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
    routeWaypoints: [''],
    routeStops: this.fb.array<FormControl<string>>([]),
    routeInformation: [''],
    imagePath: ['']
  });

  get routeStops() {
    return this.form.controls.routeStops;
  }

  addRouteStop(value = '') {
    this.routeStops.push(this.fb.nonNullable.control(value, [Validators.maxLength(180)]));
  }

  removeRouteStop(index: number) {
    this.routeStops.removeAt(index);
  }

  setTour(tour?: Tour | null) {
    if (!tour) {
      this.reset();
      return;
    }

    this.form.patchValue({
      name: tour.name,
      description: tour.description,
      fromLocation: tour.fromLocation,
      toLocation: tour.toLocation,
      transportType: tour.transportType,
      distanceKm: tour.distanceKm,
      estimatedTimeMinutes: tour.estimatedTimeMinutes,
      routeWaypoints: tour.routeWaypoints ?? '',
      routeInformation: tour.routeInformation ?? '',
      imagePath: tour.imagePath ?? ''
    });
    this.setRouteStops(tour.routeStops ?? []);
  }

  reset() {
    this.routeStops.clear();
    this.form.reset({
      name: '',
      description: '',
      fromLocation: '',
      toLocation: '',
      transportType: 'BIKE',
      distanceKm: 5,
      estimatedTimeMinutes: 60,
      routeWaypoints: '',
      routeStops: [],
      routeInformation: '',
      imagePath: ''
    });
  }

  toPayload(): TourPayload {
    const raw = this.form.getRawValue();
    return {
      ...raw,
      routeStops: raw.routeStops.map((stop) => stop.trim()).filter(Boolean)
    };
  }

  private setRouteStops(stops: string[]) {
    this.routeStops.clear();
    stops
      .map((stop) => stop.trim())
      .filter(Boolean)
      .forEach((stop) => this.addRouteStop(stop));
  }
}
