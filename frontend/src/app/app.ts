import {Component, ElementRef, afterRenderEffect, inject, signal, viewChild, computed} from '@angular/core';
import { EventsService } from './events.service';
import {EventType, HeatmapPoint, ImportStatus} from './heatmap-point';
import {Subscription, switchMap, takeWhile, timer} from 'rxjs';

const MAP_SIZE = 14900;

const MIN_GAMES = 15;

const COLORS: Record<EventType, string> = {
  KILL: '#2ecc71',
  DEATH: '#e74c3c',
  ASSIST: '#f1c40f',
};

function hexToRgb(hex: string): [number, number, number] {
  const n = parseInt(hex.slice(1), 16);
  return [(n >> 16) & 255, (n >> 8) & 255, n & 255];
}

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

  protected readonly selectedType = signal<EventType>('DEATH');

  protected readonly selectedChampion = signal('');

  protected readonly champions = computed(() => {
    const games = new Map<string, Set<string>>();
    for (const p of this.points()) {
      if (!games.has(p.champion)) games.set(p.champion, new Set());
      games.get(p.champion)!.add(p.matchId);
    }
    return [...games.entries()]
      .map(([name, ids]) => ({ name, games: ids.size }))
      .filter(c => c.games >= MIN_GAMES)
      .sort((a, b) => b.games - a.games);
  });

  protected readonly mode = signal<'dots' | 'heat'>('heat');

  protected readonly championPoints = computed(() => {
    const champ = this.selectedChampion();
    return champ ? this.points().filter(p => p.champion === champ) : this.points();
  });

  protected readonly visiblePoints = computed(() =>
    this.championPoints().filter(p => p.type === this.selectedType())
  );

  protected readonly counts = computed(() => {
    const c: Record<EventType, number> = { KILL: 0, DEATH: 0, ASSIST: 0 };
    for (const p of this.championPoints()) c[p.type]++;
    return c;
  });

  protected readonly loading = signal(false);

  protected readonly importStatus = signal<ImportStatus | null>(null);

  protected readonly importing = computed(() => {
    const s = this.importStatus()?.state;
    return s === 'QUEUED' || s === 'RUNNING' || s === 'WAITING';
  });

  private pollSub?: Subscription;

  private canvas = viewChild.required<ElementRef<HTMLCanvasElement>>('canvas');

  constructor() {
    afterRenderEffect(() => this.draw(this.visiblePoints(), this.mode()));
  }

  protected load(name: string, tag: string, count: number) {
    this.loading.set(true);

    this.eventsService.getPoints(name, tag, count).subscribe({
      next: result => {
        this.points.set(result);
        this.loading.set(false);
        this.selectedChampion.set('');
      },
      error: err => {
        console.error(err);
        this.points.set([]);
        this.loading.set(false);
      },
    });
  }

  protected startImport(name: string, tag: string, count: number) {
    this.eventsService.startImport(name, tag, count).subscribe({
      next: status => {
        this.importStatus.set(status);
        this.poll(status.puuid, name, tag, count);
      },
      error: err => console.error(err),
    });
  }

  private poll(puuid: string, name: string, tag: string, count: number) {
    this.pollSub?.unsubscribe();

    this.pollSub = timer(0, 2000).pipe(
      switchMap(() => this.eventsService.getImportStatus(puuid)),
      takeWhile(s => s.state === 'QUEUED' || s.state === 'RUNNING' || s.state === 'WAITING', true),
    ).subscribe(s => {
      this.importStatus.set(s);
      if (s.state === 'DONE') this.load(name, tag, count);
    });
  }

  private draw(points: HeatmapPoint[], mode: 'dots' | 'heat') {
    const canvas = this.canvas().nativeElement;
    const ctx = canvas.getContext('2d')!;
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    if (mode === 'heat') this.drawHeat(ctx, canvas, points);
    else this.drawDots(ctx, canvas, points);
  }

  private toPixel(p: HeatmapPoint, canvas: HTMLCanvasElement) {
    return {
      px: p.x / MAP_SIZE * canvas.width,
      py: canvas.height - p.y / MAP_SIZE * canvas.height,
    };
  }

  private drawDots(ctx: CanvasRenderingContext2D, canvas: HTMLCanvasElement, points: HeatmapPoint[]) {
    for (const p of points) {
      const { px, py } = this.toPixel(p, canvas);
      ctx.fillStyle = COLORS[p.type];
      ctx.beginPath();
      ctx.arc(px, py, 5, 0, Math.PI * 2);
      ctx.fill();
    }
  }

  private drawHeat(ctx: CanvasRenderingContext2D, canvas: HTMLCanvasElement, points: HeatmapPoint[]) {
    for (const type of this.types) {
      const pts = points.filter(p => p.type === type);
      if (pts.length === 0) continue;

      const layer = this.heatLayer(canvas, pts, COLORS[type]);
      ctx.drawImage(layer, 0, 0);
    }
  }

  private heatLayer(canvas: HTMLCanvasElement, pts: HeatmapPoint[], hex: string): HTMLCanvasElement {
    const w = canvas.width;
    const h = canvas.height;
    const density = new Float32Array(w * h);
    const r = 14;

    for (const p of pts) {
      const { px, py } = this.toPixel(p, canvas);
      const cx = Math.round(px);
      const cy = Math.round(py);

      for (let y = Math.max(0, cy - r); y <= Math.min(h - 1, cy + r); y++) {
        for (let x = Math.max(0, cx - r); x <= Math.min(w - 1, cx + r); x++) {
          const dist = Math.sqrt((x - cx) ** 2 + (y - cy) ** 2);
          if (dist > r) continue;
          density[y * w + x] += 1 - dist / r;
        }
      }
    }

    const nonZero = density.filter(v => v > 0).sort();
    const cap = nonZero.length ? nonZero[Math.floor(nonZero.length * 0.99)] : 1;

    const layer = document.createElement('canvas');
    layer.width = w;
    layer.height = h;
    const lctx = layer.getContext('2d')!;
    const img = lctx.createImageData(w, h);
    const d = img.data;
    const [cr, cg, cb] = hexToRgb(hex);

    for (let i = 0; i < density.length; i++) {
      const t = Math.min(1, density[i] / cap);
      d[i * 4] = cr;
      d[i * 4 + 1] = cg;
      d[i * 4 + 2] = cb;
      d[i * 4 + 3] = Math.round(Math.pow(t, 0.7) * 230);
    }

    lctx.putImageData(img, 0, 0);
    return layer;
  }
}
