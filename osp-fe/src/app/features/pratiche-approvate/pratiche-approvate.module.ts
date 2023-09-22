import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from 'app/shared/shared.module';
import { PrimeNGModule } from 'app/shared/modules/primeng.module';
import { PraticheApprovateComponent } from './pratiche-approvate.component';


@NgModule({
  declarations: [
    PraticheApprovateComponent
  ],
  imports: [
    CommonModule,
    PrimeNGModule,
    SharedModule,
  ], 
  exports: [
    PraticheApprovateComponent
  ]

})
export class PraticheApprovateModule { }
