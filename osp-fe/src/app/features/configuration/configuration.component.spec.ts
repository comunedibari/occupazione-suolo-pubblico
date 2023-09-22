import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UoConfigurationComponent } from './configuration.component';

describe('UoConfigurationComponent', () => {
  let component: UoConfigurationComponent;
  let fixture: ComponentFixture<UoConfigurationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UoConfigurationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UoConfigurationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
