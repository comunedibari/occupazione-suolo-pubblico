import { Component, EventEmitter, Inject, Input, OnChanges, OnDestroy, OnInit, Output, Renderer2, ViewChild } from '@angular/core';
import { AuthService } from '@services/auth/auth.service';
import { MessageService } from '@services/message.service';
import { UtilityService } from '@services/utility.service';
import { ConfirmationService } from 'primeng/api';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { PraticaDto } from '@models/dto/pratica-dto';
import { TipologicheService } from '@services/tipologiche.service';
import { SpinnerDialogService } from '@core-components/layout/spinner-dialog/services/spinner-dialog.service';
import { UploadFileElement } from './model/upload-file-element';
import { AllegatoDTO } from '@models/dto/allegato-dto';
import { AllegatiService } from '@services/allegati.service';
import { environment } from 'environments/environment';
import { AllegatoPraticaInsertRequest } from '@models/allegato-pratica-insert-request';
import { UploadDialogConfig } from './model/upload-dialog-config';
import { forkJoin, Observable } from 'rxjs';
import { TipoAllegatoGruppoStatoProcessoDTO } from '@models/dto/tipo-allegato-gruppo-stato-processo-dto';
import { TipoAllegatoDTO } from '@models/dto/tipo-allegato-dto';
import { FileUpload } from 'primeng/fileupload';
import { DestinazioneAllegato } from './enums/destinazione-allegato.enum';
import { Mode } from './enums/mode.enum';
import { RichiestaParereDTO } from '@models/dto/richiesta-parere-dto';
import { RichiestaIntegrazioneDTO } from '@models/dto/richiesta-integrazione-dto';
import { AllegatoParereInsertRequest } from '@models/allegato-parere-insert-request';
import { AllegatoIntegrazioneInsertRequest } from '@models/allegato-integrazione-insert-request';
import { AllegatoRichiestaIntegrazioneInsertRequest } from '@models/allegato-richiesta-integrazione-insert-request';

@Component({
  selector: 'app-upload-file',
  templateUrl: './upload-file.component.html',
  styleUrls: ['./upload-file.component.css']
})
export class UploadFileComponent implements  OnInit, OnChanges, OnDestroy {
  @Input() pratica: PraticaDto;
  @Input() mode: Mode; //single or multiple
  @Input() dettaglioPratica: boolean = false;
  @Input() readonly: boolean = false;
  @Input() destinazioneAllegato: DestinazioneAllegato = null;
  @Input() idGruppoDestinatarioParere: number = null;
  @Input() filtroStatoPratica: number = null;
  @Input() rettificaConcessione: boolean = false;
  @Input() filtroTipoProcesso: number = null;
  @Input() fileClearToggle: boolean;
  @Input() richistaIntegrazioneFiles: AllegatoDTO[];
  @Input() acceptedExtensions: string = '.pdf,.doc,.docx,.jpg,.png';
  @Input() nonCompetenza: boolean = false;
  @Output() idDocUploaded: EventEmitter<AllegatoDTO> = new EventEmitter<AllegatoDTO>();
  @Output() docRemoved: EventEmitter<number> = new EventEmitter<number>();
  @Output() mandatoryDocuments: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() fileInCheckout: EventEmitter<any> = new EventEmitter<any>();
  @Output() onDocUploaded: EventEmitter<any> = new EventEmitter<any>();

  @ViewChild("fp") fp: FileUpload;

  invalidFileSizeMessageSummary: string = '{0}: Dimensione file non valida, ';
  invalidFileSizeMessageDetail: string = 'dimensione massima upload {0}.';
  invalidFileTypeMessageSummary: string = '{0}: Tipo file non valido, ';
  invalidFileTypeMessageDetail: string = 'tipi di file accettati: {0}.';
  invalidFileLimitMessageDetail: string = 'il limite massimo è {0} file.';
  invalidFileLimitMessageSummary: string = 'Numero massimo di file caricabili superato:';
  loading: boolean = false;
  uploadedFiles: AllegatoDTO[] = [];
  documentiSelezionati: number[] = [];
  customLabel: string[] = [];
  tipoDocumenti: UploadFileElement[] = [];
  allTipoAllegati: TipoAllegatoDTO[] = [];
  tooltipDocs: string = '';
  dialogConfig: UploadDialogConfig = null;
  scrollHeightTipoFile: string = "100px";
  disableUpload: boolean = false;

  newFiles = new Set<Number>();

  constructor(
    private messageService: MessageService,
    private utilityService: UtilityService,
    public confirmationService: ConfirmationService,
    public authService: AuthService,
    private renderer: Renderer2,
    private dialogRef: DynamicDialogRef,
    private tipologicheService: TipologicheService,
    private spinnerService: SpinnerDialogService,
    private allegatiService: AllegatiService,
    @Inject(DynamicDialogConfig) config: any) {
      if (config.data) {
        console.log(config);
        let myConfig: UploadDialogConfig = config.data;
        this.pratica = myConfig.pratica;
        this.mode = myConfig.mode;
        this.dettaglioPratica = myConfig.dettaglioPratica;
        this.readonly = myConfig.readonly;
        if (myConfig.destinazioneAllegato != null && myConfig.destinazioneAllegato != undefined) {
          this.destinazioneAllegato = myConfig.destinazioneAllegato;
        }
        if (myConfig.idGruppoDestinatarioParere != null && myConfig.idGruppoDestinatarioParere != undefined) {
          this.idGruppoDestinatarioParere = myConfig.idGruppoDestinatarioParere;
        }
        if (myConfig.filtroStatoPratica != null && myConfig.filtroStatoPratica != undefined) {
          this.filtroStatoPratica = myConfig.filtroStatoPratica;
        }
        if (myConfig.onDocUploaded) {
          this.onDocUploaded = myConfig.onDocUploaded;
        }
        this.scrollHeightTipoFile = "200px";
        this.dialogConfig = config;
      }
  }

  ngOnDestroy() {
    if(!this.readonly && this.isMultipleFile && this.dialogConfig)
      this.renderer.removeClass(document.body, 'modal-open');
  }

  get isSingleFile(): boolean {
    return this.mode == Mode.SINGLE ? true : false;
  }

  get isMultipleFile(): boolean {
    return this.mode == Mode.MULTIPLE ? true : false;
  }

  ngOnInit() {
    if(!this.readonly && this.isMultipleFile && this.dialogConfig) {
      this.renderer.addClass(document.body, 'modal-open');
    }
    this.init();
  }

  ngOnChanges(change) {
    console.log(this.readonly);
    this.init();
    if(change.fileClearToggle !== undefined && this.fp !== undefined) {
      this.fp.clear();
      this.fp.uploadedFileCount = 0;
    }
  }

  removeDisabled(file: AllegatoDTO){
    console.error(JSON.stringify(file));
    return this.filtroTipoProcesso === environment.processes.rettificaErrori &&
    file.tipoAllegato.descrizione == 'Determina di rettifica' && !this.newFiles.has(file.id);
  }

  get fileTypeClass(): string {
    let ret = 'p-col-4';
    if (this.destinazioneAllegato == DestinazioneAllegato.INTEGRAZIONE){
      ret = 'p-col-5';
    }

    return ret;
  }

  get textAreaClass(): string {
    let ret = 'p-col-3';
    if (this.destinazioneAllegato == DestinazioneAllegato.INTEGRAZIONE){
      ret = 'p-col-2';
    }

    return ret;
  }

  private init() {
    if(this.pratica) {
      this.initDocuments();
    }
  }

  get isNewInstance(): boolean {
    return this.pratica == null || this.pratica.statoPratica.id == environment.statiPratica.bozza;
  }

  get isIntegrationInstance(): boolean {
    return false;
  }

  get isIntegrazioneDecadenza(): boolean {
    return false;
  }

  get isAttesaPagamento(): boolean {
    return false;
  }

  private findRichiestaParere(): RichiestaParereDTO {
    let ret: RichiestaParereDTO = null;
    if (this.pratica && this.pratica.richiestePareri != null && this.pratica.richiestePareri.length > 0) {
      ret = this.pratica.richiestePareri.find(
        (x: RichiestaParereDTO) =>
          x.flagInseritaRisposta === false && x.idGruppoDestinatarioParere === this.idGruppoDestinatarioParere
      );
    }
    return ret;
  }

  private findRichiestaIntegrazione(): RichiestaIntegrazioneDTO {
    let ret: RichiestaIntegrazioneDTO = null;
    if (this.pratica && this.pratica.richiesteIntegrazioni !== null && this.pratica.richiesteIntegrazioni.length > 0) {
      ret = this.pratica.richiesteIntegrazioni.find((x: RichiestaIntegrazioneDTO)=>x.integrazione==null);
    }

    return ret;
  }

  private initDocuments() {
    this.loading = true;
    this.spinnerService.showSpinner(true);
    this.mandatoryDocuments.emit(false);
    let calls: Observable<any>[] = [];
    if(this.filtroTipoProcesso != null) {
      calls.push(this.tipologicheService.getAllegati(this.authService.getLoggedUser(),
      this.pratica.statoPratica.id, this.filtroTipoProcesso));
    } else {
      calls.push(this.tipologicheService.getTipiAllegati(this.authService.getLoggedUser(), this.pratica));
    }
    switch (this.destinazioneAllegato) {
      case DestinazioneAllegato.PRATICA:
        if(this.dettaglioPratica){
          let obsAllegati: Observable<AllegatoDTO[]> = this.allegatiService.documentalePratica(this.pratica.id);
          calls.push(obsAllegati);
        } else if(this.filtroStatoPratica !== null) {
          let obsAllegati: Observable<AllegatoDTO[]> = this.allegatiService.getAllegatiPraticaPerStato(this.pratica.id, this.filtroStatoPratica);
          calls.push(obsAllegati);
        } else {
          let obsAllegati: Observable<AllegatoDTO[]> = this.allegatiService.getAllegatiPratica(this.pratica.id, this.pratica.tipoProcesso.id);
          calls.push(obsAllegati);
        }
        break;
      case DestinazioneAllegato.INTEGRAZIONE:
        let obsAllegati: Observable<AllegatoDTO[]>;
        if (this.filtroStatoPratica !== null) {
          obsAllegati = this.allegatiService.getAllegatiPraticaPerStatoAndTipoProcesso(
            this.pratica.id,
            this.filtroStatoPratica,
            this.pratica.tipoProcesso.id
          );
        } else {
          obsAllegati = this.allegatiService.getAllegatiPratica(this.pratica.id, this.pratica.tipoProcesso.id);
        }
        calls.push(obsAllegati);
        break;
      case DestinazioneAllegato.PARERE:
        let richiestaParere: RichiestaParereDTO = this.findRichiestaParere();
        if (richiestaParere) {
          let obsAllegati: Observable<AllegatoDTO[]> = this.allegatiService.getAllegatiRichiestaParere(richiestaParere.id);
          calls.push(obsAllegati);
        }
        break;
      case DestinazioneAllegato.RICHIESTA_INTEGRAZIONE:
        break;
    }

    forkJoin(calls).subscribe(
      next => {
        let tipiAllegati: TipoAllegatoGruppoStatoProcessoDTO[] = next[0];
        if (calls.length == 2) {
          let docs: AllegatoDTO[] = next[1];
          this.initDocumentiPratica(docs);
        }
        if(this.destinazioneAllegato === DestinazioneAllegato.RICHIESTA_INTEGRAZIONE && this.richistaIntegrazioneFiles.length === 0) {
          this.initDocumentiPratica([]);
        }
        this.initTipiAllegati(tipiAllegati);
        this.updateDocumentState();
        this.notifyMandatoryDocuments();
        if(
          this.destinazioneAllegato === DestinazioneAllegato.RICHIESTA_INTEGRAZIONE &&
          this.uploadedFiles.length > 0
        ) {
          this.disableUpload = true;
        } else {
          this.disableUpload = false;
        }

        this.loading = false;
        this.spinnerService.showSpinner(false);
      },
      err => {
        this.manageInitError(err);
      }
    );
  }

  private initTipiAllegati(tipiAllegati: TipoAllegatoGruppoStatoProcessoDTO[]): void {
    this.tipoDocumenti = tipiAllegati.map(
      (elem: TipoAllegatoGruppoStatoProcessoDTO)=> {
        const result: UploadFileElement = elem as UploadFileElement;
        result.flagObbligatorio = this.checkMandatoryFile(result);
        return result;
    }).sort((a: UploadFileElement, b: UploadFileElement) => {
      if (a.flagObbligatorio === b.flagObbligatorio) {
        return 0;
      } else {
        return a.flagObbligatorio ? -1 : 1;
      }
    });
    this.tooltipDocs = 'I documenti da allegare sono i seguenti:\n\n';
    this.tipoDocumenti.forEach((el: UploadFileElement) => {
      this.tooltipDocs += `- ${el.tipoAllegato.descrizione}${this.checkMandatoryFile(el) ? ' (*)' : ''}\n`;
    });
    this.tooltipDocs += '\nN.B.: il simbolo (*) indica i documenti obbligatori.'
  }
  private alreadyUploaded(el: UploadFileElement): boolean {
    if (this.uploadedFiles != null) {
      return this.uploadedFiles.find(f => f.tipoAllegato.id === el.tipoAllegato.id) != null;
    } else {
      return false;
    }
  }
  private checkMandatoryFile(el: UploadFileElement): boolean {
    return el.flagObbligatorio && !this.nonCompetenza && !this.alreadyUploaded(el);
  }

  private initDocumentiPratica(docs: AllegatoDTO[]): void {
    this.uploadedFiles = docs;
    docs.forEach((allegato: AllegatoDTO)=> {
      this.idDocUploaded.emit(allegato);
    });
  }

  private manageInitError(err: any): void {
    this.loading = false;
    this.spinnerService.showSpinner(false);
    this.messageService.showErrorMessage("Caricamento documenti", err);
  }

  public getNumeroProtocollo(allegato: AllegatoDTO): string|null {

    let ret: string = "--";
    if (allegato.codiceProtocollo) {
      ret = allegato.codiceProtocollo;
    }
    /*
    if (allegato.idIntegrazione) {
      let richiestaIntegrazione: RichiestaIntegrazioneDTO = this.pratica.richiesteIntegrazioni.find((x: RichiestaIntegrazioneDTO)=>x.integrazione != null && x.integrazione.id === allegato.idIntegrazione);
      ret = richiestaIntegrazione.integrazione.codiceProtocollo;
    }
    else if (allegato.idParere) {
      let richiestaParere: RichiestaParereDTO = this.pratica.richiestePareri.find((x: RichiestaParereDTO)=>x.parere != null && x.parere.id===allegato.idParere);
      ret = richiestaParere.parere.codiceProtocollo;
    }
    else if (allegato.idRichiestaIntegrazione) {
      ret = "--";
    }
    else if (allegato.idRichiestaParere) {
      ret = "--";
    }
    else if (allegato.idPratica) {
      if (this.pratica.protocolli) {
        let protocollo: ProtocolloDTO = null;
        protocollo = this.pratica.protocolli.find((x:ProtocolloDTO)=>x.idStatoPratica===environment.statiPratica.inserita);
        if (protocollo) {
          ret = protocollo.codiceProtocollo;
        }
      }
    }*/

    return ret;
  }

  async uploadFiles($event) {
    if(!this.checkDocsTipizzati()) {
      this.messageService.showMessage('error','Upload File', 'E\' necessario tipizzare tutti i documenti per eseguire l\'upload');
      this.fp.uploading=false;
      this.fp.disabled=false;
      this.fp.uploadedFileCount = 0;
    }
    else if(!this.checkDocsObbligatori()) {
      this.messageService.showMessage('error','Upload File', 'E\' necessario caricare tutti i file obbligatori per procedere con l\'upload');
      this.fp.uploading=false;
      this.fp.disabled=false;
      this.fp.uploadedFileCount = 0;
    }
    else if(!this.checkDocsAltroTipizzati()) {
      this.messageService.showMessage('error','Upload File', 'E\' necessario tipizzare tutti i file di tipo \'Altro\'');
      this.fp.uploading=false;
      this.fp.disabled=false;
      this.fp.uploadedFileCount = 0;
    }
    else {
      this.spinnerService.showSpinner(true);
      this.fp.uploading=true;
      let flagError = false;
      this.loading = true;
      for(let index = 0; index < this.fp.files.length && flagError==false; index++) {
        let file: File = this.fp.files[index];
        let extensionFile = file.name.substring(file.name.lastIndexOf('.') + 1);

        if(this.acceptedExtensions.indexOf(extensionFile) != -1) {
          let blob:string = (await this.utilityService.convertFileToBase64(file)) as string;
          blob = blob.substring(blob.indexOf(",")+1);
          let allegato: AllegatoDTO = new AllegatoDTO();
          allegato.nomeFile = file.name;
          allegato.mimeType = file.type;
          let tipoAllegato: TipoAllegatoDTO = new TipoAllegatoDTO();
          tipoAllegato.id = this.documentiSelezionati[index];
          allegato.tipoAllegato = tipoAllegato;
          allegato.fileAllegato = blob;
          allegato.nota = this.customLabel[index]?this.customLabel[index]:null;
          let reqObservable: Observable<AllegatoDTO>;

          switch (this.destinazioneAllegato) {
            case DestinazioneAllegato.PRATICA:
              let requestPratica: AllegatoPraticaInsertRequest = new AllegatoPraticaInsertRequest();
              requestPratica.idPratica = this.pratica.id;
              requestPratica.allegato = allegato;
              reqObservable = this.allegatiService.sendAllegatoPratica(requestPratica);
              break;
            case DestinazioneAllegato.INTEGRAZIONE:
              let requestIntegrazione: AllegatoIntegrazioneInsertRequest = new AllegatoIntegrazioneInsertRequest();
              let richiestaIntegrazione: RichiestaIntegrazioneDTO = this.findRichiestaIntegrazione();
              requestIntegrazione.idRichiestaIntegrazione = richiestaIntegrazione.id;
              requestIntegrazione.allegato = allegato;
              reqObservable = this.allegatiService.sendAllegatoIntegrazione(requestIntegrazione);
              break;
            case DestinazioneAllegato.PARERE:
              let requestParere: AllegatoParereInsertRequest = new AllegatoParereInsertRequest();
              let richiestaParere: RichiestaParereDTO = this.findRichiestaParere();
              requestParere.idRichiestaParere = richiestaParere.id;
              requestParere.allegato = allegato;
              reqObservable = this.allegatiService.sendAllegatoParere(requestParere);
              break;
            case DestinazioneAllegato.RICHIESTA_INTEGRAZIONE:
              let requestRichiestaIntegrazione: AllegatoRichiestaIntegrazioneInsertRequest = new AllegatoRichiestaIntegrazioneInsertRequest();
              requestRichiestaIntegrazione.allegato = allegato;
              reqObservable = this.allegatiService.sendAllegatoRichiestaIntegrazione(requestRichiestaIntegrazione);
              break;
          }
          this.spinnerService.showSpinner(true);
          await reqObservable.toPromise()
          .then(
            (resp: AllegatoDTO) => {
              this.newFiles.add(resp.id);
              this.uploadedFiles.push(resp);
              this.idDocUploaded.emit(resp);
              this.onDocUploaded.emit(resp);
              this.fileInCheckout.emit(false);
          }).catch(err => {
            this.messageService.showErrorMessage('Upload file', err);
            flagError = true;
          });
        }
      }

      if (!flagError) {
        this.documentiSelezionati = [];
        this.customLabel = [];
        this.nomiUltimiFiles = [];
        this.fp.clear();
        this.fp.uploadedFileCount = 0;
        setTimeout(async() => {
          this.init();
          this.messageService.showMessage('success','Upload File', 'Upload dei file ultimato');
        }, 1000);
      }

      this.fp.uploading=false;
      this.loading = false;
      this.spinnerService.showSpinner(false);
    }
  }

  checkDocsTipizzati(): boolean {
    return this.fp.files.length == Object.keys(this.documentiSelezionati).length? true : false;
  }

  isButtonInviaDisabled(): boolean {
    let ret: boolean = this.loading || !this.checkDocsObbligatori() || this.nomiUltimiFiles.length > 0;
    return ret;
  }

  isTestoLibero(id: number) {
    let ret: boolean = false;
    if (id) {
      let tipoDoc: UploadFileElement = this.findTipoDocumento(id);
      if (tipoDoc) {
        ret = this.findTipoDocumento(id).flagTestoLibero;
      }
    }
    return ret;
  }

  private getRequiredDocsNumber(): number {
    return this.tipoDocumenti.filter((element: UploadFileElement)=> element.flagObbligatorio).length;
  }

  checkDocsObbligatori(): boolean {
    let requiredDocsNumber = this.getRequiredDocsNumber();
    let requiredDocsMap: Map<number, number> = new Map<number, number>();

    this.documentiSelezionati.forEach(
      (elem:number)=> {
        if (this.findTipoDocumento(elem).flagObbligatorio) {
          requiredDocsMap.set(elem, 1);
        }
      }
    );

    this.uploadedFiles.forEach(
      (elem: AllegatoDTO) => {
        if (this.findTipoDocumento(elem.tipoAllegato.id)?.flagObbligatorio) {
          requiredDocsMap.set(elem.tipoAllegato.id, 1);
        }
      }
    );

    return requiredDocsMap.size >= requiredDocsNumber;
  }

  private notifyMandatoryDocuments() {
    let event = this.checkDocsObbligatori();
    let forceRequiredDocs = true;

    // Sulle integrazioni non ci sono documenti obbligatori: occorre verificare se esiste
    // almeno 1 allegato alla richiesta di integrazione corrente
    let countIntegrazione = -1;
    if (this.destinazioneAllegato === DestinazioneAllegato.INTEGRAZIONE) {
      let richiestaIntegrazione: RichiestaIntegrazioneDTO = this.findRichiestaIntegrazione();
      if (richiestaIntegrazione) {
        countIntegrazione = richiestaIntegrazione.id;
      }
    }

    this.uploadedFiles.forEach(
      (elem: AllegatoDTO) => {
        if (countIntegrazione != -1 && countIntegrazione === elem.idRichiestaIntegrazione) {
          countIntegrazione = -2; // Tovato allegato salvato in precedenza sulla richiesta integeazione
        }
      }
    );

    if (this.destinazioneAllegato === DestinazioneAllegato.INTEGRAZIONE && countIntegrazione !== -2) {
      // Non sono stati salvati in precedenza allegati alla richiesta di integrazione
      forceRequiredDocs = false;
    }

    //let requiredDocsNumber = this.getRequiredDocsNumber();
    //event = event || requiredDocsNumber==0;
    this.mandatoryDocuments.emit(event && forceRequiredDocs);
  }


  checkDocsAltroTipizzati(): boolean {
    let ctrNotDescripted = 0;
    this.documentiSelezionati.forEach(
      (elem: number, i:number)=>
      {
        let tipoDoc: UploadFileElement = this.findTipoDocumento(elem);
        if (tipoDoc && tipoDoc.flagTestoLibero && (!this.customLabel[i] || this.customLabel[i].trim().length==0)) {
          ctrNotDescripted++;
        }
      }
    );
    return ctrNotDescripted == 0;
  }

  public findTipoDocumento(id: number): UploadFileElement {
    return this.tipoDocumenti.find((x:UploadFileElement)=>x.tipoAllegato.id==id);
  }

  updateDocumentState(){
    this.tipoDocumenti.forEach(
      (elem: UploadFileElement)=>{elem.disabled=false;}
    );
    this.documentiSelezionati.forEach(
      (elem: number)=> {
        let tipo: UploadFileElement = this.findTipoDocumento(elem);
        if (tipo && !tipo.flagTestoLibero) {
          tipo.disabled = true;
        }
      }
    );
/*
    this.uploadedFiles.forEach(
      (elem: AllegatoDTO)=> {
        let tipo: UploadFileElement = this.findTipoDocumento(elem.idTipoAllegato);
        if (tipo && !tipo?.flagTestoLibero) {
          tipo.disabled = true;
        }
      }
    );*/
  }

  onChangeDropDown(event){
    this.updateDocumentState();
  }


  onRemoveFile(index: number) {
    this.notifyRemovedDocument(this.documentiSelezionati[index]);
    this.documentiSelezionati.splice(index, 1);
    if (this.customLabel[index]) {
      this.customLabel.splice(index, 1);
    }
    this.updateDocumentState();

    this.notifyMandatoryDocuments();
  }

  private notifyRemovedDocument(event: number) {
    this.docRemoved.emit(event);
  }


  downloadFile(index) {
    const doc = this.uploadedFiles[index];
    this.downloadDoc(doc);
  }

  downloadDoc(allegato: AllegatoDTO){
    this.allegatiService.getAllegato(allegato.id).subscribe(
      (allegato: AllegatoDTO)=> {
        this.utilityService.downloadFile(allegato.nomeFile, allegato.mimeType, allegato.fileAllegato);
      },
      err=> {
        this.messageService.showErrorMessage('Donwload file', err);
      }
    );
  }

  removeDoc(index, file){
    //confirmationDialog elimazione file dopo upload
    this.confirmationService.confirm({
      icon: "pi pi-exclamation-triangle",
      acceptButtonStyleClass: "btn-custom-style btn-dialog-confirm",
      rejectButtonStyleClass: "btn-custom-style btn-dialog-confirm",
      acceptLabel: "Conferma",
      rejectLabel: "Annulla",
      header: "Eliminazione documento",
      message: "Confermi di voler eliminare il documento " + file.nomeFile + " ?",
      accept: () =>{
        this.allegatiService.deleteAllegato(this.uploadedFiles[index].id).subscribe(
          resp => {this.init();},
          err => {this.messageService.showErrorMessage("Upload file", err)}
        );
        this.fp.uploadedFileCount = 0;
      }
    });
  }

  public inviaRichiesta(): void {
    this.dialogRef.close(true);
  }

  nomiUltimiFiles: string[] = [];

  //in caso di pre-upload di files duplicati avvisa l'utente
  checkDuplicateFiles(event) {
    //rimuovo ulteriori duplicati da array contenente i nomi dei file inseriti nel pre-upload
    this.nomiUltimiFiles = this.removeDuplicates(this.nomiUltimiFiles);

    //creo due array con nomi dei files, quelli correnti e quelli selezionati dall'utente
    let nomiFilesCurrent = event.currentFiles.map(file => file.name);
    let nomiFilesSelected = [...event.files].map(file => file.name);

    //inserisco nell'array persistente di nomi dei files gli ultimi correnti
    nomiFilesCurrent.forEach(file => {
      this.nomiUltimiFiles.push(file);
    });

    //salvo in un array i nomi dei file duplicati dell'array persistente dei files
    let results = this.findDuplicates(this.nomiUltimiFiles);

    //per ogni file selezionato dall'utente verifico che non sia presente nell'array dei files duplicati
    nomiFilesSelected.forEach(fileSel => {
      results.forEach(file => {
        if (file == fileSel) {
          this.messageService.showMessage('warn','Upload File', 'File già presente tra i documenti inseriti');
        }
      })
    });
    this.selectedFile({ files: nomiFilesSelected });
  }

  //elimino dall'array persistente dei files in pre-upload il file selezionato dalla remove
  removeSingleFile(event) {
    this.nomiUltimiFiles = this.nomiUltimiFiles.filter(valore => {
      return valore != event.file.name;
    });
    this.selectedFile({ files: this.nomiUltimiFiles });
  }

  //restituisco array degli elementi duplicati
  findDuplicates = (arr) => {
    return arr.filter((nome, indice, array) => array.indexOf(nome) !== indice);
  }

  //rimuove i duplicati in un array
  removeDuplicates = (arr) => {
    return arr.filter((valore, indice, array) => array.indexOf(valore) === indice);
  }

  checkSpecialCharactersFieldName(index) {
    let isValid = this.utilityService.excludeSpecialCharacters(this.customLabel[index]);
    if(!isValid) {
      this.customLabel[index] = this.customLabel[index].replace(/[^A-Za-z0-9 ']/g, '').trim();
    }
  }

  selectedFile(event){
    if(event.files && event.files.length > 0)
      this.fileInCheckout.emit(true);
    else
      this.fileInCheckout.emit(false);
  }
  get isDeleteVisible() {
    return this.pratica?.statoPratica.id == environment.statiPratica.bozza;
  }
}
