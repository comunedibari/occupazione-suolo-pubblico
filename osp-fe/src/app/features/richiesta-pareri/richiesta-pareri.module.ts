import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { SharedModule } from 'app/shared/shared.module';
import { PrimeNGModule } from 'app/shared/modules/primeng.module';
import { RichiestaPareriComponent } from './richiesta-pareri.component';


@NgModule({
  declarations: [
    RichiestaPareriComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    PrimeNGModule,
    ReactiveFormsModule
  ], 
  exports: [
    RichiestaPareriComponent
  ]
})
export class RichiestaPareriModule { }