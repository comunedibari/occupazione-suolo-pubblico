import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from '@core-components/layout/login/login.component';
import { NotFoundComponent } from '@core-components/layout/not-found/not-found.component';
import { UnauthorizedAccessComponent } from '@core-components/layout/unauthorized-access/unauthorized-access.component';
import { ConcessioniValideComponent } from '@features/concessioni-valide/concessioni-valide.component';
import { UoConfigurationComponent } from '@features/configuration/configuration.component';
import { GestionePraticaComponent } from '@features/gestione-pratica/gestione-pratica.component';
import { HomePageComponent } from '@features/home-page/home-page.component';
import { NecessariaIntegrazioneComponent } from '@features/necessaria-integrazione/necessaria-integrazione.component';
import { PraticaBozzaComponent } from '@features/pratica-bozza/pratica-bozza.component';
import { PraticheApprovateComponent } from '@features/pratiche-approvate/pratiche-approvate.component';
import { PraticheArchiviateComponent } from '@features/pratiche-archiviate/pratiche-archiviate.component';
import { PraticheDaRigettareComponent } from '@features/pratiche-da-rigettare/pratiche-da-rigettare.component';
import { PraticheInSospesoComponent } from '@features/pratiche-in-sospeso/pratiche-in-sospeso.component';
import { PratichePronteRilascioComponent } from '@features/pratiche-pronte-rilascio/pratiche-pronte-rilascio.component';
import { PraticheRigettateComponent } from '@features/pratiche-rigettate/pratiche-rigettate.component';
import { PresaInCaricoComponent } from '@features/presa-in-carico/presa-in-carico.component';
import { RicercaPraticheFascicoloComponent } from '@features/ricerca-pratiche-fascicolo/ricerca-pratiche-fascicolo.component';
import { RichiestaPareriComponent } from '@features/richiesta-pareri/richiesta-pareri.component';
import { StatisticheComponent } from '@features/statistiche/statistiche.component';
import { TemplateDocumentiComponent } from '@features/template-documenti/template-documenti.component';
import { UserProfileComponent } from '@features/user-management/components/user-profile/user-profile.component';
import { UserManagementComponent } from '@features/user-management/user-management.component';
import { ValidazionePraticaComponent } from '@features/validazione-pratica/validazione-pratica.component';
import { VerificaRipristinoLuoghiComponent } from '@features/verifica-ripristino-luoghi/verifica-ripristino-luoghi.component';
import { AuthGuard } from '@guards/auth.guard';

const routes: Routes = [
  { path: "home", component: HomePageComponent, data: { breadcrumb: 'Home Page' }, canActivate: [AuthGuard] },
  { path: 'login', component: LoginComponent },
  { path: '', component : LoginComponent},

  //sistema
  { path: 'sistema', 
    data: { breadcrumb: 'Sistema' }, 
    children: [
      { path:'', redirectTo: '/home',pathMatch: 'full'},
      { path: 'usermanagement', component: UserManagementComponent, canActivate: [AuthGuard], data: { breadcrumb: 'Gestione utenti' } },
      { path: 'user', component: UserProfileComponent, canActivate: [AuthGuard], data: { breadcrumb: 'Profilo utente' } },
      { path: 'template_documenti', component: TemplateDocumentiComponent, canActivate: [AuthGuard], data: { breadcrumb: 'Template documenti' } },
      { path: 'configuration', component: UoConfigurationComponent, canActivate: [AuthGuard], data: { breadcrumb: 'Configurazione parametri' } },
      { path: 'statistiche', component: StatisticheComponent, canActivate: [AuthGuard], data: { breadcrumb: 'Statistiche' } }
    ]
  },

  //Gestione Richieste
  { path: 'gestione_richieste', 
    data: { breadcrumb: 'Gestione richieste' }, 
    children: [
      { path:'',  redirectTo: '/home',pathMatch: 'full'},
      { path: 'inserisci_richiesta', component: GestionePraticaComponent, canActivate: [AuthGuard], data: { breadcrumb: 'Inserisci richiesta' } },
      { path: 'pratiche_in_bozza', component: PraticaBozzaComponent, canActivate: [AuthGuard], data: { breadcrumb: 'Pratiche in bozza' } },
      { path: 'concessioni_valide', component: ConcessioniValideComponent, canActivate: [AuthGuard], data: { breadcrumb: 'Concessioni valide' } },
      { path: 'ricerca_pratiche', component: RicercaPraticheFascicoloComponent, canActivate: [AuthGuard], data: { breadcrumb: 'Ricerca pratiche' } },
    ]
  },
  //Pratiche da lavorare
  { path: 'pratiche_da_lavorare', 
    data: { breadcrumb: 'Pratiche da lavorare' }, 
    children: [
      { path:'', redirectTo: '/home',pathMatch: 'full'},
      { path: 'presa_in_carico', component: PresaInCaricoComponent, canActivate: [AuthGuard], data: { breadcrumb: 'Presa in carico/Assegnazione pratica' } },
      { path: 'verifica_pratiche', component: ValidazionePraticaComponent, canActivate: [AuthGuard], data: { breadcrumb: 'Validazione pratiche' } },
      { path: 'richiesta_pareri', component: RichiestaPareriComponent, canActivate: [AuthGuard], data: { breadcrumb: 'Rielaborazione pareri/Esprimi parere' } },
      { path: 'verifica_ripristino_luoghi', component: VerificaRipristinoLuoghiComponent, canActivate: [AuthGuard], data: { breadcrumb: 'Verifica ripristino luoghi' } },
      { path: 'necessaria_integrazione', component: NecessariaIntegrazioneComponent, canActivate: [AuthGuard], data: { breadcrumb: 'Necessarie integrazioni' } },
      { path: 'pratiche_approvate', component: PraticheApprovateComponent, canActivate: [AuthGuard], data: { breadcrumb: 'Pratiche approvate' } },
      { path: 'pratiche_in_sospeso', component: PraticheInSospesoComponent, canActivate: [AuthGuard], data: { breadcrumb: 'Attesa di pagamento' } },
      { path: 'ritiro_rilascio', component: PratichePronteRilascioComponent, canActivate: [AuthGuard], data: { breadcrumb: 'Pronte al rilascio' } },
      { path: 'pratiche_da_rigettare', component: PraticheDaRigettareComponent, canActivate: [AuthGuard], data: { breadcrumb: 'Pratiche da rigettare' } },
    ]
  },
  //Pratiche concluse
  { path: 'pratiche_concluse', 
    data: { breadcrumb: 'Pratiche concluse' }, 
    children: [
      { path:'',  redirectTo: '/home',pathMatch: 'full'},
      { path: 'pratiche_archiviate', component: PraticheArchiviateComponent, canActivate: [AuthGuard], data: { breadcrumb: 'Pratiche archiviate' } },
      { path: 'pratiche_rigettate', component: PraticheRigettateComponent, canActivate: [AuthGuard], data: { breadcrumb: 'Pratiche rigettate' } },
    ]
  },
  { path: 'unauthorized-error', component: UnauthorizedAccessComponent, data: { breadcrumb: 'Non Autorizzato' } },
  { path : '**', component : NotFoundComponent, data: { breadcrumb: 'Non trovata' }}
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, { relativeLinkResolution: 'legacy', scrollPositionRestoration: 'enabled' })
  ],
  exports: [RouterModule]
})
export class AppRoutingModule { }
