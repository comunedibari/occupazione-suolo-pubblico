import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TemplateDocumentiComponent } from './template-documenti.component';
import { PrimeNGModule } from 'app/shared/modules/primeng.module';
import { SharedModule } from 'app/shared/shared.module';



@NgModule({
  declarations: [TemplateDocumentiComponent],
  imports: [
    CommonModule,
    PrimeNGModule,
    SharedModule
  ],
  exports: [TemplateDocumentiComponent]
})
export class TemplateDocumentiModule { }
