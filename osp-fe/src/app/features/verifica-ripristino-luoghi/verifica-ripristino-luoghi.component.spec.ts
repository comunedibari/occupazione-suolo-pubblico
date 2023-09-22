import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VerificaRipristinoLuoghiComponent } from './verifica-ripristino-luoghi.component';

describe('VerificaRipristinoLuoghiComponent', () => {
  let component: VerificaRipristinoLuoghiComponent;
  let fixture: ComponentFixture<VerificaRipristinoLuoghiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VerificaRipristinoLuoghiComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VerificaRipristinoLuoghiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
