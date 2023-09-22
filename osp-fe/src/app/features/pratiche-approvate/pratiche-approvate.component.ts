import { Component, OnInit } from '@angular/core';
import { SpinnerDialogService } from '@core-components/layout/spinner-dialog/services/spinner-dialog.service';
import { AllegatoDTO } from '@models/dto/allegato-dto';
import { PraticaDto } from '@models/dto/pratica-dto';
import { TemplateDTO } from '@models/dto/template-dto';
import { RicercaPraticaRequest } from '@models/ricerca-pratica-request';
import { AllegatiService } from '@services/allegati.service';
import { AuthService } from '@services/auth/auth.service';
import { MessageService } from '@services/message.service';
import { PraticaService } from '@services/pratica.service';
import { TemplateService } from '@services/template.service';
import { UtilityService } from '@services/utility.service';
import { Action } from '@shared-components/azioni-pratica/models/action';
import { ColumnSchema } from '@shared-components/table-prime-ng/models/column-schema';
import { DestinazioneAllegato } from '@shared-components/upload-file/enums/destinazione-allegato.enum';
import { Mode } from '@shared-components/upload-file/enums/mode.enum';
import { BasePraticaAction } from 'app/shared/classes/base-pratica-action';
import { environment } from 'environments/environment';
import { DialogService } from 'primeng/dynamicdialog';

@Component({
  selector: 'app-pratiche-approvate',
  templateUrl: './pratiche-approvate.component.html',
  styleUrls: ['./pratiche-approvate.component.css']
})
export class PraticheApprovateComponent extends BasePraticaAction implements OnInit {
  destinazioneAllegato = DestinazioneAllegato.PRATICA;
  uploadMode = Mode.SINGLE;
  filtroStatoPratica = environment.statiPratica.approvata;
  initSortColumn = 'dataModifica';
  directionSortColumn = "1"; //1=asc  0=rand   -1=desc
  titleTable: string = 'Pratiche approvate';
  exportName = 'Pratiche approvate';
  globalFilters: any[] = [
    {value:'firmatario.codiceFiscalePartitaIva',label:'Cod. Fiscale/P. IVA'},
    {value:'datiRichiesta.ubicazioneOccupazione',label:'Indirizzo'},
    {value:'tipoProcesso.descrizione',label:'Tip. Processo'},
    {value:'utentePresaInCarico.username',label:'Istruttore'}
  ];

  dataDetermina: Date;
  limitData: Date = new Date();
  numProtocollo: string = '';
  showProtocolloDialog: boolean = false;
  isProtocollata: boolean = false;
  newUploadedFiles: AllegatoDTO[] = [];
  isFileInCheckout: boolean = false;

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
      header: "Tipo Processo",
      type: "dropdown"
    },
    {
      field: "utentePresaInCarico.username",
      header: "Istruttore",
      type: "text",
    },
    {
      field: "municipio.id",
      header: "N. Municipio",
      type: "dropdown"
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
      key: 'downloadDetermina',
      icon: "pi pi-download",
      tooltip: 'DOWNLOAD TEMPLATE DETERMINA',
      hidden: (el) => {
         return this.authService.getGroup() != environment.groups.idGruppoIstruttoreMunicipio;
      }
    },
    {
      key: 'uploadDetermina',
      icon: "pi pi-upload",
      tooltip: 'UPLOAD DETERMINA',
      hidden: (el) => {
         return this.authService.getGroup() != environment.groups.idGruppoIstruttoreMunicipio;
      }
    },
  ];

  dataSource: PraticaDto[];
  identificativoDetermina: string = '';
  pratica: PraticaDto = null;
  showUploadDeterminaDialog: boolean = false;

  constructor(
    private authService: AuthService,
    private spinnerService: SpinnerDialogService,
    private messageService: MessageService,
    protected dialogService: DialogService,
    protected utilityService: UtilityService,
    private praticaService: PraticaService,
    private templateService: TemplateService,
    private allegatiService: AllegatiService
  ) {
    super(dialogService, utilityService);
  }

  calculateYearRange(): string {
    let min = new Date().getFullYear() - 100;
    let max = new Date().getFullYear();
    return min.toString() + ":" + max.toString();
  }

  ngOnInit(): void {
    this.cercaPratiche();
  }

  cercaPratiche(){
    let ricercaPratiche = new RicercaPraticaRequest();
    ricercaPratiche.idsMunicipi = this.authService.getMunicipiAppartenenza();
    ricercaPratiche.idsStatiPratica = [this.filtroStatoPratica];

    this.spinnerService.showSpinner(true);
    this.praticaService.getPratiche(ricercaPratiche, this.authService.getLoggedUser()).subscribe(
      data => {
        this.spinnerService.showSpinner(false);
        this.dataSource = data.content.map(el => Object.setPrototypeOf(el, PraticaDto.prototype));
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage('Ricerca pratiche', err);
      }
    );
  }

  downloadDetermina(pratica: PraticaDto): void {
    this.templateService.getTemplateElaborato(pratica.id, environment.template.determinaDiConcessione).subscribe(
      (data: TemplateDTO) => {
        this.utilityService.downloadFile(data.nomeFile, data.mimeType, data.fileTemplate);
      },
      err => {this.messageService.showErrorMessage("Template determina", err);}
    );
  }

  uploadDetermina(pratica: PraticaDto): void {
    this.pratica = pratica;
    this.showUploadDeterminaDialog = true;
  }

  mandatoryDocumentsAttached: boolean = false;
  onMandatoryDocuments(event: boolean) {
    this.mandatoryDocumentsAttached = event;
  }

  isConfirmDisabled(): boolean {
    return !(this.identificativoDetermina.trim().length > 0 && this.mandatoryDocumentsAttached) || this.isFileInCheckout;
  }

  confermaUploadDetermina() {
    let dataDeterminaBE = this.utilityService.formatDateForBe( '' + this.dataDetermina);
    this.spinnerService.showSpinner(true);
    this.praticaService.inserimentoDetermina(this.pratica.id, this.authService.getLoggedUser().userLogged.id, this.identificativoDetermina, dataDeterminaBE).subscribe(
      (data: PraticaDto) => {
        this.newUploadedFiles = [];
        this.showUploadDeterminaDialog = false;
        this.spinnerService.showSpinner(false);
        this.messageService.showMessage("success", "Inserimento determina pratica", "Inserimento della determina avvenuto con successo");
        if (data && data.protocolli) {
          const pratica = data;
          this.isProtocollata = pratica.protocolli.length > 0 ;
          this.numProtocollo = this.utilityService.getLastProtocol(pratica.protocolli);
          this.showProtocolloDialog = true;
        } else {
          this.messageService.showErrorMessage('Errore associazione determina', 'Protocollo non associato');
          this.numProtocollo = '--|--';
          this.showProtocolloDialog = true;
        }
      },
      err => {
        this.messageService.showErrorMessage('Errore associazione determina', err);
        this.spinnerService.showSpinner(false);
        this.numProtocollo = '--|--';
        this.showProtocolloDialog = true;
      }
    );
  }

  closeUploadDeterminaDialog(): void {
    this.identificativoDetermina = '';
    this.dataDetermina = null;
    this.showUploadDeterminaDialog = false;

    if(this.newUploadedFiles.length > 0) {
      const promises = this.newUploadedFiles.map((allegato, index) => {
        return this.allegatiService.deleteAllegato(allegato.id).toPromise();
      });
      Promise.all(promises).finally(() => {
        this.newUploadedFiles = [];
      });
    }
  }

  closeProtocolloDialog(event?) {
    this.showProtocolloDialog = false;
    this.isProtocollata = false;
    this.numProtocollo = '';
    this.cercaPratiche();
  }

  onUploadedFile(event) {
    this.newUploadedFiles.push(event);
  }

  fileInCheckout(status_checkout) {
    this.isFileInCheckout = status_checkout;
  }
}
