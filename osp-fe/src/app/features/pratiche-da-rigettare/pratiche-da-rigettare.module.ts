import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from 'app/shared/shared.module';
import { PrimeNGModule } from 'app/shared/modules/primeng.module';
import { PraticheDaRigettareComponent } from './pratiche-da-rigettare.component';


@NgModule({
  declarations: [
    PraticheDaRigettareComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    PrimeNGModule
  ], 
  exports: [
    PraticheDaRigettareComponent
  ]
})
export class PraticheDaRigettareModule { }
