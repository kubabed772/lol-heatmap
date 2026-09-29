import { Component, inject, signal } from '@angular/core';
import { EventsService } from './events.service';
import { HeatmapPoint } from './heatmap-point';

@Component({
  imports: [],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  private eventsService = inject(EventsService);

  protected readonly points = signal<HeatmapPoint[]>([]);

  constructor() {
    this.eventsService.getPoints('Słodki Femboy', '2137', 1).subscribe(result => {
      console.log(result);
      this.points.set(result);
    });
  }
}
