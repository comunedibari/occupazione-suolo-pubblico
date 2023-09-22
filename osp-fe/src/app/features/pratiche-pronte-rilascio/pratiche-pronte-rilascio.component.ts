import { Component, OnInit } from '@angular/core';
import { environment } from 'environments/environment';
import { TableEvent } from '@shared-components/table-prime-ng/models/table-event';
import { PraticaDto } from '@models/dto/pratica-dto';
import { SpinnerDialogService } from '@core-components/layout/spinner-dialog/services/spinner-dialog.service';
import { RicercaPraticaRequest } from '@models/ricerca-pratica-request';
import { PraticaService } from '@services/pratica.service';
import { UtilityService } from '@services/utility.service';
import { MessageService } from '@services/message.service';
import { DialogService } from 'primeng/dynamicdialog';
import { AuthService } from '@services/auth/auth.service';
import { Action } from '@shared-components/azioni-pratica/models/action';
import { BasePraticaAction } from 'app/shared/classes/base-pratica-action';

@Component({
  selector: 'app-pratiche-pronte-rilascio',
  templateUrl: './pratiche-pronte-rilascio.component.html',
  styleUrls: ['./pratiche-pronte-rilascio.component.css']
})
export class PratichePronteRilascioComponent extends BasePraticaAction implements OnInit {

  constructor(
    private spinnerService: SpinnerDialogService,
    private praticaService: PraticaService,
    private messageService: MessageService,
    protected dialogService: DialogService,
    protected utilityService: UtilityService,
    private authService: AuthService,
  ) {
    super(dialogService, utilityService);
  }

  pratica: PraticaDto;
  showRilasciaAttoDialog: boolean = false;

  dataSource: PraticaDto[];

  initSortColumn = 'dataModifica';
  directionSortColumn = "1"; //1=asc  0=rand   -1=desc
  titleTable = 'Pronte al rilascio';
  exportName = 'Pronte al rilascio';
  title = 'Pronte al rilascio';
  globalFilters: any[] = [
    {value:'firmatario.codiceFiscalePartitaIva',label:'Cod. Fiscale/P. IVA'},
    {value:'datiRichiesta.ubicazioneOccupazione',label:'Indirizzo'},
    {value:'tipoProcesso.descrizione',label:'Tip. Processo'},
    {value:'utentePresaInCarico.username',label:'Istruttore'}
  ];

  columnSchema = [
    {
      field: "firmatario.codiceFiscalePartitaIva",
      header: "Cod. Fiscale/P. IVA",
      type: "text"
    },
    {
      field: "datiRichiesta.ubicazioneOccupazione",
      header: "Indirizzo",
      inactive: true,
      type: "text"
    },
    {
      field: 'protocollo',
      header: 'N. Protocollo',
      type: 'text',
      /*show: (el) => {
        return UtilityService.getProtocolloInserimento(el);
      },*/
      customSortFunction: this.utilityService.protocolSortFunction
    },
    {
      field: "tipoProcesso.descrizione",
      header: "Tip. Processo",
      type: "dropdown",
    },
    {
      field: "utentePresaInCarico.username",
      header: "Istruttore",
      type: "text",
    },
    {
      field: "statoPratica.descrizione",
      header: "Stato",
      type: "dropdown",
    },
    {
      field: "dataModifica",
      header: "Data operazione",
      type: "date",
      pipe: "date"
    }
  ];

  actions: Action[] = [
    {
      key: 'dettaglioPratica',
      icon: "pi pi-search",
      tooltip: 'DETTAGLIO',
    },
    {
      key: 'rilasciaAttoDialog',
      icon: "pi pi-arrow-right",
      tooltip: 'RILASCIA ATTO CONCESSORIO',
      hidden: (el) => {
        return this.authService.getGroup() != environment.groups.idGruppoIstruttoreMunicipio;
      }
    },
  ];

  ngOnInit(): void {
    this.cercaPratiche();
  }

  cercaPratiche(){
    let pratica = new RicercaPraticaRequest();
    pratica.idsMunicipi = this.authService.getMunicipiAppartenenza();
    pratica.idsStatiPratica = [environment.statiPratica.prontaRilascio];

    this.spinnerService.showSpinner(true);
    this.praticaService.getPratiche(pratica, this.authService.getLoggedUser()).subscribe(
      data => {
        this.spinnerService.showSpinner(false);
        this.dataSource = data.content.map(el => Object.setPrototypeOf(el, PraticaDto.prototype));
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage('Ricerca pratiche', err);
      });
  }

  rilasciaAttoDialog(element: any) {
    this.pratica = element;
    this.showRilasciaAttoDialog = true;
  }

  statoConcessioneValida(){
    this.spinnerService.showSpinner(true);
    this.praticaService.statoConcessioneValida(this.pratica.id, this.authService.getLoggedUser().userLogged.id).subscribe(
      data => {
        this.spinnerService.showSpinner(false);
        this.showRilasciaAttoDialog = false;
        this.messageService.showMessage('success', 'Aggiornamento pratica', 'La pratica è passata in stato Concessione Valida');
        this.cercaPratiche();
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage('Aggiornamento pratica', err);
      });
  }

}
