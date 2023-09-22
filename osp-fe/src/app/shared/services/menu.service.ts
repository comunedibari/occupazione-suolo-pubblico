import { Injectable } from '@angular/core';
import { Group } from '@enums/Group.enum';
import { StatoPratica } from '@enums/StatoPratica.enum';
import { RicercaPraticaRequest } from '@models/ricerca-pratica-request';
import { MenuItem } from 'primeng/api';
import { environment } from 'environments/environment';
import { AuthService } from './auth/auth.service';
import { PraticaService } from './pratica.service';

@Injectable({
  providedIn: 'root'
})
export class MenuService {

  constructor(
    private authService: AuthService,
    private praticaService: PraticaService
  ) { }

  async getAppMenu(): Promise<MenuItem[]> {
    const countPerStato = await Object.keys(environment.statiPratica).reduce(async (prev, next) => {
      const prevObj = await prev;
      const count = await this.praticaService.getCountPratiche(environment.statiPratica[next], this.authService.getMunicipiAppartenenza()).toPromise();
      return {
        [next]: count,
        ...prevObj
      };
    }, Promise.resolve({}));

    return [
      {
        label: 'Gestione richieste',
        icon:'pi pi-fw pi-list',
        visible: this.authService.isActiveLinkGestioneRichieste(),
        items: [
          {
              label: 'Inserisci pratica',
              title: 'Inserisci nuova pratica',
              icon:'pi pi-fw pi-plus',
              routerLink: '/gestione_richieste/inserisci_richiesta',
              visible: this.authService.checkGroups(AuthService.AUTH_INSERISCI_RICHIESTA)
          },
          {
            label: 'Pratiche in bozza', 
            title: 'Completa pratica in bozza',
            icon:'pi pi-fw pi-pencil', 
            badge: countPerStato['bozza'],
            routerLink: '/gestione_richieste/pratiche_in_bozza',
            visible: this.authService.checkGroups(AuthService.AUTH_INSERISCI_RICHIESTA)
          },
          {
              label: 'Concessioni valide',
              title: 'Elenco concessioni valide con atto concessorio rilasciato',
              icon:'pi pi-fw pi-check',
              badge: countPerStato['concessioneValida'],
              routerLink: '/gestione_richieste/concessioni_valide',
              visible: this.authService.checkGroups(AuthService.AUTH_CONCESSIONI_VALIDE)
          },
          {
              label: 'Ricerca pratiche',
              title: 'Ricerca pratiche',
              icon:'pi pi-fw pi-search',
              routerLink: '/gestione_richieste/ricerca_pratiche',
              visible: this.authService.checkGroups(AuthService.AUTH_FASCICOLO)
          }
        ]
      },
      { 
        label: 'Pratiche da lavorare',
        icon:'pi pi-fw pi-lock-open',
        visible: this.authService.isActiveLinkPraticheDaLavorare(),
        items: [
/*           {
            label: 'Inserita',
            icon:'pi pi-fw pi-file-o',
            routerLink: '/pratiche_da_lavorare/pratiche_inserite',
            visible: this.authService.checkGroups(AuthService.AUTH_INSERITA)
          }, */
          {
              label: this.authService.getGroup() == Group.IstruttoreMunicipio || this.authService.getGroup() == Group.OperatoreSportello ? 'Presa in carico' : 'Assegnazione pratica',
              title: this.authService.getGroup() == Group.IstruttoreMunicipio || this.authService.getGroup() == Group.OperatoreSportello ? 'Prendi in carico': 'Assegna pratica',
              icon:'pi pi-fw pi-paperclip',
              badge: countPerStato['inserita'],
              routerLink: '/pratiche_da_lavorare/presa_in_carico',
              visible: this.authService.checkGroups(AuthService.AUTH_INSERITA)
          },
          {
            label: 'Validazione pratiche',
            title: 'Verifica formale e richiedi pareri/integrazione',
            icon:'pi pi-fw pi-check-circle',
            badge: countPerStato['verificaFormale'],
            routerLink: '/pratiche_da_lavorare/verifica_pratiche',
            visible: this.authService.checkGroups(AuthService.AUTH_VERIFICA_PRATICHE)
          },
          {
            label: this.authService.getGroup() == Group.IstruttoreMunicipio || this.authService.getGroup() == Group.DirettoreMunicipio ? 'Rielaborazione pareri' : 'Esprimi parere',
            title: this.authService.getGroup() == Group.IstruttoreMunicipio || this.authService.getGroup() == Group.DirettoreMunicipio ? 'Rielabora pareri ricevuti e approva/rigetta' : 'Rilascia parere',
            icon:'pi pi-fw pi-comments',
            badge: countPerStato['richiestaPareri'],
            routerLink: '/pratiche_da_lavorare/richiesta_pareri',
            visible: this.authService.checkGroups(AuthService.AUTH_RICHIESTA_PARERI)
          },
          {
            label: 'Verifica ripristino luoghi',
            title: 'Rilascia parere sul ripristino luoghi dopo scadenza, rinuncia, revoca, annullamento o decadenza',
            icon:'pi pi-fw pi-map',
            badge:
              countPerStato['rinunciata'] +
              countPerStato['revocata'] +
              countPerStato['annullata'] +
              countPerStato['terminata'],
            routerLink: '/pratiche_da_lavorare/verifica_ripristino_luoghi',
            visible: this.authService.checkGroups(AuthService.AUTH_VERIFICA_RIPRISTINO_LUOGHI)
          },
          {
            label: 'Necessarie integrazioni',
            title: 'Elenco pratiche in stato: Necessaria Integrazione/Preavviso diniego',
            icon:'pi pi-fw pi-window-minimize',
            badge: countPerStato['necessariaIntegrazione'] +
                  countPerStato['preavvisoDiniego'] +
                  countPerStato['rettificaDate'],
            routerLink: '/pratiche_da_lavorare/necessaria_integrazione',
            visible: this.authService.checkGroups(AuthService.AUTH_NECESSARIA_INTEGRAZIONE)
          },
          {
            label: 'Approvate',
            title: 'Inserisci Determina esecutiva',
            icon:'pi pi-fw pi-check',
            badge: countPerStato['approvata'],
            routerLink: '/pratiche_da_lavorare/pratiche_approvate',
            visible: this.authService.checkGroups(AuthService.AUTH_PRATICHE_APPROVATE)
          },
          {
            label: 'Attesa di pagamento',
            title: 'Elenco pratiche in attesa del pagamento dei tributi',
            icon:'pi pi-fw pi-calendar-times',
            badge: countPerStato['attesaPagamento'],
            routerLink: '/pratiche_da_lavorare/pratiche_in_sospeso',
            visible: this.authService.checkGroups(AuthService.AUTH_PRATICHE_ATTESA_PAGAMENTO)
          },
          {
            label: 'Rilascio',
            title: 'Rilascia atto concessorio',
            icon:'pi pi-fw pi-sign-in',
            badge: countPerStato['prontaRilascio'],
            routerLink: '/pratiche_da_lavorare/ritiro_rilascio',
            visible: this.authService.checkGroups(AuthService.AUTH_PRATICHE_PRONTE_RILASCIO)
          },
          {
            label: 'Rigetto',
            title: 'Inserisci determina esecutiva di Rigetto',
            icon:'pi pi-fw pi-times-circle',
            badge: countPerStato['praticaDaRigettare'],
            routerLink: '/pratiche_da_lavorare/pratiche_da_rigettare',
            visible: this.authService.checkGroups(AuthService.AUTH_PRATICHE_DA_RIGETTARE)
          }
        ]
      },
      {
        label: 'Pratiche concluse',
        icon:'pi pi-fw pi-lock',
        visible: this.authService.isActiveLinkPraticheConcluse(),
        items: [
          {
              label: 'Pratiche archiviate',
              title: 'Elenco pratiche archiviate',
              icon:'pi pi-fw pi-inbox',
              badge: 
                countPerStato['archiviata'] +
                countPerStato['rinunciata'] +
                countPerStato['revocata'] +
                countPerStato['annullata'] +
                countPerStato['terminata'],
              routerLink: '/pratiche_concluse/pratiche_archiviate',
              visible: this.authService.checkGroups(AuthService.AUTH_PRATICHE_ARCHIVIATE)
          },
          {
              label: 'Pratiche rigettate',
              title: 'Elenco pratiche rigettate',
              icon:'pi pi-fw pi-trash',
              badge: countPerStato['rigettata'],
              routerLink: '/pratiche_concluse/pratiche_rigettate',
              visible: this.authService.checkGroups(AuthService.AUTH_PRATICHE_RIGETTATE)
          }
        ]
      },
      { 
        label: 'Sistema',
        icon:'pi pi-fw pi-cog',
        visible: this.authService.isActiveLinksSistema(),
        items: [
          {
            label: 'Gestione utenti',
            title: 'Aggiungi, modifica, elimina e disabilita utenti',
            icon:'pi pi-fw pi-users',
            routerLink: '/sistema/usermanagement',
            visible: this.authService.checkGroups(AuthService.AUTH_GESTIONE_UTENTI)
          },
          {
            label: 'Profilo utente',
            title: 'Modifica dati anagrafici e cambia password',
            icon:'pi pi-fw pi-user',
            routerLink: '/sistema/user',
            visible: this.authService.checkGroups(AuthService.AUTH_IL_MIO_PROFILO)
          },
          {
            label: 'Template documenti',
            title: 'Aggiorna template documentali',
            icon:'pi pi-fw pi-file-o',
            routerLink: '/sistema/template_documenti',
            visible: this.authService.checkGroups(AuthService.AUTH_GESTIONE_UTENTI)
          },
          {
            label: 'Configurazione parametri',
            title: 'Configura utenti automi per il protocollo',
            icon:'pi pi-fw pi-sliders-h',
            routerLink: '/sistema/configuration',
            visible: this.authService.checkGroups(AuthService.AUTH_GESTIONE_UTENTI)
          },
          {
            label: 'Statistiche',
            title: 'Visualizza statistiche dei processi di occupazione suolo pubblico',
            icon:'pi pi-fw pi-chart-line',
            routerLink: '/sistema/statistiche',
            visible: this.authService.checkGroups(AuthService.AUTH_GESTIONE_UTENTI) || this.authService.checkGroups(AuthService.AUTH_VERIFICA_PRATICHE)
          },
        ]
      }
    ];
  }

}
