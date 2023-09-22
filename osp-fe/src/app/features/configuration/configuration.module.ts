import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimeNGModule } from 'app/shared/modules/primeng.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from 'app/shared/shared.module';
import { BrowserModule } from '@angular/platform-browser';
import { UoConfigurationComponent } from './configuration.component';



@NgModule({
  declarations: [
    UoConfigurationComponent,
  ],
  imports: [
    BrowserModule,
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    PrimeNGModule,
    SharedModule
  ],
  exports: [
    UoConfigurationComponent
  ]
})
export class UoConfigurationModule { }
