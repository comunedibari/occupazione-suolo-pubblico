import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StoricoPraticaComponent } from './storico-pratica.component';

describe('StoricoPraticaComponent', () => {
  let component: StoricoPraticaComponent;
  let fixture: ComponentFixture<StoricoPraticaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ StoricoPraticaComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(StoricoPraticaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
