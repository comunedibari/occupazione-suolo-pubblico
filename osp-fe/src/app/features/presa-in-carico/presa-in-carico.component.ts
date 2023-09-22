import { Component, OnInit} from '@angular/core';
import { AuthService } from '@services/auth/auth.service';
import { UserService } from '@services/user.service';
import { environment } from 'environments/environment';
import { MessageService } from '@services/message.service';
import { SpinnerDialogService } from '@core-components/layout/spinner-dialog/services/spinner-dialog.service';
import { PraticaService } from '@services/pratica.service';
import { RicercaPraticaRequest } from '@models/ricerca-pratica-request';
import { ConfirmationService } from 'primeng/api';
import { DialogService } from 'primeng/dynamicdialog';
import { UtilityService } from '@services/utility.service';
import { PraticaDto } from '@models/dto/pratica-dto';
import { Action } from '@shared-components/azioni-pratica/models/action';
import { ColumnSchema } from '@shared-components/table-prime-ng/models/column-schema';
import { BasePraticaAction } from 'app/shared/classes/base-pratica-action';
import { Group } from '@enums/Group.enum';

@Component({
  selector: 'app-presa-in-carico',
  templateUrl: './presa-in-carico.component.html',
  styleUrls: ['./presa-in-carico.component.css']
})
export class PresaInCaricoComponent extends BasePraticaAction implements OnInit {

  constructor(
    private praticaService: PraticaService,
    public authService: AuthService,
    public userService: UserService,
    private messageService: MessageService,
    protected utilityService: UtilityService,
    protected dialogService: DialogService,
    public confirmationService: ConfirmationService,
    private spinnerService: SpinnerDialogService,
  ) {
    super(dialogService, utilityService);
  }

  pratica: any;
  idPratica: number;
  codProtocollo: string;
  showAssegnaDialog: boolean = false;
  istruttoriMunicipio: any[];
  selectedIstruttore: any;
  dataSource: PraticaDto[];
  isAssegnaPratica: boolean = false;

  numProtocollo: string = '';

  showIntegrazioneDialog: boolean = false;
  noteIstruttoreMunicipio: string = '';

/*   get isRevoca():boolean {
    return this.pratica?.dati_istanza?.tipologia_processo == TipologiaPratica.Revoca ? true : false;
  }

  get isDecadenza():boolean {
    return this.pratica?.dati_istanza?.tipologia_processo == TipologiaPratica.Decadenza ? true : false;
  } */

  initSortColumn = 'dataModifica';
  directionSortColumn = "-1"; //1=asc  0=rand   -1=desc
  titleTable: string = this.authService.getGroup() == Group.IstruttoreMunicipio || this.authService.getGroup() == Group.OperatoreSportello ? 'Pratiche da prendere in carico' : 'Pratiche da assegnare';
  exportName = 'Pratiche da prendere in carico';
  globalFilters: any[] = [
    {value:'firmatario.codiceFiscalePartitaIva',label:'Cod. Fiscale/P. IVA'},
    {value:'datiRichiesta.ubicazioneOccupazione',label:'Indirizzo'},
    {value:'municipio.id',label:'N. Municipio'},
    {value:'numero_protocollo',label:'N. Protocollo'},
    {value:'tipoProcesso.descrizione',label:'Tip. Processo'},
    {value:'statoPratica.descrizione',label:'Stato'},
    {value:'dataCreazione',label:'Data operazione'},
  ]

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
      key: 'assegnaPraticaDialog',
      icon: "pi pi-tag",
      tooltip: 'ASSEGNA',
      hidden: (el) => {
        return this.authService.getGroup() != environment.groups.idGruppoDirettoreMunicipio;
      }
    },
    {
      key: 'prendiInCarico',
      icon: "pi pi-check",
      tooltip: 'PRENDI IN CARICO',
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
    pratica.idsStatiPratica = [environment.statiPratica.inserita];

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

  aggiornaStatoPratica(){
    let idIstruttore: number = null;
    let idUtenteDirettore: number = null;

    if(this.authService.getLoggedUser().groups.id == environment.groups.idGruppoDirettoreMunicipio){
      idUtenteDirettore = this.authService.getLoggedUser().userLogged.id
    }

    if(this.selectedIstruttore == null){
      idIstruttore = this.authService.getLoggedUser().userLogged.id
    } else{
      idIstruttore = this.selectedIstruttore.id
    }

    this.spinnerService.showSpinner(true);
    this.praticaService.statoPresaInCarico(this.idPratica, idIstruttore, idUtenteDirettore).subscribe(
      data => {
        this.spinnerService.showSpinner(false);
        this.showAssegnaDialog = false;
        this.messageService.showMessage('success', 'Aggiornamento pratica', 'La pratica è passata in stato Verifica formale');
        this.cercaPratiche();
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage('Aggiornamento pratica', err);
      });
  }

  assegnaPraticaDialog(element: PraticaDto) {
    let userMunicipioId = this.authService.getLoggedUser().userLogged.municipio_ids[0];

    this.pratica = element;
    this.codProtocollo = this.utilityService.getProtocolloPratica(this.pratica);
    this.idPratica = this.pratica.id;

    this.userService.getGruppoMunicipio(environment.groups.idGruppoIstruttoreMunicipio, userMunicipioId).subscribe(
      data => {
        this.istruttoriMunicipio = data;
        this.showAssegnaDialog = true;
      },
      err => {
        this.messageService.showErrorMessage('Ricerca istruttori', err);
      }
    );
  }

  closeAssegnaDialog(event?){
    this.selectedIstruttore = null;
    this.showAssegnaDialog = false;
  }

  prendiInCarico(element: PraticaDto) {
    this.pratica = element;
    this.idPratica = this.pratica.id;
    this.codProtocollo = this.utilityService.getProtocolloPratica(this.pratica);

    this.confirmationService.confirm({
      icon: "pi pi-exclamation-triangle",
      acceptLabel: "Conferma",
      rejectLabel: "Annulla",
      acceptButtonStyleClass: "btn-custom-style btn-dialog-confirm",
      rejectButtonStyleClass: "btn-custom-style btn-dialog-confirm",
      header:this.authService.getGroup() == Group.IstruttoreMunicipio || this.authService.getGroup() == Group.OperatoreSportello ? 'Presa in carico' : 'Assegnazione pratica',
      message:this.authService.getGroup() == Group.IstruttoreMunicipio || this.authService.getGroup() == Group.OperatoreSportello ? `Confermi di prendere in carico la pratica cod. protocollo ${this.codProtocollo}?` : ` Confermi di assegnare la pratica cod. protocollo ${this.codProtocollo}?`,
      accept: () => {
        this.submitPrendiInCarico(false);
      }
    });
  }


  submitPrendiInCarico(isAssegnaPratica) {
    this.isAssegnaPratica = isAssegnaPratica;
    this.spinnerService.showSpinner(true);
    this.numProtocollo = this.codProtocollo || '';

    this.aggiornaStatoPratica();
    this.spinnerService.showSpinner(false);
  }

}
