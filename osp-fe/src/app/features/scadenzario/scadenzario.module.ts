import { NgModule } from '@angular/core';
import { ScadenzarioComponent } from './scadenzario.component';
import { PrimeNGModule } from 'app/shared/modules/primeng.module';
import { SharedModule } from 'app/shared/shared.module';



@NgModule({
  declarations: [
    ScadenzarioComponent
  ],
  imports: [
    PrimeNGModule,
    SharedModule
  ],
  exports: [
    ScadenzarioComponent
  ]
})
export class ScadenzarioModule { }
