import {Component, OnInit} from '@angular/core';
import {Mode} from '@shared-components/upload-file/enums/mode.enum';
import {PraticaDto} from '@models/dto/pratica-dto';
import {environment} from 'environments/environment';
import {DestinazioneAllegato} from '@shared-components/upload-file/enums/destinazione-allegato.enum';
import {ColumnSchema} from '@shared-components/table-prime-ng/models/column-schema';
import {RicercaPraticaRequest} from '@models/ricerca-pratica-request';
import {SpinnerDialogService} from '@core-components/layout/spinner-dialog/services/spinner-dialog.service';
import {AuthService} from '@services/auth/auth.service';
import {MessageService} from '@services/message.service';
import {DialogService} from 'primeng/dynamicdialog';
import {UtilityService} from '@services/utility.service';
import {PraticaService} from '@services/pratica.service';
import {TemplateDTO} from '@models/dto/template-dto';
import {Action} from '@shared-components/azioni-pratica/models/action';
import {TemplateService} from '@services/template.service';
import {BasePraticaAction} from 'app/shared/classes/base-pratica-action';
import {AllegatiService} from '@services/allegati.service';
import {AllegatoDTO} from '@models/dto/allegato-dto';

@Component({
  selector: 'app-pratiche-da-rigettare',
  templateUrl: './pratiche-da-rigettare.component.html',
  styleUrls: ['./pratiche-da-rigettare.component.css']
})
export class PraticheDaRigettareComponent extends BasePraticaAction implements OnInit {


  showProtocolloDialog = false;
  isProtocollata = false;
  numProtocollo = '';

  constructor(
    private spinnerService: SpinnerDialogService,
    private authService: AuthService,
    private messageService: MessageService,
    protected dialogService: DialogService,
    protected utilityService: UtilityService,
    private templateService: TemplateService,
    private praticaService: PraticaService,
    private allegatiService: AllegatiService
  ) {
    super(dialogService, utilityService);
  }

  destinazioneAllegato = DestinazioneAllegato.PRATICA;
  uploadMode = Mode.SINGLE;
  filtroStatoPratica: number = environment.statiPratica.praticaDaRigettare;

  pratica: PraticaDto;
  identificativoDetermina: string = '';
  showUploadDialog: boolean = false;

  dataDetermina: Date;
  limitData: Date = new Date();

  dataSource: PraticaDto[];
  directionSortColumn = '1'; // 1=asc  0=rand   -1=desc
  titleTable = 'Pratiche da rigettare';
  exportName = 'Pratiche da rigettare';
  title = 'Pratiche da rigettare';

  initSortColumn = 'dataModifica';
  globalFilters: any[] = [
    {value: 'firmatario.codiceFiscalePartitaIva', label: 'Cod. Fiscale/P. IVA'},
    {value: 'datiRichiesta.ubicazioneOccupazione', label: 'Indirizzo'},
    {value: 'tipoProcesso.descrizione', label: 'Tip. Processo'},
    {value: 'utentePresaInCarico.username', label: 'Istruttore'}
  ];

  uploadedFiles: AllegatoDTO[] = [];
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
    },
    {
      key: 'downloadAction',
      icon: "pi pi-download",
      tooltip: 'DOWNLOAD TEMPLATE DETERMINA',
      hidden: (el) => {
        return this.authService.getGroup() !== environment.groups.idGruppoIstruttoreMunicipio; //|| el.dati_istanza.tipologia_processo == TipologiaPratica.Rinuncia; */
      }
    },
    {
      key: 'uploadDialog',
      icon: "pi pi-upload",
      tooltip: 'UPLOAD DETERMINA',
      hidden: (el) => {
        return this.authService.getGroup() !== environment.groups.idGruppoIstruttoreMunicipio; //|| el.dati_istanza.tipologia_processo == TipologiaPratica.Rinuncia; */
      }
    },
  ];

  getIdDeterminaRigetto() {
    return environment.template.determinaRigetto;
  }

  calculateYearRange(): string {
    let min = new Date().getFullYear() - 100;
    let max = new Date().getFullYear();
    return min.toString() + ":" + max.toString();
  }

  ngOnInit(): void {
    this.cercaPratiche();
  }

  cercaPratiche() {
    let praticaDaRigettare = new RicercaPraticaRequest();
    praticaDaRigettare.idsMunicipi = this.authService.getMunicipiAppartenenza();
    praticaDaRigettare.idsStatiPratica = [environment.statiPratica.praticaDaRigettare];

    this.spinnerService.showSpinner(true);
    this.praticaService.getPratiche(praticaDaRigettare, this.authService.getLoggedUser()).subscribe(
      (data) => {
        this.spinnerService.showSpinner(false);
        this.dataSource = data.content.map(el => Object.setPrototypeOf(el, PraticaDto.prototype));
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage('Ricerca pratiche', err);
      });
  }

  downloadAction(element: any) {
    this.pratica = element;
    this.generaTemplateElaborato(this.getIdDeterminaRigetto());
  }

  uploadDialog(element: any) {
    this.pratica = element;
    this.identificativoDetermina = '';
    this.dataDetermina = null;
    this.showUploadDialog = true;
  }

  onCloseUploadDialog() {
    this.showUploadDialog = false;
    if (this.uploadedFiles.length > 0) {
      const promises = this.uploadedFiles.map((allegato, index) => {
        return this.allegatiService.deleteAllegato(allegato.id).toPromise();
      });
      Promise.all(promises).finally(() => {
        this.uploadedFiles = [];
      });
    }
  }

  onUploadedFile(event) {
    this.uploadedFiles.push(event);
  }

  inviaDeterminaRigetto() {
    let dataDeterminaBE = this.utilityService.formatDateForBe('' + this.dataDetermina);
    this.spinnerService.showSpinner(true);
    this.praticaService.statoDeterminaRigetto(this.pratica.id, this.authService.getLoggedUser().userLogged.id, this.identificativoDetermina, dataDeterminaBE).subscribe(
      (data: PraticaDto) => {
        this.uploadedFiles = [];
        this.showUploadDialog = false;
        this.spinnerService.showSpinner(false);
        this.messageService.showMessage('success', 'Rigetto pratica', "Pratica rigettata con successo");
        if (data && data.protocolli) {
          const pratica = data;
          this.isProtocollata = pratica.protocolli.length > 0 ;
          this.numProtocollo = this.utilityService.getLastProtocol(pratica.protocolli);
          this.showProtocolloDialog = true;
        } else {
          this.messageService.showErrorMessage('Rigetto pratica', 'Protocollo non associato');
          this.numProtocollo = '--|--';
          this.showProtocolloDialog = true;
        }
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage('Rigetto pratica', err);
        this.numProtocollo = '--|--';
        this.showProtocolloDialog = true;
      }
    );
  }

  generaTemplateElaborato(tipoTemplate: number): void {
    this.templateService.getTemplateElaborato(this.pratica.id, tipoTemplate).subscribe(
      (data: TemplateDTO) => {
        this.utilityService.downloadFile(data.nomeFile, data.mimeType, data.fileTemplate);
      },
      err => {
        this.messageService.showErrorMessage("Template determina", err);
      }
    );
  }

  fileInCheckout(status_checkout) {
    this.isFileInCheckout = status_checkout;
  }

  closeProtocolloDialog(event?): void {
    this.showProtocolloDialog = false;
    this.isProtocollata = false;
    this.numProtocollo = '';
    this.cercaPratiche();
  }
}
