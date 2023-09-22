import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TemplateDocumentiComponent } from './template-documenti.component';

describe('TemplateDocumentiComponent', () => {
  let component: TemplateDocumentiComponent;
  let fixture: ComponentFixture<TemplateDocumentiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TemplateDocumentiComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateDocumentiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
