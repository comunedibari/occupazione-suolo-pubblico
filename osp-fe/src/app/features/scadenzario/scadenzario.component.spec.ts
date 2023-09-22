import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScadenzarioComponent } from './scadenzario.component';

describe('ScadenzarioComponent', () => {
  let component: ScadenzarioComponent;
  let fixture: ComponentFixture<ScadenzarioComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ScadenzarioComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScadenzarioComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
