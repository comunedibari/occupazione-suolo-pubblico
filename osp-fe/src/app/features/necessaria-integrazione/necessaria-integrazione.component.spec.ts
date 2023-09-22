import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NecessariaIntegrazioneComponent } from './necessaria-integrazione.component';

describe('NecessariaIntegrazioneComponent', () => {
  let component: NecessariaIntegrazioneComponent;
  let fixture: ComponentFixture<NecessariaIntegrazioneComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NecessariaIntegrazioneComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NecessariaIntegrazioneComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
