import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { CoreModule } from './core/core.module';
import { TranslationsService } from '@services/translation/translations.service';
import { PrimeNGModule } from './shared/modules/primeng.module';
import { ScadenzarioModule } from '@features/scadenzario/scadenzario.module';
import { ReactiveFormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { SharedModule } from './shared/shared.module';
import { MockHttpInterceptor } from '@interceptors/mock-http.interceptor';
import { HomePageModule } from '@features/home-page/home-page.module';
import { ErrorInterceptor } from '@interceptors/error.interceptor';
import { UserManagementModule } from '@features/user-management/user-management.module';
import { SpinnerDialogService } from '@core-components/layout/spinner-dialog/services/spinner-dialog.service';
import { GestionePraticaModule } from '@features/gestione-pratica/gestione-pratica.module';
import { DatePipe } from '@angular/common';
import { PraticaBozzaModule } from '@features/pratica-bozza/pratica-bozza.module';
import { ConcessioniValideModule } from './features/concessioni-valide/concessioni-valide.module';
import { RicercaPraticheFascicoloModule } from './features/ricerca-pratiche-fascicolo/ricerca-pratiche-fascicolo.module';
import { ValidazionePraticaModule } from './features/validazione-pratica/validazione-pratica.module';
import { RichiestaPareriModule } from './features/richiesta-pareri/richiesta-pareri.module';
import { NecessariaIntegrazioneModule } from '@features/necessaria-integrazione/necessaria-integrazione.module';
import { PraticheApprovateModule } from './features/pratiche-approvate/pratiche-approvate.module';
import { PraticheInSospesoModule } from './features/pratiche-in-sospeso/pratiche-in-sospeso.module';
import { PratichePronteRilascioModule } from './features/pratiche-pronte-rilascio/pratiche-pronte-rilascio.module';
import { PraticheDaRigettareModule } from './features/pratiche-da-rigettare/pratiche-da-rigettare.module';
import { PraticheArchiviateModule } from './features/pratiche-archiviate/pratiche-archiviate.module';
import { PraticheRigettateModule } from './features/pratiche-rigettate/pratiche-rigettate.module';
import { PresaInCaricoModule } from './features/presa-in-carico/presa-in-carico.module';
import { TemplateDocumentiModule } from '@features/template-documenti/template-documenti.module';
import { StatisticheModule } from '@features/statistiche/statistiche.module';
import { DettaglioPraticaModule } from '@features/dettaglio-pratica/dettaglio-pratica.module';
import { VerificaRipristinoLuoghiModule } from '@features/verifica-ripristino-luoghi/verifica-ripristino-luoghi.module';
import { UoConfigurationModule } from '@features/configuration/configuration.module';

@NgModule({
  declarations: [
    AppComponent,
  ],
  imports: [
    CoreModule,
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    HttpClientModule,
    ReactiveFormsModule,
    PrimeNGModule,
    SharedModule,
    ScadenzarioModule,
    HomePageModule,
    UserManagementModule,
    GestionePraticaModule,
    PraticaBozzaModule,
    ConcessioniValideModule,
    RicercaPraticheFascicoloModule,
    ValidazionePraticaModule,
    RichiestaPareriModule,
    VerificaRipristinoLuoghiModule,
    NecessariaIntegrazioneModule,
    PraticheApprovateModule,
    PraticheInSospesoModule,
    PratichePronteRilascioModule,
    PraticheDaRigettareModule,
    PraticheArchiviateModule,
    PraticheRigettateModule,
    PresaInCaricoModule,
    TemplateDocumentiModule,
    StatisticheModule,
    DettaglioPraticaModule,
    UoConfigurationModule
  ],
  providers: [
    TranslationsService,
    DatePipe,
    { 
			provide: HTTP_INTERCEPTORS, 
			useClass: MockHttpInterceptor, 
			multi: true,
			deps: []
		},
    {
      provide: HTTP_INTERCEPTORS, 
      useClass: ErrorInterceptor, 
      multi: true,
      deps: [SpinnerDialogService]
    }    
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
