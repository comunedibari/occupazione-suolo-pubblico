import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RiferimentiPraticaComponent } from './riferimenti-pratica.component';

describe('RiferimentiPraticaComponent', () => {
  let component: RiferimentiPraticaComponent;
  let fixture: ComponentFixture<RiferimentiPraticaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RiferimentiPraticaComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RiferimentiPraticaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
