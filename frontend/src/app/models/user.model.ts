export interface AuthenticatedUser {
  id: string;
  username: string;
  displayName: string;
}

export interface LoginPayload {
  username: string;
  password: string;
}

export interface RegisterPayload extends LoginPayload {
  displayName: string;
}
