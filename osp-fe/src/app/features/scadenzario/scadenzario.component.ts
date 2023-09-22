import { Component, Input, OnInit } from '@angular/core';
import { PraticaDto } from '@models/dto/pratica-dto';
import { MessageService } from '@services/message.service';
import { PraticaService } from '@services/pratica.service';
import { UtilityService } from '@services/utility.service';
import { Action } from '@shared-components/azioni-pratica/models/action';
import { ColumnSchema } from '@shared-components/table-prime-ng/models/column-schema';
import { DialogService} from 'primeng/dynamicdialog';
import { BasePraticaAction } from 'app/shared/classes/base-pratica-action';
import { SpinnerDialogService } from '@core-components/layout/spinner-dialog/services/spinner-dialog.service';
import { AuthService } from '@services/auth/auth.service';
import { ConfirmationService } from 'primeng/api';
import { ScadenzarioService } from '@services/scadenzario.service';
import { environment } from 'environments/environment';
import { DettaglioPraticaDialogConfig } from '@features/dettaglio-pratica/model/dettaglio-pratica-dialog-config';
import { DettaglioPraticaComponent } from '@features/dettaglio-pratica/dettaglio-pratica.component';

@Component({
  selector: 'app-scadenzario',
  templateUrl: './scadenzario.component.html',
  styleUrls: ['./scadenzario.component.css']
})
export class ScadenzarioComponent extends BasePraticaAction   implements OnInit {

  showSpinner: boolean = false;
  @Input() dataSource: PraticaDto[];

  initSortColumn = 'dataNotifica';
  directionSortColumn = "-1"; //1=asc  0=rand   -1=desc
  titleTable: string = 'Pratiche in scadenza';
  exportName = 'Pratiche in scadenza';
  globalFilters: any[] = [
    {value:'pratica.datiRichiesta.ubicazioneOccupazione',label:'Indirizzo'},
    {value:'pratica.utentePresaInCarico.username',label:'Istruttore'}
  ];

  columnsSchema: ColumnSchema[] = [
    {
      field: "pratica.firmatario.codiceFiscalePartitaIva",
      header: "Cod. Fiscale/P. IVA",
      type: "text"
    },
    {
      field: "protocollo",
      header: "N. Protocollo",
      type:"text",
      customSortFunction: this.utilityService.protocolSortFunction
    },
    {
      field: "pratica.datiRichiesta.ubicazioneOccupazione",
      header: "Indirizzo",
      inactive: true,
      type: "text"
    },
    {
      field: "pratica.statoPratica.descrizione",
      header: "Stato",
      type: "dropdown",
    },
    {
      field: "pratica.utentePresaInCarico.username",
      header: "Istruttore",
      type: "text",
    },
    {
      field: "tipoNotificaScadenzario.descrizione",
      header: "Informazioni",
      type: "text",
    },
    {
      field: "dataNotifica",
      header: "Data notifica",
      type: "date",
      pipe: "date"
    }
  ];

  actions: Action[] = [
    {
      key: 'dettaglioPraticaScadenzario',
      icon: "pi pi-search",
      tooltip: 'DETTAGLIO',
    },
    {
      key: 'archiviaPratica',
      icon: "pi pi-inbox",
      tooltip: 'ARCHIVIA',
      hidden: (el) => {
        return el.tipoNotificaScadenzario.id != environment.scadenzario.scadenzaTempisticheIntegrazione &&
        el.tipoNotificaScadenzario.id != environment.scadenzario.scadenzaTempistichePagamento ||
        this.authService.getGroup() != environment.groups.idGruppoIstruttoreMunicipio;
      }
    }
  ];

  constructor(
    private spinnerService: SpinnerDialogService,
    private praticaService: PraticaService,
    private messageService: MessageService,
    protected dialogService: DialogService,
    protected utilityService: UtilityService,
    private authService: AuthService,
    public confirmationService: ConfirmationService,
    private scadenzarioService : ScadenzarioService,
    ) {
    super(dialogService, utilityService);
  }

  protected getProtocolloScadenzario(el): string {
    return this.utilityService.getProtocolloPratica(el.pratica);
}

  ngOnInit(): void {
    this.cercaPratiche();
  }

  cercaPratiche() {
    this.spinnerService.showSpinner(true);
    this.scadenzarioService.cercaNotificheScadenziario(this.authService.getLoggedUser().userLogged.id).subscribe(
      (data: any) => {
        this.spinnerService.showSpinner(false);
        this.dataSource = data.map(el => {
          const p = Object.setPrototypeOf(el.pratica, PraticaDto.prototype);
          console.log(p);
          el.protocollo = p.getProtocollo;
          return el;
        });
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage("Ricerca pratiche", err);
      });
  }

  protected dettaglioPraticaScadenzario(element) {
    let config: DettaglioPraticaDialogConfig = {
      pratica: element.pratica,
      isPraticaOrigine: false,
      showStorico: true
    }

    this.dialogService.open(DettaglioPraticaComponent,  this.utilityService.configDynamicDialogFullScreen(config, "Pratica cittadino"));
}

/*   ngOnDestroy() {
    this.renderer.removeClass(document.body, 'modal-open');
  } */

  archiviaPratica(element: any) {
    this.confirmationService.confirm({
      icon: "pi pi-exclamation-triangle",
      acceptLabel: "Conferma",
      rejectLabel: "Annulla",
      acceptButtonStyleClass: "btn-custom-style btn-dialog-confirm",
      rejectButtonStyleClass: "btn-custom-style btn-dialog-confirm",
      header: "Archiviazione pratica",
      message: "Confermi di voler archiviare la pratica selezionata?",
      accept: () => {
        this.archiviazionePratica(element);
      }
    });
  }

  archiviazionePratica(element){
    this.spinnerService.showSpinner(true);
    this.praticaService.archiviazionePratica(element.pratica.id, this.authService.getLoggedUser().userLogged.id).subscribe(
      (data: PraticaDto) => {
        this.spinnerService.showSpinner(false);
        this.messageService.showMessage('success', 'Archiviazione pratica', "Pratica archiviata con successo");
        this.cercaPratiche();
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage("Archiviazione pratica", err);
      }
    );
  }
}
