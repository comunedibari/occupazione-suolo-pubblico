import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InformazioniAggiuntiveComponent } from './informazioni-aggiuntive.component';

describe('InformazioniAggiuntiveComponent', () => {
  let component: InformazioniAggiuntiveComponent;
  let fixture: ComponentFixture<InformazioniAggiuntiveComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ InformazioniAggiuntiveComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(InformazioniAggiuntiveComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
