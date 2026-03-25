export const DIFFICULTY_LEVELS = ['EASY', 'MODERATE', 'HARD'] as const;

export type Difficulty = typeof DIFFICULTY_LEVELS[number];
