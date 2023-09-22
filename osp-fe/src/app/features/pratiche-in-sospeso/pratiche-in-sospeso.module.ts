import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from 'app/shared/shared.module';
import { PrimeNGModule } from 'app/shared/modules/primeng.module';
import { PraticheInSospesoComponent } from './pratiche-in-sospeso.component';


@NgModule({
  declarations: [
    PraticheInSospesoComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    PrimeNGModule
  ], 
  exports: [
    PraticheInSospesoComponent
  ]
})
export class PraticheInSospesoModule { }
