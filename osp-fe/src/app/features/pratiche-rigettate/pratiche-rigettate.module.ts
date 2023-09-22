import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from 'app/shared/shared.module';
import { PrimeNGModule } from 'app/shared/modules/primeng.module';
import { PraticheRigettateComponent } from './pratiche-rigettate.component';



@NgModule({
  declarations: [
    PraticheRigettateComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    PrimeNGModule
  ], 
  exports: [
    PraticheRigettateComponent
  ]
})
export class PraticheRigettateModule { }
