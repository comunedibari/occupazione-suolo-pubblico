import { TestBed } from '@angular/core/testing';

import { SpinnerDialogService } from './spinner-dialog.service';

describe('SpinnerDialogService', () => {
  let service: SpinnerDialogService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SpinnerDialogService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
