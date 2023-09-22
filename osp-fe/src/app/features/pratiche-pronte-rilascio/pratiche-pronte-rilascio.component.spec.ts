import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PratichePronteRilascioComponent } from './pratiche-pronte-rilascio.component';

describe('PratichePronteRilascioComponent', () => {
  let component: PratichePronteRilascioComponent;
  let fixture: ComponentFixture<PratichePronteRilascioComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PratichePronteRilascioComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PratichePronteRilascioComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
