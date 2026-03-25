export const TRANSPORT_TYPES = ['BIKE', 'HIKE', 'RUNNING', 'CAR', 'TRAIN', 'PLANE'] as const;

export type TransportType = typeof TRANSPORT_TYPES[number];
