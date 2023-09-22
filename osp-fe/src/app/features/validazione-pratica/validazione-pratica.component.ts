import { ThrowStmt } from '@angular/compiler';
import { Component, EventEmitter, OnInit } from '@angular/core';
import { SpinnerDialogService } from '@core-components/layout/spinner-dialog/services/spinner-dialog.service';
import { AllegatoDTO } from '@models/dto/allegato-dto';
import { GeoMultiPointDTO } from '@models/dto/geo-multi-point-dto';
import { GruppoDTO } from '@models/dto/gruppo-dto';
import { PraticaDto } from '@models/dto/pratica-dto';
import { RichiestaIntegrazioneDTO } from '@models/dto/richiesta-integrazione-dto';
import { RichiestaParereDTO } from '@models/dto/richiesta-parere-dto';
import { RicercaPraticaRequest } from '@models/ricerca-pratica-request';
import { AllegatiService } from '@services/allegati.service';
import { AuthService } from '@services/auth/auth.service';
import { MessageService } from '@services/message.service';
import { PraticaService } from '@services/pratica.service';
import { TipologicheService } from '@services/tipologiche.service';
import { UtilityService } from '@services/utility.service';
import { Action } from '@shared-components/azioni-pratica/models/action';
import { MappaComponent } from '@shared-components/mappa/mappa.component';
import { DestinazioneAllegato } from '@shared-components/upload-file/enums/destinazione-allegato.enum';
import { Mode } from '@shared-components/upload-file/enums/mode.enum';
import { BasePraticaAction } from 'app/shared/classes/base-pratica-action';
import { environment } from 'environments/environment';
import { ConfirmationService } from 'primeng/api';
import { DialogService } from 'primeng/dynamicdialog';


@Component({
  selector: 'app-validazione-pratica',
  templateUrl: './validazione-pratica.component.html',
  styleUrls: ['./validazione-pratica.component.css']
})
export class ValidazionePraticaComponent extends BasePraticaAction implements OnInit {

  dataSource: PraticaDto[];
  directionSortColumn = "-1"; //1=asc  0=rand   -1=desc
  titleTable = 'Pratiche da validare';
  exportName = 'Pratiche da validare';
  title = 'Pratiche da validare';
  initSortColumn = 'dataModifica';

  private tipoGruppi: GruppoDTO[] = null;
  attorePareri: GruppoDTO;
  showValidaDialog: boolean = false;
  pratica: PraticaDto;
  idSelectedPratica: number;
  idUtentePresaInCarico: number;
  showIntegrazioneDialog: boolean = false;

  numProtocollo: string = '';
  showProtocolloDialog: boolean = false;
  isProtocollata: boolean = false;
  actionRiprotocollazione: string = '';

  noteIstruttoreMunicipio: string = '';
  numeroProtocollo: string = '';
  sceltaValidazione: string = '';

  private mappaElements: PraticaDto[] = [];
  coordUbicazioneTemporanea : GeoMultiPointDTO;
  disableCheckbox: boolean = false;

  uploadMode = Mode.SINGLE;
  isEsente: boolean = false;
  isEsenteOnInit: boolean = false;
  destinazioneAllegato: DestinazioneAllegato = DestinazioneAllegato.RICHIESTA_INTEGRAZIONE;
  doc_richiesta_integrazione_diniego: any = {
    // id: '',
    // data_emissione: '',
    id_blob: ''
  };

  newRichiestaIntegrazioneUploadedFiles: AllegatoDTO[] = [];

  showMotivazioneEsenzioneCupDialog: boolean;
  showEsenzioneCupConfirmDialog: boolean;
  motivazioneEsenzioneCup: string;

  annullaEsenzioneMarcaDaBollo: boolean;
  riabilitaEsenzioneMarcaDaBollo: boolean = false;

  errorDialogOverlap: boolean;
  errorMessageOverlap: string;


  idAllegatoRichiestaIntegrazione: number = null;
  errorDialogOverlapRichiestaIntegrazione: boolean;
  errorMessageOverlapRichiestaIntegrazione: string;

  skipCheckOccupazione: boolean;

  globalFilters: any[] = [
    {value:'firmatario.codiceFiscalePartitaIva', label:'Cod. Fiscale/P. IVA'},
    {value:'datiRichiesta.ubicazioneOccupazione', label:'Indirizzo'},
    {value:'municipio.id', label:'N. Municipio'},
    {value:'utentePresaInCarico.username', label:'Istruttore'}
  ];

  get isFaseIntegrazioneMassimaRaggiunta(): boolean {
    return this.pratica?.contatoreRichiesteIntegrazioni == environment.maxRichiesteIntegrazione;
  }

  get isFaseDiniego(): boolean {
    return this.pratica?.flagProceduraDiniego;
  }

  get isFlagEsenzioneModificato(): boolean {
    return this.pratica?.datiRichiesta.flagEsenzioneMarcaDaBolloModificato ? true : false;
  }

  get isPraticaEsente(): boolean {
    return this.pratica?.datiRichiesta?.flagEsenzioneMarcaDaBollo;
  }

  valoreProtocollo(el){
    this.numeroProtocollo = this.getProtocollo(el);
  }

  onApriMappaClicked(): void {
    if(!this.disableCheckbox){
      const emitter = new EventEmitter();
      emitter.subscribe((next) => {
        this.skipCheckOccupazioneChange(next);
      })
      let data = {
        elements: this.mappaElements,
        fullScreen: true,
        enableHeatmap: true,
        showDetailButton: false,
        test: false,
        skipCheckOccupazioneChange: emitter
      }

      let dialogRef = this.dialogService.open(MappaComponent, this.utilityService.configDynamicDialogFullScreen(data, "Ubicazione pratica cittadino"));

      dialogRef.onClose.subscribe((element) => {
        if (element) {
          //this.dettaglioPratica(el);
        }
      });
    }
  }

  columnsSchema = [
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
      type: "dropdown",
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
      type: "dropdown"
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
      key: 'esenzioneCupAction',
      icon: "pi pi-wallet",
      tooltip: 'ESENZIONE CUP',
      hidden: (el) => {
        return el.flagEsenzionePagamentoCUP ||
          this.authService.getGroup() === environment.groups.idGruppoDirettoreMunicipio ||
          el.tipoProcesso.id === environment.processes.rinunciaConcessione;
      }
    },
    {
      key: 'validaPraticaDialog',
      icon: "pi pi-check-circle",
      tooltip: 'VALIDA',
      hidden: (el) => {
        return this.authService.getGroup() != environment.groups.idGruppoIstruttoreMunicipio;
      }
    },
  ];

  constructor(
    private spinnerService: SpinnerDialogService,
    private praticaService: PraticaService,
    private authService: AuthService,
    private messageService: MessageService,
    protected dialogService: DialogService,
    protected utilityService: UtilityService,
    public confirmationService: ConfirmationService,
    private tipologicheService: TipologicheService,
    private allegatiService: AllegatiService
  ) {
    super(dialogService, utilityService);
  }

  getVerificaOccupazione(){
    return environment.validaPratica.verificaOccupazione;
  }

  getSaltaVerificaOccupazione(){
    return environment.validaPratica.saltaVerificaOccupazione;
  }
  getOccupazioneCorretta(){
    return environment.validaPratica.occupazioneCorretta;
  }

  ngOnInit(): void {
    this.tipologicheService.getGruppi().subscribe(
      (data: GruppoDTO[]) => {
        this.tipoGruppi = data;
        this.attorePareri = this.tipoGruppi.find((x: GruppoDTO) => x.id == environment.groups.idGruppoPoliziaLocale);
      }
    );
    this.cercaPratiche();
  }

  cercaPratiche() {
    let praticaVerificaFormale = new RicercaPraticaRequest();
    praticaVerificaFormale.idsMunicipi = this.authService.getMunicipiAppartenenza();
    praticaVerificaFormale.idsStatiPratica = [environment.statiPratica.verificaFormale];

    this.spinnerService.showSpinner(true);
    this.praticaService.getPratiche(praticaVerificaFormale, this.authService.getLoggedUser()).subscribe(
      (data) => {
        this.spinnerService.showSpinner(false);
        this.dataSource = data.content.map(el => Object.setPrototypeOf(el, PraticaDto.prototype));
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage('Ricerca pratiche', err);
      });
  }

  validaPraticaDialog(element: PraticaDto) {
    this.showValidaDialog = true;

    this.pratica = element;
    this.isEsenteOnInit = this.isEsente = this.pratica.datiRichiesta.flagEsenzioneMarcaDaBollo;
    this.idSelectedPratica = this.pratica.id;
    this.coordUbicazioneTemporanea = element.datiRichiesta.coordUbicazioneTemporanea;
    this.mappaElements.push(this.pratica);
    this.idUtentePresaInCarico = this.pratica.utentePresaInCarico.id;
    if (this.pratica.datiRichiesta.tipoOperazioneVerificaOccupazione != null){
      this.sceltaValidazione = this.pratica.datiRichiesta.tipoOperazioneVerificaOccupazione;
      this.disableCheckbox = true;
    }
    if (this.isRinunciaConcessione()) {
      this.sceltaValidazione = this.getSaltaVerificaOccupazione();
    } else if (!this.disableCheckbox) {
      this.sceltaValidazione = null;
    }
    this.valoreProtocollo(this.pratica);
  }

  async inviaRichiestaPareri(element: PraticaDto) {
    this.aggiornaRichiestaPareri();
  }

  closeValidaDialog(){
    this.showValidaDialog = false;
    this.sceltaValidazione = '';
    this.mappaElements = [];
    this.disableCheckbox = false;
  }

  disableControl(){
    if(this.isFaseDiniego){
      return false;
    } else if (this.isRinunciaConcessione() && this.isFaseIntegrazioneMassimaRaggiunta)
    {
      return true;
    } else
    {
      return !this.noteIstruttoreMunicipio.trim() || this.checkFileInCheckout;
    }
  }

  async closeIntegrazioneDialog(event?) {
    this.showIntegrazioneDialog = false;
    if(this.newRichiestaIntegrazioneUploadedFiles.length > 0) {
      const promises = this.newRichiestaIntegrazioneUploadedFiles.map((allegato, index) => {
        return this.allegatiService.deleteAllegato(allegato.id).toPromise();
      });
      Promise.all(promises).finally(() => {
        this.newRichiestaIntegrazioneUploadedFiles = [];
      });
    } else
      this.clearData();
    this.showIntegrazioneDialog = false;
  }

  clearData(){
    this.noteIstruttoreMunicipio = '';
    this.showIntegrazioneDialog = false;
    setTimeout(() => {
      this.pratica = JSON.parse(JSON.stringify(this.pratica));
    }, 100);
  }

  async aggiornaRichiestaPareri() {
    this.spinnerService.showSpinner(true);
    this.actionRiprotocollazione = 'aggiornaRichiestaPareri';
    this.spinnerService.showSpinner(true);
    if (this.disableCheckbox == false) {
      let response;
      try {
        response = await this.inviaVerificaOccupazione();
        this.skipCheckOccupazione = false;
        if(response) {
          this.richiestaPareriPoliziaLocale()
        }
      } catch(err) {
        this.spinnerService.showSpinner(false);
        if (err.error.code == 'E19') {
          this.skipCheckOccupazione = true;
          this.errorDialogOverlap = true;
          this.errorMessageOverlap = this.utilityService.accapoMessage(err.error.message.slice(0, 300)) + "<br> Vuoi proseguire comunque confermando la verifica di occupazione?";
        } else {
          this.messageService.showErrorMessage('Check pratica', err);
        }
      }
    } else {
      this.richiestaPareriPoliziaLocale();
    }
  }

  richiestaPareriPoliziaLocale() {
    this.actionRiprotocollazione = 'richiestaPareriPoliziaLocale';
    this.praticaService.statoRichiestaPareri(this.idSelectedPratica, this.idUtentePresaInCarico, this.riabilitaEsenzioneMarcaDaBollo).subscribe(
      (data: RichiestaParereDTO) => {
        this.riabilitaEsenzioneMarcaDaBollo = false;
        this.spinnerService.showSpinner(false);
        this.messageService.showMessage('success', 'Aggiornamento pratica', 'La pratica è passata in stato Richiesta pareri al gruppo ' + this.attorePareri.descrizione);
        this.closeValidaDialog();
        const parere = data;
        if(parere !== null) {
          this.isProtocollata = !!parere.codiceProtocollo;
          this.numProtocollo = `${parere?.codiceProtocollo || '--|--'}`;
          this.showProtocolloDialog = true;
          this.errorDialogOverlap = false;
          this.errorMessageOverlap = "";
        } else {
          this.spinnerService.showSpinner(false);
          this.messageService.showErrorMessage('Aggiornamento pratica', "Errore interno");
        }
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage('Aggiornamento pratica', err);
        this.numProtocollo = '--|--';
        this.showProtocolloDialog = true;
      });
  }

  async invioRichiestaIntegrazione() {
    const idAllegato = this.newRichiestaIntegrazioneUploadedFiles.length > 0 ? this.newRichiestaIntegrazioneUploadedFiles[0].id : null;
    this.idAllegatoRichiestaIntegrazione = idAllegato;
    if (this.isEsente && this.annullaEsenzioneMarcaDaBollo) {
      this.isEsente = false;
    }
    if (this.riabilitaEsenzioneMarcaDaBollo) {
      this.isEsente = true;
    }
    if (this.isPraticaEsente && this.isFlagEsenzioneModificato) {
      this.isEsente = true;
    }

    this.statoRichiestaIntegrazione(this.idSelectedPratica, this.noteIstruttoreMunicipio, this.isEsente, idAllegato);
    /*try {
      const response = await this.inviaVerificaOccupazione();
      this.skipCheckOccupazione = false;
      if (response) {
      }
    } catch(err) {
      this.spinnerService.showSpinner(false);
      if (err.error.code === 'E19') {
        this.skipCheckOccupazione = true;
        this.errorDialogOverlapRichiestaIntegrazione = true;
        this.errorMessageOverlapRichiestaIntegrazione = this.utilityService.accapoMessage(err.error.message.slice(0, 300)) + "<br> Vuoi proseguire comunque confermando la verifica di occupazione?";
      } else {
        this.messageService.showErrorMessage('Check pratica', err);
      }
    }*/
  }

  inviaVerificaOccupazione(): Promise<any> {
    if (this.sceltaValidazione == environment.validaPratica.verificaOccupazione) {
      this.coordUbicazioneTemporanea = this.utilityService.drawCoordinates;
    } else if (this.sceltaValidazione == environment.validaPratica.saltaVerificaOccupazione) {
      this.coordUbicazioneTemporanea = null;
    } else {
      this.coordUbicazioneTemporanea = this.pratica.datiRichiesta.coordUbicazioneTemporanea;
    }

    return this.praticaService.verificaOccupazione(this.idSelectedPratica, this.authService.getLoggedUser().userLogged.id,
      this.sceltaValidazione, this.coordUbicazioneTemporanea, this.skipCheckOccupazione).toPromise();
  }

  get successMessageIntegrazione(){
    let ret: string = 'La pratica è passata in stato "Necessaria integrazione"';

    if(this.isFaseIntegrazioneMassimaRaggiunta && !this.isFaseDiniego){
      ret = 'La pratica è passata in stato "Preavviso diniego"';
    } else if(this.isFaseDiniego){
      ret = 'La pratica è passata in stato "Pratica da rigettare"';
    }

    return ret;
  }

  statoRichiestaIntegrazione(idPratica: number, testoNota: string, flagEsenzioneMarcaDaBollo?: boolean, idAllegato?: number): void{
    /*if(this.disableCheckbox == false){
      await this.inviaVerificaOccupazione();
    }*/

    this.errorDialogOverlapRichiestaIntegrazione = false;
    this.actionRiprotocollazione = 'statoRichiestaIntegrazione';
    this.spinnerService.showSpinner(true);
    this.praticaService.statoRichiestaIntegrazione(idPratica, this.idUtentePresaInCarico, testoNota, flagEsenzioneMarcaDaBollo, idAllegato).subscribe(
      (data: RichiestaIntegrazioneDTO) => {
        this.newRichiestaIntegrazioneUploadedFiles = [];
        this.spinnerService.showSpinner(false);
        this.messageService.showMessage('success', 'Aggiornamento pratica', this.successMessageIntegrazione);
        this.showIntegrazioneDialog = false;
        this.closeValidaDialog();
        if (!this.isFaseDiniego) {
          const parere = data;
          this.isProtocollata = !!parere.codiceProtocollo;
          this.numProtocollo = `${parere?.codiceProtocollo || '--|--'}`;
          this.showProtocolloDialog = true;
        } else {
          this.cercaPratiche();
        }
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage('Aggiornamento pratica', err);
        if(!this.isFaseDiniego) {
          this.numProtocollo = '--|--';
          this.showProtocolloDialog = true;
        }
      });
  }

  isRinunciaConcessione(){
    if(this.pratica?.tipoProcesso.id == environment.processes.rinunciaConcessione){
      return true;
    }

    return false;
  }

  approvaPratica() {
    this.spinnerService.showSpinner(true);
    this.praticaService.approvaPratica(this.pratica.id, this.authService.getLoggedUser().userLogged.id).subscribe(
      (data: PraticaDto) => {
        this.spinnerService.showSpinner(false);
        this.messageService.showMessage('success', 'Approvazione pratica', "Pratica approvata con successo");
        this.closeValidaDialog();
        this.cercaPratiche();
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage("Approvazione pratica", err);
      }
    );
  }

  confirmDisabled: boolean = true;
  onMandatoryDocuments(event: boolean) {
    this.confirmDisabled = !event;
  }

  checkFileInCheckout: boolean = false;
  getIdDocIntegrazioneUploaded(resp) {
    this.doc_richiesta_integrazione_diniego.id_blob = resp.id;
    this.checkFileInCheckout = false;
  }

  fileInCheckout(status_checkout) {
    this.checkFileInCheckout = status_checkout;
  }


  esenzioneCupAction(element: PraticaDto) {
    this.showMotivazioneEsenzioneCupDialog = true;
    this.pratica = element;
  }

  confermaEsenzioneCup() {
    this.showEsenzioneCupConfirmDialog = true;

    this.spinnerService.showSpinner(true);
    this.praticaService.dichiaraEsenzioneCup({
      idPratica: this.pratica.id,
      flagEsenzionePagamentoCUP: true,
      motivazioneEsenzionePagamentoCup: this.motivazioneEsenzioneCup
    }).subscribe(res => {
      this.motivazioneEsenzioneCup = '';
      this.spinnerService.showSpinner(false);
      this.showEsenzioneCupConfirmDialog = false;
      this.messageService.showMessage('success','Esenzione CUP',"Dichiarazione esenzione CUP inserita con successo");
      this.cercaPratiche();
    }, err => {
      this.motivazioneEsenzioneCup = '';
      this.spinnerService.showSpinner(false);
      this.messageService.showErrorMessage("Esenzione CUP", err);
    });
  }

  closeProtocolloDialog(event?) {
    this.showProtocolloDialog = false;
    this.isProtocollata = false;
    this.numProtocollo = '';
    this.actionRiprotocollazione = '';
    this.cercaPratiche();
  }

  onRichiestaIntegrazioneUploadedFile(file) {
    this.newRichiestaIntegrazioneUploadedFiles.push(file);
  }

  showIntegrazione() {
    this.showIntegrazioneDialog = true;
    this.annullaEsenzioneMarcaDaBollo = false;
  }

  skipCheckOccupazioneChange(event) {
    this.skipCheckOccupazione = event;
  }
}
