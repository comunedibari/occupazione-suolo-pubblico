import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from 'app/shared/shared.module';
import { PrimeNGModule } from 'app/shared/modules/primeng.module';
import { PraticaBozzaComponent } from './pratica-bozza.component';


@NgModule({
  declarations: [
    PraticaBozzaComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    PrimeNGModule
  ], 
  exports: [
    PraticaBozzaComponent
  ]

})
export class PraticaBozzaModule { }