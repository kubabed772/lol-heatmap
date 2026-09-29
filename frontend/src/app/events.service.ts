import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HeatmapPoint } from './heatmap-point';

@Injectable({ providedIn: 'root' })
export class EventsService {
  private http = inject(HttpClient);

  getPoints(name: string, tag: string, count: number): Observable<HeatmapPoint[]> {
    const params = new HttpParams()
      .set('name', name)
      .set('tag', tag)
      .set('count', count);

    return this.http.get<HeatmapPoint[]>('/api/events', { params });
  }
}
