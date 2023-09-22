import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from 'app/shared/shared.module';
import { PrimeNGModule } from 'app/shared/modules/primeng.module';
import { NecessariaIntegrazioneComponent } from './necessaria-integrazione.component';


@NgModule({
  declarations: [
    NecessariaIntegrazioneComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    PrimeNGModule
  ], 
  exports: [
    NecessariaIntegrazioneComponent
  ]
})
export class NecessariaIntegrazioneModule { }
