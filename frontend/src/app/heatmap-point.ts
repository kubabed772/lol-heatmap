export type EventType = 'KILL' | 'DEATH' | 'ASSIST';

export interface HeatmapPoint {
  matchId: string;
  champion: string;
  type: EventType;
  x: number;
  y: number;
  timestamp: number;
}
