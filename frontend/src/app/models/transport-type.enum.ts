export const TRANSPORT_TYPES = ['BIKE', 'HIKE', 'RUNNING', 'CAR'] as const;

export type TransportType = typeof TRANSPORT_TYPES[number];
