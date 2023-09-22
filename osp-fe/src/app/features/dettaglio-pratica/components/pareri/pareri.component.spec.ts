import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PareriComponent } from './pareri.component';

describe('PareriComponent', () => {
  let component: PareriComponent;
  let fixture: ComponentFixture<PareriComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PareriComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PareriComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
