import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { SharedModule } from 'app/shared/shared.module';
import { PrimeNGModule } from 'app/shared/modules/primeng.module';
import { VerificaRipristinoLuoghiComponent } from './verifica-ripristino-luoghi.component';



@NgModule({
  declarations: [
    VerificaRipristinoLuoghiComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    PrimeNGModule,
    ReactiveFormsModule
  ], 
  exports: [
    VerificaRipristinoLuoghiComponent
  ]
})
export class VerificaRipristinoLuoghiModule { }