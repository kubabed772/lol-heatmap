import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {HeatmapPoint, ImportStatus} from './heatmap-point';

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

  startImport(name: string, tag: string, count: number): Observable<ImportStatus> {
    const params = new HttpParams()
      .set('name', name)
      .set('tag', tag)
      .set('count', count);

    return this.http.post<ImportStatus>('/api/import', null, { params });
  }

  getImportStatus(puuid: string): Observable<ImportStatus> {
    return this.http.get<ImportStatus>(`/api/import/${puuid}`);
  }
}
