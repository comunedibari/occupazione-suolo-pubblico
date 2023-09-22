import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PraticheInSospesoComponent } from './pratiche-in-sospeso.component';

describe('PraticheInSospesoComponent', () => {
  let component: PraticheInSospesoComponent;
  let fixture: ComponentFixture<PraticheInSospesoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PraticheInSospesoComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PraticheInSospesoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
