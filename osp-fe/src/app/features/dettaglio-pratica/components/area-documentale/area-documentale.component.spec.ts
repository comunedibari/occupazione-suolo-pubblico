import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AreaDocumentaleComponent } from './area-documentale.component';

describe('AreaDocumentaleComponent', () => {
  let component: AreaDocumentaleComponent;
  let fixture: ComponentFixture<AreaDocumentaleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AreaDocumentaleComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AreaDocumentaleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
