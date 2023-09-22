import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PraticaBozzaComponent } from './pratica-bozza.component';

describe('PraticaBozzaComponent', () => {
  let component: PraticaBozzaComponent;
  let fixture: ComponentFixture<PraticaBozzaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PraticaBozzaComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PraticaBozzaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
