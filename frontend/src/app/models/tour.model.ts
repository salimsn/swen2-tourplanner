export interface Tour {
  id: number;
  name: string;
  description: string;
  fromLocation: string;
  toLocation: string;
  distance: number;
  estimatedTime: number;
  imageUrl: string;
}
