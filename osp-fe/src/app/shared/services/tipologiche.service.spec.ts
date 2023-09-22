import { TestBed } from '@angular/core/testing';

import { TipologicheService } from './tipologiche.service';

describe('TipologicheService', () => {
  let service: TipologicheService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TipologicheService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
