import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AzioniPraticaComponent } from './azioni-pratica.component';

describe('AzioniPraticaComponent', () => {
  let component: AzioniPraticaComponent;
  let fixture: ComponentFixture<AzioniPraticaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AzioniPraticaComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AzioniPraticaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
