import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GestionePraticaComponent } from './gestione-pratica.component';

describe('GestionePraticaComponent', () => {
  let component: GestionePraticaComponent;
  let fixture: ComponentFixture<GestionePraticaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GestionePraticaComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GestionePraticaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
