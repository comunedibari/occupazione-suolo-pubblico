import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from 'app/shared/shared.module';
import { PrimeNGModule } from 'app/shared/modules/primeng.module';
import { PratichePronteRilascioComponent } from './pratiche-pronte-rilascio.component';



@NgModule({
  declarations: [
    PratichePronteRilascioComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    PrimeNGModule
  ], 
  exports: [
    PratichePronteRilascioComponent
  ]
})
export class PratichePronteRilascioModule { }
