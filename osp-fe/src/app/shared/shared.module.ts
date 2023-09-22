import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormatDatePipe } from './pipes/format-date.pipe';
import { HttpClientModule } from '@angular/common/http';
import { TablePrimeNGComponent } from './components/table-prime-ng/table-prime-ng.component';
import { PrimeNGModule } from './modules/primeng.module';
import { UploadFileComponent } from './components/upload-file/upload-file.component';
import { EmptyValuePipe } from '@pipes/empty-value.pipe';
import { AzioniPraticaComponent } from './components/azioni-pratica/azioni-pratica.component';
import { MappaComponent } from './components/mappa/mappa.component';
import { KeysPipe } from '@pipes/keys.pipe';
import { SafeHtmlPipe } from '@pipes/safeHtml.pipe';
import { HelpComponent } from '@shared-components/help/help.component';
import { MinMaxDirective } from './components/directives/min-max.directive';

@NgModule({
  declarations: [
    FormatDatePipe,
    EmptyValuePipe,
    KeysPipe,
    SafeHtmlPipe,
    TablePrimeNGComponent,
    UploadFileComponent,
    AzioniPraticaComponent,
    HelpComponent,
    MappaComponent,
    MinMaxDirective
  ],
  imports: [
    CommonModule,
    BrowserAnimationsModule,
    HttpClientModule,
    ReactiveFormsModule,
    PrimeNGModule
  ],
  exports: [
    FormatDatePipe,
    KeysPipe,
    SafeHtmlPipe,
    TablePrimeNGComponent,
    UploadFileComponent,
    AzioniPraticaComponent,
    HelpComponent,
    MappaComponent,
    MinMaxDirective
  ],
  providers: [
    FormatDatePipe,
  ]
})
export class SharedModule { }
