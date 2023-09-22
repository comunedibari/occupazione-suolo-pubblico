import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from 'app/shared/shared.module';
import { PrimeNGModule } from 'app/shared/modules/primeng.module';
import { PresaInCaricoComponent } from './presa-in-carico.component';


@NgModule({
  declarations: [
    PresaInCaricoComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    PrimeNGModule
  ], 
  exports: [
    PresaInCaricoComponent
  ]
})
export class PresaInCaricoModule { }
