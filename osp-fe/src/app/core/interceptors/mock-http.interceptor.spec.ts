import { TestBed } from '@angular/core/testing';

import { MockHttpInterceptor } from './mock-http.interceptor';

describe('MockHttpInterceptor', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [
      MockHttpInterceptor
      ]
  }));

  it('should be created', () => {
    const interceptor: MockHttpInterceptor = TestBed.inject(MockHttpInterceptor);
    expect(interceptor).toBeTruthy();
  });
});
