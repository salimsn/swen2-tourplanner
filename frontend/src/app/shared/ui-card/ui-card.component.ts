import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-ui-card',
  standalone: true,
  template: `
    <div class="ui-card" [class.ui-card--muted]="variant === 'muted'">
      <ng-content />
    </div>
  `,
  styles: [`
    .ui-card {
      background: var(--surface, #ffffff);
      border-radius: 1rem;
      padding: 1.5rem;
      border: 1px solid rgba(15, 23, 42, 0.08);
      box-shadow: 0 15px 35px rgba(15, 23, 42, 0.07);
    }

    .ui-card--muted {
      background: rgba(255, 255, 255, 0.4);
      border-style: dashed;
    }
  `]
})
export class UiCardComponent {
  @Input() variant: 'surface' | 'muted' = 'surface';
}
