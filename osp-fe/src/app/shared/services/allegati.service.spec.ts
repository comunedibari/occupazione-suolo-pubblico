import { TestBed } from '@angular/core/testing';

import { AllegatiService } from './allegati.service';

describe('AllegatiService', () => {
  let service: AllegatiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AllegatiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
