import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from 'app/shared/shared.module';
import { PrimeNGModule } from 'app/shared/modules/primeng.module';
import { PraticheArchiviateComponent } from './pratiche-archiviate.component';



@NgModule({
  declarations: [
    PraticheArchiviateComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    PrimeNGModule
  ], 
  exports: [
    PraticheArchiviateComponent
  ]
})
export class PraticheArchiviateModule { }
