import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from 'app/shared/shared.module';
import { PrimeNGModule } from 'app/shared/modules/primeng.module';
import { ValidazionePraticaComponent } from './validazione-pratica.component';


@NgModule({
  declarations: [
    ValidazionePraticaComponent
  ],
  imports: [
    CommonModule,
    PrimeNGModule,
    SharedModule,
  ], 
  exports: [
    ValidazionePraticaComponent
  ]
})
export class ValidazionePraticaModule { }
