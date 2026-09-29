export type EventType = 'KILL' | 'DEATH' | 'ASSIST';

export interface HeatmapPoint {
  matchId: string;
  champion: string;
  type: EventType;
  x: number;
  y: number;
  timestamp: number;
}

export type ImportState = 'QUEUED' | 'RUNNING' | 'WAITING' | 'DONE' | 'FAILED';

export interface ImportStatus {
  puuid: string;
  state: ImportState;
  total: number;
  done: number;
  retryInSeconds: number;
}
