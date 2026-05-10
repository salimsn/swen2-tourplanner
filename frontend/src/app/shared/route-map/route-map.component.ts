import { CommonModule } from '@angular/common';
import {
  AfterViewInit,
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnDestroy,
  SimpleChanges,
  ViewChild
} from '@angular/core';

declare const L: any;

@Component({
  selector: 'app-route-map',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="route-map">
      <div #mapTarget class="route-map__canvas"></div>
      <div class="route-map__fallback" *ngIf="!mapReady">
        <strong>Route</strong>
        <span>{{ fromLocation }} -> {{ toLocation }}</span>
      </div>
    </div>
  `,
  styles: [`
    .route-map {
      position: relative;
      min-height: 260px;
      border-radius: 1rem;
      overflow: hidden;
      border: 1px solid rgba(15, 23, 42, 0.12);
      background: #e7edf3;
    }

    .route-map__canvas {
      height: 100%;
      min-height: 260px;
      width: 100%;
    }

    .route-map__fallback {
      position: absolute;
      inset: 0;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 0.35rem;
      text-align: center;
      padding: 1rem;
      color: #0f172a;
      background: linear-gradient(135deg, rgba(231, 237, 243, 0.92), rgba(255, 255, 255, 0.9));
    }
  `]
})
export class RouteMapComponent implements AfterViewInit, OnChanges, OnDestroy {
  @Input() routeGeoJson = '';
  @Input() fromLocation = '';
  @Input() toLocation = '';
  @ViewChild('mapTarget') private readonly mapTarget?: ElementRef<HTMLDivElement>;

  mapReady = false;
  private map?: any;
  private routeLayer?: any;

  ngAfterViewInit() {
    this.renderMap();
  }

  ngOnChanges(_: SimpleChanges) {
    this.renderMap();
  }

  ngOnDestroy() {
    this.map?.remove();
  }

  private renderMap() {
    queueMicrotask(() => {
      if (!this.mapTarget || typeof L === 'undefined') {
        this.mapReady = false;
        return;
      }

      if (!this.map) {
        this.map = L.map(this.mapTarget.nativeElement, {
          zoomControl: true,
          attributionControl: true
        }).setView([48.2082, 16.3738], 7);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          maxZoom: 19,
          attribution: '&copy; OpenStreetMap contributors'
        }).addTo(this.map);
      }

      this.routeLayer?.remove();
      const geometry = this.parseRoute();
      if (!geometry) {
        this.mapReady = false;
        return;
      }

      this.routeLayer = L.geoJSON(geometry, {
        style: {
          color: '#2563eb',
          weight: 5,
          opacity: 0.85
        }
      }).addTo(this.map);
      this.map.fitBounds(this.routeLayer.getBounds(), { padding: [24, 24] });
      this.mapReady = true;
      setTimeout(() => this.map?.invalidateSize(), 0);
    });
  }

  private parseRoute() {
    if (!this.routeGeoJson) {
      return null;
    }

    try {
      return JSON.parse(this.routeGeoJson);
    } catch {
      return null;
    }
  }
}
