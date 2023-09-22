import { TestBed } from '@angular/core/testing';

import { ScadenzarioService } from './scadenzario.service';

describe('ScadenzarioService', () => {
  let service: ScadenzarioService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ScadenzarioService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
