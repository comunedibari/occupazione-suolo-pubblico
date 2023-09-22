import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StatisticheComponent } from './statistiche.component';
import { PrimeNGModule } from 'app/shared/modules/primeng.module';
import { SharedModule } from 'app/shared/shared.module';



@NgModule({
  declarations: [
    StatisticheComponent
  ],
  imports: [
    CommonModule,
    PrimeNGModule,
    SharedModule
  ],
  exports: [
    StatisticheComponent
  ]
})
export class StatisticheModule { }
