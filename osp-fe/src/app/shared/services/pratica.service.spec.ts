import { TestBed } from '@angular/core/testing';

import { PraticaService } from './pratica.service';

describe('PraticaService', () => {
  let service: PraticaService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PraticaService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
