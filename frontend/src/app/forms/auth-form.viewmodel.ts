import { Injectable, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { LoginPayload, RegisterPayload } from '../models/user.model';

@Injectable()
export class AuthFormViewModel {
  private readonly fb = inject(FormBuilder);

  readonly loginForm = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  readonly registerForm = this.fb.nonNullable.group({
    displayName: ['', [Validators.required, Validators.minLength(2)]],
    username: ['', [Validators.required, Validators.minLength(3)]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  loginPayload(): LoginPayload {
    return this.loginForm.getRawValue();
  }

  registerPayload(): RegisterPayload {
    return this.registerForm.getRawValue();
  }
}
