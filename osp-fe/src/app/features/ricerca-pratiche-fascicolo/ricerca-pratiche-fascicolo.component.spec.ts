import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RicercaPraticheFascicoloComponent } from './ricerca-pratiche-fascicolo.component';

describe('RicercaPraticheFascicoloComponent', () => {
  let component: RicercaPraticheFascicoloComponent;
  let fixture: ComponentFixture<RicercaPraticheFascicoloComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RicercaPraticheFascicoloComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RicercaPraticheFascicoloComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
