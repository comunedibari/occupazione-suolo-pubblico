import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GestionePraticaComponent } from './gestione-pratica.component';
import { PrimeNGModule } from 'app/shared/modules/primeng.module';
import { SharedModule } from 'app/shared/shared.module';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';



@NgModule({
  declarations: [
    GestionePraticaComponent
  ],
  imports: [
    CommonModule,
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,    
    SharedModule,
    PrimeNGModule
  ],
  exports: [
    GestionePraticaComponent
  ]
})
export class GestionePraticaModule { }
