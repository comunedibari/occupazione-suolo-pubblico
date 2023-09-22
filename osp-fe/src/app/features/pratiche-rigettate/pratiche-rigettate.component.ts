import { Component, OnInit } from '@angular/core';
import { TableEvent } from '@shared-components/table-prime-ng/models/table-event';
import { PraticaDto } from '@models/dto/pratica-dto';
import { DialogService } from 'primeng/dynamicdialog';
import { SpinnerDialogService } from '@core-components/layout/spinner-dialog/services/spinner-dialog.service';
import { UtilityService } from '@services/utility.service';
import { PraticaService } from '@services/pratica.service';
import { MessageService } from '@services/message.service';
import { RicercaPraticaRequest } from '@models/ricerca-pratica-request';
import { environment } from 'environments/environment';
import { ColumnSchema } from '@shared-components/table-prime-ng/models/column-schema';
import { AuthService } from '@services/auth/auth.service';
import { Action } from '@shared-components/azioni-pratica/models/action';
import { BasePraticaAction } from 'app/shared/classes/base-pratica-action';


@Component({
  selector: 'app-pratiche-rigettate',
  templateUrl: './pratiche-rigettate.component.html',
  styleUrls: ['./pratiche-rigettate.component.css']
})
export class PraticheRigettateComponent extends BasePraticaAction implements OnInit {

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
  dataSource: PraticaDto[];

  initSortColumn = 'dataModifica';
  directionSortColumn = "1"; //1=asc  0=rand   -1=desc
  titleTable = 'Pratiche rigettate';
  exportName = 'Pratiche rigettate';
  title = 'Pratiche rigettate';
  globalFilters: any[] = [
    {value:'firmatario.codiceFiscalePartitaIva',label:'Cod. Fiscale/P. IVA'},
    {value:'datiRichiesta.ubicazioneOccupazione',label:'Indirizzo'},
    {value:'tipoProcesso.descrizione',label:'Tip. Processo'},
    {value:'utentePresaInCarico.username',label:'Istruttore'}
  ];

  columnSchema: ColumnSchema[] = [
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
      field: "municipio.id",
      header: "N. Municipio",
      type: "dropdown"
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
    }
  ];

  ngOnInit(): void {
    this.cercaPratiche();
  }

  cercaPratiche(){
    let pratica = new RicercaPraticaRequest();
    pratica.idsMunicipi = this.authService.getMunicipiAppartenenza();
    pratica.idsStatiPratica = [environment.statiPratica.rigettata];

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

}
