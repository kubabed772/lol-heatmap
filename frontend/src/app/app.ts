import {Component, ElementRef, afterRenderEffect, inject, signal, viewChild, computed} from '@angular/core';
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

  protected readonly types: EventType[] = ['KILL', 'DEATH', 'ASSIST'];
  
  protected readonly colors = COLORS;

  protected readonly visibleTypes = signal<Record<EventType, boolean>>({
    KILL: true,
    DEATH: true,
    ASSIST: true,
  });

  protected readonly visiblePoints = computed(() =>
    this.points().filter(p => this.visibleTypes()[p.type])
  );

  protected readonly counts = computed(() => {
    const c: Record<EventType, number> = { KILL: 0, DEATH: 0, ASSIST: 0 };
    for (const p of this.points()) c[p.type]++;
    return c;
  });

  protected readonly loading = signal(false);

  private canvas = viewChild.required<ElementRef<HTMLCanvasElement>>('canvas');

  constructor() {
    afterRenderEffect(() => this.draw(this.visiblePoints()));
  }

  protected load(name: string, tag: string, count: number) {
    this.loading.set(true);

    this.eventsService.getPoints(name, tag, count).subscribe({
      next: result => {
        this.points.set(result);
        this.loading.set(false);
      },
      error: err => {
        console.error(err);
        this.points.set([]);
        this.loading.set(false);
      },
    });
  }

  protected toggle(type: EventType) {
    this.visibleTypes.update(v => ({ ...v, [type]: !v[type] }));
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
