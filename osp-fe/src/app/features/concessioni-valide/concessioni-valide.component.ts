import { Component, OnInit, Renderer2 } from '@angular/core';
import { Action } from '@shared-components/azioni-pratica/models/action';
import { ColumnSchema } from '@shared-components/table-prime-ng/models/column-schema';
import { PraticaDto } from '@models/dto/pratica-dto';
import { GestionePraticaDialogConfig } from '@features/gestione-pratica/model/gestione-pratica-dialog-config';
import { DialogService } from 'primeng/dynamicdialog';
import { GestionePraticaComponent } from '@features/gestione-pratica/gestione-pratica.component';
import { UtilityService } from '@services/utility.service';
import { SpinnerDialogService } from '@core-components/layout/spinner-dialog/services/spinner-dialog.service';
import { PraticaService } from '@services/pratica.service';
import { MessageService } from '@services/message.service';
import { environment } from 'environments/environment';
import { AuthService } from '@services/auth/auth.service';
import { RicercaPraticaRequest } from '@models/ricerca-pratica-request';
import { BasePraticaAction } from 'app/shared/classes/base-pratica-action';
import { MappaComponent } from '@shared-components/mappa/mappa.component';
import { DestinazioneAllegato } from '@shared-components/upload-file/enums/destinazione-allegato.enum';
import { Mode } from '@shared-components/upload-file/enums/mode.enum';
import { TemplateService } from '@services/template.service';
import { TemplateDTO } from '@models/dto/template-dto';
import { AllegatoDTO } from '@models/dto/allegato-dto';
import { AllegatiService } from '@services/allegati.service';

@Component({
  selector: 'app-concessioni-valide',
  templateUrl: './concessioni-valide.component.html',
  styleUrls: ['./concessioni-valide.component.css']
})
export class ConcessioniValideComponent extends BasePraticaAction implements OnInit {

  pratica: PraticaDto;
  destinazioneAllegato = DestinazioneAllegato.PRATICA;
  uploadMode = Mode.SINGLE;
  filtroStatoPratica = environment.statiPratica.concessioneValida;
  showProcessiPostConcessioneDialog: boolean = false;

  dataSource: PraticaDto[];
  directionSortColumn = "1"; //1=asc  0=rand   -1=desc
  titleTable = 'Concessioni valide';
  exportName = 'Concessioni valide';
  title = 'Concessioni valide';

  initSortColumn = 'dataModifica';
  globalFilters: any[] = [
    {value:'firmatario.codiceFiscalePartitaIva',label:'Cod. Fiscale/P. IVA'},
    {value:'datiRichiesta.ubicazioneOccupazione',label:'Indirizzo'},
    {value:'tipoProcesso.descrizione',label:'Tip. Processo'},
    {value:'utentePresaInCarico.username',label:'Istruttore'}
  ];

  dataDetermina: Date;
  limitData: Date = new Date();

  determinaRevocaDialog = environment.template.determinaDiRevoca;
  determinaDecadenzaDialog = environment.template.determinaDiDecadenza;
  determinaAnnullamentoDialog = environment.template.determinaDiAnnullamento;
  tipoDetermina: number = null;
  tipoProcesso: number = null;
  numProtocollo: string = '';
  showProtocolloDialog: boolean = false;
  isProtocollata: boolean = false;
  newUploadedFiles: AllegatoDTO[] = [];
  isFileInCheckout: boolean = false;

  // @ts-ignore
  columnSchema: ColumnSchema[] = [
    {
      field: 'firmatario.codiceFiscalePartitaIva',
      header: 'Cod. Fiscale/P. IVA',
      type: 'text'
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
      key: 'processiPostConcessione',
      icon: "pi pi-arrow-circle-right",
      tooltip: 'AVVIA PROCESSO POST CONCESSIONE',
      hidden: (el) => {
        return (this.authService.getGroup() != environment.groups.idGruppoOperatoreSportello &&
        this.authService.getGroup() != environment.groups.idGruppoIstruttoreMunicipio &&
        this.authService.getGroup() != environment.groups.idGruppoDirettoreMunicipio);
      }
    },
  ];

  showUploadDeterminaDialog: boolean = false;
  identificativoDetermina: string = '';
  notePerCittadino: string = '';

  constructor(
    protected dialogService: DialogService,
    protected utilityService: UtilityService,
    private spinnerService: SpinnerDialogService,
    private messageService: MessageService,
    private authService: AuthService,
    private praticaService: PraticaService,
    private templateService: TemplateService,
    private allegatiService: AllegatiService
  ) {
    super(dialogService, utilityService);
  }

  ngOnInit(): void {
    this.cercaPratiche();
  }

  cercaPratiche() {
    let praticaConcessioneValida = new RicercaPraticaRequest();
    praticaConcessioneValida.idsMunicipi = this.authService.getMunicipiAppartenenza();
    praticaConcessioneValida.idsStatiPratica = [environment.statiPratica.concessioneValida];

    this.spinnerService.showSpinner(true);
    this.praticaService.getPratiche(praticaConcessioneValida, this.authService.getLoggedUser()).subscribe(
      (data) => {
        this.spinnerService.showSpinner(false);
        this.dataSource = data.content.map(el => Object.setPrototypeOf(el, PraticaDto.prototype));
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage('Ricerca pratiche', err);
      });
  }

  openMapDialog(){
    let data = {
      elements: this.dataSource,
      fullScreen: true,
      enableHeatmap: true
    }

    let dialogRef = this.dialogService.open(MappaComponent, this.utilityService.configDynamicDialogFullScreen(data, "Concessioni valide"));

    dialogRef.onClose.subscribe((element) => {
      if (element) {
        let el = this.dataSource.find(pratica => pratica.id == element.id);
        this.dettaglioPratica(el);
      }
    });
  }

  mandatoryDocumentsAttached: boolean = false;
  onMandatoryDocuments(event: boolean) {
    this.mandatoryDocumentsAttached = event;
  }

  isConfirmDisabled(): boolean {
    return !(this.identificativoDetermina.trim().length > 0 &&
    this.mandatoryDocumentsAttached && this.notePerCittadino.trim().length > 0);
  }

  processiPostConcessione(element: PraticaDto) {
    this.pratica = element;
    this.showProcessiPostConcessioneDialog = true;
  }

  closeProcessiPostConcessioneDialog() {
    this.showProcessiPostConcessioneDialog = false;
  }

  prorogaConcessione(element: PraticaDto) {
    this.spinnerService.showSpinner(true);
    this.praticaService.prorogaPreCompilata(element.id).subscribe(
      (data: any) =>{
        this.spinnerService.showSpinner(false);
        let config: GestionePraticaDialogConfig = {
          pratica: data,
          readonly: false,
          isRichiestaProroga: true
        };
        let dialogRef = this.dialogService.open(GestionePraticaComponent,  this.utilityService.configDynamicDialogFullScreen(config, "Inserisci richiesta proroga"));
        dialogRef.onClose.subscribe((data: any) => {
          this.closeProcessiPostConcessioneDialog();
        });
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage("Errore", err);
      }
    )
  }

  rinunciaConcessione(element: PraticaDto) {
    this.spinnerService.showSpinner(true);
    this.praticaService.rinunciaPreCompilata(element.id).subscribe(
      (data: any) =>{
        this.spinnerService.showSpinner(false);
        let config: GestionePraticaDialogConfig = {
          pratica: data,
          readonly: false,
          isRinunciaConcessione: true
        }
        let dialogRef = this.dialogService.open(GestionePraticaComponent,  this.utilityService.configDynamicDialogFullScreen(config, "Rinuncia concessione"));
        dialogRef.onClose.subscribe((data: any) => {
          this.closeProcessiPostConcessioneDialog();
        });
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage("Errore", err);
      }
    )
  }

  idTipoProcesso() {
    if (this.tipoDetermina == this.determinaRevocaDialog) {
      return this.tipoProcesso = environment.processes.revocaConcessione
    } else if (this.tipoDetermina == this.determinaDecadenzaDialog) {
      return this.tipoProcesso = environment.processes.decadenzaConcessione
    } else {
      return this.tipoProcesso = environment.processes.annullamentoConcessione
    }
  }

  generaTemplateElaborato(tipoTemplate: number): void {
    this.templateService.getTemplateElaborato(this.pratica.id, tipoTemplate, this.notePerCittadino).subscribe(
      (data: TemplateDTO) => {
        this.utilityService.downloadFile(data.nomeFile, data.mimeType, data.fileTemplate);
      },
      err => {this.messageService.showErrorMessage("Template determina", err);}
    );
  }

  getDescrizioneTipoTemplate(): string {
    let ret = "Determina di revoca";
    if(this.tipoDetermina == this.determinaDecadenzaDialog){
      ret = "Determina di decadenza";
    } else if (this.tipoDetermina == this.determinaAnnullamentoDialog) {
      ret = "Determina di annullamento";
    }
    return ret;
   }

  apriDeterminaDialog(pratica: PraticaDto, numeroTipoDetermina: number) {
    this.pratica = pratica;
    this.tipoDetermina = numeroTipoDetermina;
    this.closeProcessiPostConcessioneDialog();
    this.newUploadedFiles = [];
    this.showUploadDeterminaDialog = true;
  }

  confermaUploadDeterminaDialog() {
    let dataDeterminaBE = this.utilityService.formatDateForBe( '' + this.dataDetermina);
    this.spinnerService.showSpinner(true);
    if (this.tipoDetermina == this.determinaRevocaDialog) {
      this.praticaService.inserimentoDeterminaRevoca(this.pratica.id, this.authService.getLoggedUser().userLogged.id, this.identificativoDetermina, dataDeterminaBE, this.notePerCittadino).subscribe(
        (data: PraticaDto) => {
          this.newUploadedFiles = [];
          this.showUploadDeterminaDialog = false;
          this.spinnerService.showSpinner(false);
          this.messageService.showMessage("success", "Revoca", "Processo di revoca avviato con successo.");
          const pratica = data;
          this.isProtocollata = pratica.protocolli.length > 0 ;
          this.numProtocollo = this.utilityService.getLastProtocol(pratica.protocolli);
          this.showProtocolloDialog = true;
        },
        err => {
          this.messageService.showErrorMessage('Errore', err);
          this.spinnerService.showSpinner(false);
          /*this.numProtocollo = '--|--';
          this.showProtocolloDialog = true;*/
        }
      );
    } else if (this.tipoDetermina == this.determinaDecadenzaDialog) {
      this.praticaService.inserimentoDeterminaDecadenza(this.pratica.id, this.authService.getLoggedUser().userLogged.id, this.identificativoDetermina, dataDeterminaBE, this.notePerCittadino).subscribe(
        (data: PraticaDto) => {
          this.newUploadedFiles = [];
          this.showUploadDeterminaDialog = false;
          this.spinnerService.showSpinner(false);
          this.messageService.showMessage("success", "Decadenza", "Processo di decadenza avviato con successo.");
          const pratica = data;
          this.isProtocollata = pratica.protocolli.length > 0 ;
          this.numProtocollo = this.utilityService.getLastProtocol(pratica.protocolli);
          this.showProtocolloDialog = true;
        },
        err => {
          this.messageService.showErrorMessage('Errore', err);
          this.spinnerService.showSpinner(false);
          /*this.numProtocollo = '--|--';
          this.showProtocolloDialog = true;*/
        }
      );
    } else {
      this.praticaService.inserimentoDeterminaAnnullamento(this.pratica.id, this.authService.getLoggedUser().userLogged.id, this.identificativoDetermina, dataDeterminaBE, this.notePerCittadino).subscribe(
        (data: PraticaDto) => {
          this.newUploadedFiles = [];
          this.showUploadDeterminaDialog = false;
          this.spinnerService.showSpinner(false);
          this.messageService.showMessage("success", "Annullamento", "Processo di annullamento avviato con successo.");
          const pratica = data;
          this.isProtocollata = pratica.protocolli.length > 0 ;
          this.numProtocollo = this.utilityService.getLastProtocol(pratica.protocolli);
          this.showProtocolloDialog = true;
        },
        err => {
          this.messageService.showErrorMessage('Errore', err);
          this.spinnerService.showSpinner(false);
          /*this.numProtocollo = '--|--';
          this.showProtocolloDialog = true;*/
        }
      );
    }
  }

  calculateYearRange(): string {
    let min = new Date().getFullYear() - 100;
    let max = new Date().getFullYear();
    return min.toString() + ":" + max.toString();
  }

  closeUploadDeterminaDialog(): void {
    this.showUploadDeterminaDialog = false;
    this.identificativoDetermina = '';
    this.notePerCittadino = '';
    this.dataDetermina = null;

    if (this.newUploadedFiles.length > 0) {
      const promises = this.newUploadedFiles.map((allegato, index) => {
        return this.allegatiService.deleteAllegato(allegato.id).toPromise();
      });
      Promise.all(promises).finally(() => {
        this.newUploadedFiles = [];
      });
    }
  }

  onUploadedFile(event) {
    this.newUploadedFiles.push(event);
  }

  closeProtocolloDialog(event?) {
    this.showProtocolloDialog = false;
    this.isProtocollata = false;
    this.numProtocollo = '';
    this.cercaPratiche();
  }

  fileInCheckout(status_checkout) {
    this.isFileInCheckout = status_checkout;
  }
}
