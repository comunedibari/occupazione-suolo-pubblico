import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from 'app/shared/shared.module';
import { PrimeNGModule } from 'app/shared/modules/primeng.module';
import { ConcessioniValideComponent } from './concessioni-valide.component';


@NgModule({
  declarations: [
    ConcessioniValideComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    PrimeNGModule
  ],
  exports: [
    ConcessioniValideComponent
  ]
})
export class ConcessioniValideModule { }
