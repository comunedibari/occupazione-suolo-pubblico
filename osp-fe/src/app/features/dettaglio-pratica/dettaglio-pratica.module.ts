import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DettaglioPraticaComponent } from './dettaglio-pratica.component';
import { PrimeNGModule } from 'app/shared/modules/primeng.module';
import { ReactiveFormsModule } from '@angular/forms';
import { GestionePraticaModule } from '@features/gestione-pratica/gestione-pratica.module';
import { InformazioniAggiuntiveComponent } from './components/informazioni-aggiuntive/informazioni-aggiuntive.component';
import { PareriComponent } from './components/pareri/pareri.component';
import { RiferimentiPraticaComponent } from './components/riferimenti-pratica/riferimenti-pratica.component';
import { AreaDocumentaleComponent } from './components/area-documentale/area-documentale.component';
import { SharedModule } from 'app/shared/shared.module';
import { StoricoPraticaComponent } from './components/storico-pratica/storico-pratica.component';



@NgModule({
  declarations: [
    DettaglioPraticaComponent,
    InformazioniAggiuntiveComponent,
    PareriComponent,
    RiferimentiPraticaComponent,
    AreaDocumentaleComponent,
    StoricoPraticaComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    PrimeNGModule,
    SharedModule,
    GestionePraticaModule
  ],
  exports: [
    DettaglioPraticaComponent
  ]
})
export class DettaglioPraticaModule { }
