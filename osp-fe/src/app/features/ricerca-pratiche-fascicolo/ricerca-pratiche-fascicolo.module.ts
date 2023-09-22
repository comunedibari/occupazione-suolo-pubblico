import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from 'app/shared/shared.module';
import { PrimeNGModule } from 'app/shared/modules/primeng.module';
import { RicercaPraticheFascicoloComponent } from './ricerca-pratiche-fascicolo.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';


@NgModule({
  declarations: [
    RicercaPraticheFascicoloComponent
  ],
  imports: [
    CommonModule,
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    PrimeNGModule,
    SharedModule,
  ],  
  exports: [
    RicercaPraticheFascicoloComponent
  ]
})
export class RicercaPraticheFascicoloModule { }
