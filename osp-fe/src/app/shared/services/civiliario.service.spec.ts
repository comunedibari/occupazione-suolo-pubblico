import { TestBed } from '@angular/core/testing';

import { CiviliarioService } from './civiliario.service';

describe('CiviliarioService', () => {
  let service: CiviliarioService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CiviliarioService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
