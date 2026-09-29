import { Component, ElementRef, afterRenderEffect, inject, signal, viewChild } from '@angular/core';
import { EventsService } from './events.service';
import {EventType, HeatmapPoint} from './heatmap-point';

const MAP_SIZE = 14900;

const COLORS: Record<EventType, string> = {
  KILL: '#2ecc71',
  DEATH: '#e74c3c',
  ASSIST: '#f1c40f',
};

@Component({
  imports: [],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  private eventsService = inject(EventsService);

  protected readonly points = signal<HeatmapPoint[]>([]);

  private canvas = viewChild.required<ElementRef<HTMLCanvasElement>>('canvas');

  constructor() {
    this.eventsService.getPoints('Słodki Femboy', '2137', 1).subscribe(result => {
      console.log(result);
      this.points.set(result);
    });

    afterRenderEffect(() => this.draw(this.points()));
  }

  private draw(points: HeatmapPoint[]) {
    const canvas = this.canvas().nativeElement;
    const ctx = canvas.getContext('2d')!;

    ctx.clearRect(0, 0, canvas.width, canvas.height);

    for (const p of points) {
      const px = p.x / MAP_SIZE * canvas.width;
      const py = canvas.height - p.y / MAP_SIZE * canvas.height;

      ctx.fillStyle = COLORS[p.type];
      ctx.beginPath();
      ctx.arc(px, py, 5, 0, Math.PI * 2);
      ctx.fill();
    }
  }
}
