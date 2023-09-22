import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, Validators } from '@angular/forms';
import { MessageService } from '@services/message.service';
import { AuthService } from '@services/auth/auth.service';
import { SpinnerDialogService } from '@core-components/layout/spinner-dialog/services/spinner-dialog.service';
import { PraticaService } from '@services/pratica.service';
import { RicercaPraticaRequest } from '@models/ricerca-pratica-request';
import { UtilityService } from '@services/utility.service';
import { environment } from 'environments/environment';
import { PraticaDto } from '@models/dto/pratica-dto';
import { DialogService } from 'primeng/dynamicdialog';
import { DestinazioneAllegato } from '@shared-components/upload-file/enums/destinazione-allegato.enum';
import { Mode } from '@shared-components/upload-file/enums/mode.enum';
import { TemplateDTO } from '@models/dto/template-dto';
import { TemplateService } from '@services/template.service';
import { TipologicheService } from '@services/tipologiche.service';
import { GruppoDTO } from '@models/dto/gruppo-dto';
import { ColumnSchema } from '@shared-components/table-prime-ng/models/column-schema';
import { Action } from '@shared-components/azioni-pratica/models/action';
import { RichiestaParereDTO } from '@models/dto/richiesta-parere-dto';
import { ParereDTO } from '@models/dto/parere-dto';
import { BasePraticaAction } from 'app/shared/classes/base-pratica-action';
import { GruppoDTODisabled } from '@models/dto/gruppo-dto-disabled';
import { UtenteDTO } from '@models/utente-dto';
import { Observable } from 'rxjs';
import { UserService } from '@services/user.service';
import { AllegatoDTO } from '@models/dto/allegato-dto';
import { ParereInsertRequest } from '@models/parere-insert-request';
import { Group } from '@enums/Group.enum';
import { RichiestaIntegrazioneDTO } from '@models/dto/richiesta-integrazione-dto';
import { AllegatiService } from '@services/allegati.service';
import {ConfirmationService} from "primeng/api";

@Component({
  selector: 'app-richiesta-pareri',
  templateUrl: './richiesta-pareri.component.html',
  styleUrls: ['./richiesta-pareri.component.css']
})
export class RichiestaPareriComponent extends BasePraticaAction implements OnInit {

  constructor(
    private spinnerService: SpinnerDialogService,
    private praticaService: PraticaService,
    private messageService: MessageService,
    protected dialogService: DialogService,
    protected utilityService: UtilityService,
    private tipologicheService: TipologicheService,
    private templateService: TemplateService,
    private allegatiService: AllegatiService,
    public authService: AuthService,
    private userService: UserService,
    private confirmationService: ConfirmationService
  ) {
    super(dialogService, utilityService);
  }

  destinazioneAllegato = DestinazioneAllegato.PARERE;
  uploadMode = Mode.MULTIPLE;
  idGruppoDestinatarioParere: number = null;

  dataSource: PraticaDto[];
  directionSortColumn = "1"; //1=asc  0=rand   -1=desc
  titleTable: string = this.authService.getGroup() == Group.IstruttoreMunicipio || this.authService.getGroup() == Group.DirettoreMunicipio ? 'Pratiche rielaborazione pareri' : 'Pratiche in attesa del parere';
  exportName = 'Richiesta pareri';
  title = 'Richiesta pareri';

  numProtocollo: string = '';
  showProtocolloDialog: boolean = false;
  isProtocollata: boolean = false;
  actionRiprotocollazione: string = '';
  showEsprimiParereDialog: boolean = false;
  showRichiediParereDialog: boolean = false;
  showRielaboraParereDialog: boolean = false;
  indexRielabora: number = 0;
  selectedAttori: any[] = [];
  attoriPareriSchema : GruppoDTODisabled[];
  noteIstruttoreMunicipio: string = '';
  rielaboaraSelectedTab: number;
  pratica: PraticaDto;
  utentiDestinatariOrdinanze$: Observable<UtenteDTO[]>;

  idUtentePresaInCarico: number;
  idSelectedPratica: number;

  initSortColumn = 'dataModifica';
  globalFilters: any[] = [
    {value:'firmatario.codiceFiscalePartitaIva',label:'Cod. Fiscale/P. IVA'},
    {value:'datiRichiesta.ubicazioneOccupazione',label:'Indirizzo'},
    {value:'tipoProcesso.descrizione',label:'Tip. Processo'},
    {value:'utentePresaInCarico.username',label:'Istruttore'}
  ];

  uploadModeRichiestaIntegrazione = Mode.SINGLE;
  annullaEsenzioneMarcaDaBollo: boolean = false;
  isEsente: boolean = false;
  isEsenteOnInit: boolean = false;
  destinazioneAllegatoRichiestaIntegrazione: DestinazioneAllegato = DestinazioneAllegato.RICHIESTA_INTEGRAZIONE;

  esprimiParereFilesInCheckout: boolean = false;
  richiestaIntegrazioneFilesInCheckout: boolean = false;
  rigettaFilesInCheckout: boolean = false;

  newEsprimiParereUploadedFiles: AllegatoDTO[] = [];
  newRichiestaIntegrazioneUploadedFiles: AllegatoDTO[] = [];
  newRigettaUploadedFiles: AllegatoDTO[] = [];

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
      field: "richiestePareri",
      header: "Parere Polizia",
      type: "text",
      pipe: 'html',
      selector: 'li',
      show: (el) => {
        let parerePolizia = this.parerePolizia(el);
        return this.getDescrizioneParere(parerePolizia);
      }
    },
    {
      field: "richiestePareri",
      header: "Parere IVOOPP",
      type: 'text',
      pipe: 'html',
      selector: 'li',
      show: (el) => {
        return this.parereIVOOP(el);
      }
    },
    {
      field: "richiestePareri",
      header: "Parere Ripartizioni",
      type: "text",
      pipe: 'html',
      selector: 'li',
      show: (el) => {
        return this.parereRipartizioni(el);
      }
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
      key: 'esprimiParereDialog',
      icon: "pi pi-comment",
      tooltip: 'ESPRIMI PARERE',
      hidden: (el: PraticaDto) => {
        return this.authService.getGroup() == environment.groups.idGruppoIstruttoreMunicipio ||
        this.authService.getGroup() == environment.groups.idGruppoDirettoreMunicipio || this.hideEsprimiParere(el) == true;
      }
    },
    {
      key: 'richiediParereDialog',
      icon: "pi pi-comments",
      tooltip: 'RICHIEDI PARERE',
      hidden: (el: PraticaDto) => {
        return this.authService.getGroup() != environment.groups.idGruppoIstruttoreMunicipio ||
        el.tipoProcesso.id == environment.processes.rinunciaConcessione;
      }
    },
    {
      key: 'rielaboraParereDialog',
      icon: "pi pi-sign-in",
      tooltip: 'RIELABORA PARERE',
      hidden: (el: PraticaDto) => {
        return this.authService.getGroup() != environment.groups.idGruppoIstruttoreMunicipio ||
        el.tipoProcesso.id == environment.processes.rinunciaConcessione;
      }
    }
  ];

  hideEsprimiParere(el: PraticaDto): boolean{ //Se TRUE nasconde
    let ret: boolean = true;
    let richiestePareri = el.richiestePareri;
    richiestePareri.forEach(el => {
      if(el.idGruppoDestinatarioParere == this.authService.getGroup() && el.parere === null){
        ret = false;
        return;
      }
    });

    return ret;
  }

  //selectButton Competenza Attori Coinvolti
  competenza: any[] = [
    { label: "Competenza", value: true },
    { label: "Non Competenza", value: false }
  ];
 //selectButton Pareri Attori Coinvolti
  parere: any[] = [
    { label: "Positivo", value: true, disabled: false },
    { label: "Negativo", value: false, disabled: false },
    { label: "Non di competenza", value: 3, disabled : true }
  ];

  abilitaRichiestaPareri: any[] = [
    { label: "Si", value: true, disabled: false },
    { label: "No", value: false, disabled: false },
  ];

  //selectButton Rielabora Pareri
  rielaboraOptions: any[] = [
    { label: "Approva" , value: 'approva'},
    { label: "Chiedi integrazione" , value: 'integrazione'},
    { label: "Rigetta" , value: 'rigetta'}
  ]

  //formGroup
  esprimiParereForm = new FormGroup({
    'competenza': new FormControl(null, [Validators.required]),
    'nota': new FormControl(null),
    'parere': new FormControl(undefined, [Validators.required, this.validParere]),
    'abilitaRichiestaPareri': new FormControl(undefined),
    'destinatariOrdinanza': new FormControl(null)
  }, { validators: [this.controlloNote] });

  private tipoGruppi: GruppoDTO[] = null;

  get isPoliziaLocale(): boolean{
    return this.authService.getGroup() == environment.groups.idGruppoPoliziaLocale;
  }

  get isFlagEsenzioneModificato(): boolean {
    return this.pratica?.datiRichiesta?.flagEsenzioneMarcaDaBolloModificato ? true : false;
  }

  isTemplateOrdinanza(): number {
    return environment.template.ordinanza;
  }

  statoPostConcessione = [
    environment.statiPratica.decaduta,
    environment.statiPratica.revocata,
    environment.statiPratica.annullata
  ]


  ngOnInit(): void {
    this.resetEsprimiParereForm();
    this.idGruppoDestinatarioParere = this.authService.getLoggedUser().groups.id;
    this.utentiDestinatariOrdinanze$ = this.userService.getUsersGruppo(environment.groups.idGruppoDestinatariOrdinanze);
    this.tipologicheService.getGruppi().subscribe(
      (data: GruppoDTO[]) => {
        this.tipoGruppi = data;
      }
    );

    this.cercaPratiche();
  }

  cercaPratiche() {
    let praticaRichiestaPareri = new RicercaPraticaRequest();
    praticaRichiestaPareri.idsMunicipi = this.authService.getMunicipiAppartenenza();
    praticaRichiestaPareri.idsStatiPratica = [environment.statiPratica.richiestaPareri]
    this.spinnerService.showSpinner(true);
    this.praticaService.getPratiche(praticaRichiestaPareri, this.authService.getLoggedUser()).subscribe(
      (data) => {
        this.spinnerService.showSpinner(false);
        this.dataSource = data.content.map(el => Object.setPrototypeOf(el, PraticaDto.prototype));
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage('Ricerca pratiche', err);
      });
  }


  validParere(control: AbstractControl): { [key: string]: boolean } | null {
    if (control.value === undefined) {
      return { 'validParere': true };
    }
    return null;
  }

  controlloParere() {
    let competenzaValue = this.esprimiParereForm.get('competenza').value;

    this.parere.forEach(
      (elem)=> {
        if (elem.value == 3) {
          elem.disabled = competenzaValue;
          this.esprimiParereForm.get('parere').patchValue(3);
        }
      }
    );

    //resetto il campo parere
    //this.esprimiParereForm.get('parere').reset();
  }

  controlloNote(control: AbstractControl): { [key: string]: boolean } | null {
    if (!control.get('nota').value) {
      return {'controlloNoteRequired': true};
    }
    return null;
  }

  get valueRichiestaPareri(){
    return this.esprimiParereForm.get('abilitaRichiestaPareri').value;
  }

  get isFaseIntegrazioneMassimaRaggiunta(): boolean {
    return this.pratica?.contatoreRichiesteIntegrazioni == environment.maxRichiesteIntegrazione;
  }

  get isFaseDiniego(): boolean {
    return this.pratica?.flagProceduraDiniego;
  }

  parerePolizia(richiestePareri: RichiestaParereDTO[]){
    //Ultimo parere che soddisfa la condizione
    let index: number;
    for(let i=(richiestePareri.length - 1); i>=0; i--){
      if(richiestePareri[i].idGruppoDestinatarioParere === environment.groups.idGruppoPoliziaLocale){
          index = i;
          break;
      }
    }

    return richiestePareri[index];
  }

  ultimiPareriEspressi(el: RichiestaParereDTO[], idDestinatari: number[]){
    let ultimiPareriEspressi: RichiestaParereDTO[] = [];

    for (let i = 0; i < idDestinatari.length; i++) {
      for(let j=(el.length - 1); j>=0; j--){
        if(el[j].idGruppoDestinatarioParere === idDestinatari[i]){
            ultimiPareriEspressi.push(el[j]);
            break;
        }
      }
    }

    return ultimiPareriEspressi;
  }

  private parereIVOOP(el: RichiestaParereDTO[]): string {
    let ret: string = '';
    let idDestinatari: number[] = [];

    if (this.tipoGruppi) {
      el.forEach((element:RichiestaParereDTO) => {
        if(element.idGruppoDestinatarioParere == environment.groups.idGruppoSettoreUrbanizzazioniPrimarie ||
          element.idGruppoDestinatarioParere == environment.groups.idGruppoSettoreGiardini ||
          element.idGruppoDestinatarioParere == environment.groups.idGruppoSettoreInfrastruttureRete ||
          element.idGruppoDestinatarioParere == environment.groups.idGruppoSettoreInterventiTerritorio){

          if (!idDestinatari.includes(element.idGruppoDestinatarioParere)) {
            idDestinatari.push(element.idGruppoDestinatarioParere);
          }
        }
      });
    }

    ret = this.renderPareri(this.ultimiPareriEspressi(el, idDestinatari));
    return ret;
  }

  private parereRipartizioni(el: RichiestaParereDTO[]): string {
    let ret: string = '';
    let idDestinatari: number[] = [];

    if (this.tipoGruppi) {
      el.forEach((element:RichiestaParereDTO) => {
        if(element.idGruppoDestinatarioParere == environment.groups.idGruppoRipartizionePatrimonio ||
          element.idGruppoDestinatarioParere == environment.groups.idGruppoRipartizioneUrbanistica) {

          if (!idDestinatari.includes(element.idGruppoDestinatarioParere)) {
            idDestinatari.push(element.idGruppoDestinatarioParere);
          }
        }
      });
    }
    ret = this.renderPareri(this.ultimiPareriEspressi(el, idDestinatari));
    return ret;
  }

  private renderPareri(pareri: RichiestaParereDTO[]): string {
    let ret = 'Non richiesto';
    if (pareri && pareri.length) {
      ret = '<ul id="customList">';
      pareri.forEach((richiestaParere: RichiestaParereDTO)=> {
        ret += `<li>${this.tipoGruppi.find((x: GruppoDTO)=> x.id === richiestaParere.idGruppoDestinatarioParere).descrizione}: <b>${this.getDescrizioneParere(richiestaParere)}</b></li>`;
      });
      ret += "</ul>";
    }
    return ret;
  }

  private getDescrizioneParere(richiestaParere: RichiestaParereDTO): string {
    let ret = 'Non richiesto';

    if (richiestaParere) {
      if (richiestaParere.parere) {
        if (richiestaParere.parere.flagCompetenza) {
          ret = richiestaParere.parere.esito == true ? 'Positivo' : 'Negativo'
        } else {
          ret = "Non di competenza";
        }
      }
      else {
        ret = "In attesa";
      }
    }
    return ret;
  }

  getDescrizioneTipoTemplate(): string {
    let ret = "Relazione di servizio";
    if(this.authService.getGroup() != environment.groups.idGruppoPoliziaLocale){
      ret = "Istruttoria tecnica";
    }

    return ret;
   }

  getTipoTemplate(): number{
    const userGroup: number = this.authService.getGroup();

    if(userGroup == environment.groups.idGruppoPoliziaLocale){
      return environment.template.relazioneDiServizio;
    } else if(userGroup == environment.groups.idGruppoSettoreUrbanizzazioniPrimarie){
      return environment.template.urbanizzazioniPrimarie;
    } else if(userGroup == environment.groups.idGruppoSettoreGiardini){
      return environment.template.settoreGiardini;
    } else if(userGroup == environment.groups.idGruppoSettoreInterventiTerritorio){
      return environment.template.interventiTerritorio;
    } else if(userGroup == environment.groups.idGruppoSettoreInfrastruttureRete){
      return environment.template.infrastruttureRete;
    } else if(userGroup == environment.groups.idGruppoRipartizioneUrbanistica){
      return environment.template.istruttoriaTecnicaUrbanistica;
    }  else if(userGroup == environment.groups.idGruppoRipartizionePatrimonio){
      return environment.template.istruttoriaTecnicaPatrimonio;
    }

  }


  getIdDeterminaConcessione(){
    return environment.template.determinaDiConcessione;
  }

  //Rielabora Pareri
  rielaboraParereDialog(element: any) {
    this.pratica = element;
    this.isEsenteOnInit = this.pratica.datiRichiesta.flagEsenzioneMarcaDaBollo;
    this.annullaEsenzioneMarcaDaBollo = false;
    this.showRielaboraParereDialog = true;

    this.idSelectedPratica = this.pratica.id;
    this.idUtentePresaInCarico = this.pratica.utentePresaInCarico.id;
  }

  async closeRielaboraParereDialog(event?) {
    this.showRielaboraParereDialog = false;
    this.indexRielabora = 0;
    this.noteIstruttoreMunicipio = '';
    this.selectedAttori = [];

    if(this.newRichiestaIntegrazioneUploadedFiles.length > 0) {
      const promises = this.newRichiestaIntegrazioneUploadedFiles.map((allegato, index) => {
        return this.allegatiService.deleteAllegato(allegato.id).toPromise();
      });
      Promise.all(promises).finally(() => {
        this.newRichiestaIntegrazioneUploadedFiles = [];
      });
    }

    if(this.newRigettaUploadedFiles.length > 0) {
      const promises = this.newRigettaUploadedFiles.map((allegato, index) => {
        return this.allegatiService.deleteAllegato(allegato.id).toPromise();
      });
      Promise.all(promises).finally(() => {
        this.newRigettaUploadedFiles = [];
      });
    }
  }

  invioPraticaDaRigettare() {
    this.actionRiprotocollazione = 'inviaRielaboraParere';
    this.spinnerService.showSpinner(true);
    const idAllegato = this.newRigettaUploadedFiles.length > 0 ?
                        this.newRigettaUploadedFiles[0].id :
                        null;
    this.isEsente = this.isEsenteOnInit;
    if (this.isEsenteOnInit && this.annullaEsenzioneMarcaDaBollo) {
      this.isEsente = false;
    }
    this.praticaService.praticaDaRigettare(this.idSelectedPratica, this.idUtentePresaInCarico, this.noteIstruttoreMunicipio, this.isEsente, idAllegato).subscribe(
      (data: PraticaDto) => {
        this.newRigettaUploadedFiles = [];
        this.spinnerService.showSpinner(false);
        this.showRielaboraParereDialog = false;
        this.messageService.showMessage('success', 'Aggiornamento pratica', this.successMessageRigetta);
        if(!this.isFaseDiniego) {
          const pratica = data;
          this.isProtocollata = pratica.protocolli.length > 0 ;
          this.numProtocollo = this.utilityService.getLastProtocolByDate(pratica.protocolli);
          this.showProtocolloDialog = true;
        } else {
          this.actionRiprotocollazione = '';
          this.cercaPratiche();
        }
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage('Aggiornamento pratica', err);
        if (!this.isFaseDiniego) {
          this.numProtocollo = '--|--';
          this.showProtocolloDialog = true;
        }
      });
  }

  inviaRielaboraParere(scelta) {
    if(scelta != null && scelta != undefined) {
      this.rielaboaraSelectedTab = scelta;
    }
    switch(this.rielaboaraSelectedTab) {
      //0 = approvata / 1 = integrazione / 2 = rigetta
      case 0:  //Generazione determina
        this.avanzamentoPratica();
        break;
      case 1:
        this.invioRichiestaIntegrazione();
        break;
      case 2:
        this.invioPraticaDaRigettare();
        break;
    }
  }

  invioRichiestaIntegrazione() {
    if(this.noteIstruttoreMunicipio) {
      this.statoRichiestaIntegrazione(this.idSelectedPratica, this.noteIstruttoreMunicipio)
    }
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

  get successMessageRigetta(){
    let ret: string = 'La pratica è passata in stato "Preavviso diniego"';
    if(this.isFaseDiniego){
      ret = 'La pratica è passata in stato "Pratica da rigettare"';
    }

    return ret;
  }

  statoRichiestaIntegrazione(idPratica: number, testoNota: string){
    this.actionRiprotocollazione = 'inviaRielaboraParere';
    this.spinnerService.showSpinner(true);
    const idAllegato = this.newRichiestaIntegrazioneUploadedFiles.length > 0 ?
                        this.newRichiestaIntegrazioneUploadedFiles[0].id :
                        null;
    this.isEsente = this.isEsenteOnInit;
    if (this.isEsenteOnInit && this.annullaEsenzioneMarcaDaBollo) {
      this.isEsente = false;
    }
    this.praticaService.statoRichiestaIntegrazione(idPratica, this.idUtentePresaInCarico, testoNota, this.isEsente, idAllegato).subscribe(
      (data: RichiestaIntegrazioneDTO) => {
        this.newRichiestaIntegrazioneUploadedFiles = [];
        this.spinnerService.showSpinner(false);
        this.showRielaboraParereDialog = false;
        this.messageService.showMessage('success', 'Aggiornamento pratica', this.successMessageIntegrazione);
        const parere = data;
        this.isProtocollata = !!parere?.codiceProtocollo;
        this.numProtocollo = `${parere?.codiceProtocollo || '--|--'}`;
        this.showProtocolloDialog = true;
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage('Aggiornamento pratica', err);
        this.numProtocollo = '--|--';
        this.showProtocolloDialog = true;
      });
  }

  avanzamentoPratica() {
    this.spinnerService.showSpinner(true);
    this.praticaService.approvaPratica(this.pratica.id, this.authService.getLoggedUser().userLogged.id).subscribe(
      (data: PraticaDto) => {
        this.spinnerService.showSpinner(false);
        this.showRielaboraParereDialog = false;
        this.messageService.showMessage('success', 'Approvazione pratica', "Pratica approvata con successo");
        this.cercaPratiche();
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage("Approvazione pratica", err);
      }
    );
  }


  resetEsprimiParereForm(){
    this.esprimiParereForm.reset();
    this.esprimiParereForm.get('parere').patchValue(undefined);
  }

  esprimiParereDialog(element: any) {
    this.pratica = element;
    this.idSelectedPratica = this.pratica.id;

    this.showEsprimiParereDialog = true;
    this.esprimiParereForm.get('abilitaRichiestaPareri').patchValue(false); //Default value di Richiedi pareri è No

  }

  closeEsprimiPareriDialog() {
    this.showEsprimiParereDialog = false;
    this.resetEsprimiParereForm();
    this.parere.forEach(el => {
      if(el.value == null)
        el.disabled = true;
    });
    if (this.newEsprimiParereUploadedFiles.length > 0) {
      const promises = this.newEsprimiParereUploadedFiles.map((allegato, index) => {
        return this.allegatiService.deleteAllegato(allegato.id).toPromise();
      });
      Promise.all(promises).finally(() => {
        this.newEsprimiParereUploadedFiles = [];
      });
    }
    this.docsMap = new Map<number, AllegatoDTO>();
    this.newEsprimiParereUploadedFiles = [];
  }

  confirmDisabled: boolean = true;
  onMandatoryDocuments(event: boolean) {
    this.confirmDisabled = !event;
  }

  isConfirmRielaboraParereDisabled(indexRielabora) {
    const _default = indexRielabora != 0 && !this.noteIstruttoreMunicipio.trim() ;
    switch(indexRielabora) {
      //0 = approvata / 1 = integrazione / 2 = rigetta
      case 1:
        return _default || this.richiestaIntegrazioneFilesInCheckout;
      case 2:
        return _default || this.rigettaFilesInCheckout;
      default:
        _default;
    }
  }

  isConfirmEnabledControl(): boolean {
    let ret: boolean = false;

    if(this.isPoliziaLocale){
      if(this.esprimiPareriEnabled() == false){
        ret = this.isAbilitaRichiesta();
      } else{
        ret = true;
      }
    } else {
      ret = this.esprimiPareriEnabled();
    }
    let destinatari: boolean = false;
    if (this.esprimiParereForm.controls['destinatariOrdinanza'].value) {
      let valDestinatari: string = ''+ this.esprimiParereForm.controls['destinatariOrdinanza'].value;
      destinatari = (valDestinatari.length > 0)?true:false;
    }

    //ret = (this.showUtentiOrdinanza() && destinatari)?false:true;
    return ret || this.esprimiParereFilesInCheckout;
  }

  public isAbilitaRichiesta(): boolean{
    //Disabilitato = True / Abilitato = False
    if(this.esprimiParereForm.get('abilitaRichiestaPareri').value == true){
      if(this.selectedAttori.length > 0){
        return false;
      } else{
        return true;
      }
    } else{
      return false;
    }
  }

  public esprimiPareriEnabled(): boolean {
    let esitoParere = this.esprimiParereForm.get('parere').value;
    let flagCompetenza: boolean = this.esprimiParereForm.get('competenza').value;
    let note: string = this.esprimiParereForm.get('nota').value;
    if(flagCompetenza == false && note?.trim().length > 0){ // Non competenza + Nota inserita + Parere N.D
      return false;
      //return this.esprimiParereForm.valid;
    } else if((esitoParere == false || esitoParere) && !this.confirmDisabled){
      return this.esprimiParereForm.invalid;
    }

    return this.esprimiParereForm.invalid || this.confirmDisabled;
  }

  esprimiTuttiPareri(){
    const esprimiParere: Array<number> = this.esprimiParereForm.get('destinatariOrdinanza').value;
    if (!this.showUtentiOrdinanza() || (esprimiParere != null && esprimiParere.length > 0)) {
      this.esprimiParere();
      if(this.isPoliziaLocale && this.valueRichiestaPareri){
        this.inviaRichiestaPareri();
      }
    } else {this.confirmationService.confirm({
      icon: "pi pi-exclamation-triangle",
      acceptLabel: "Conferma",
      rejectLabel: "Annulla",
      acceptButtonStyleClass: "btn-custom-style btn-dialog-confirm",
      rejectButtonStyleClass: "btn-custom-style btn-dialog-confirm",
      header: 'Esprimi parere',
      message: 'Si sta rilasciando il parere senza trasmettere l\'ordinanza. Procedere comunque?',
      accept: () => {
        this.esprimiParere();
        if(this.isPoliziaLocale && this.valueRichiestaPareri){
          this.inviaRichiestaPareri();
        }
      }
    });
    }
  }

  esprimiParere() {
    this.spinnerService.showSpinner(true);
    let idUtenteParere = this.authService.getLoggedUser().userLogged.id;
    let flagCompetenza: boolean = this.esprimiParereForm.get('competenza').value;
    let testoNota : string = this.esprimiParereForm.get('nota').value;
    let esitoParere = this.esprimiParereForm.get('parere').value;
    if(esitoParere == 3){
      esitoParere = null;
    }
    let destinatariOrdinanza: string = this.esprimiParereForm.get('destinatariOrdinanza').value;

    let richiestaParere = this.pratica.richiestePareri.find(
      (x: RichiestaParereDTO) => {
        return x.idGruppoDestinatarioParere === this.authService.getLoggedUser().groups.id && !x.flagInseritaRisposta;
      }
    );

    let parereReq: ParereInsertRequest = new ParereInsertRequest();
    parereReq.listaIdUtentiEmail = null;

      if (destinatariOrdinanza) {
        let as: string[] = ('' +destinatariOrdinanza).split(",");
        let destinatari: number[] = [];
        if (as && as.length>0) {
          as.forEach((elem: string)=> {destinatari.push(+elem)});
          parereReq.listaIdUtentiEmail = destinatari;
        }
      }
    parereReq.parere = new ParereDTO();
    parereReq.parere.idRichiestaParere = richiestaParere.id;
    parereReq.parere.idUtenteParere = idUtenteParere;
    parereReq.parere.nota = testoNota;
    parereReq.parere.esito = esitoParere;
    parereReq.parere.flagCompetenza = flagCompetenza;
    parereReq.flagPec = true;

    this.actionRiprotocollazione = 'esprimiTuttiPareri';
    this.praticaService.esprimiPareri(parereReq).subscribe(
      (data: ParereDTO) => {
        const uploadedFilesLength = this.newEsprimiParereUploadedFiles.length;
        this.newEsprimiParereUploadedFiles = [];
        this.spinnerService.showSpinner(false);
        this.showEsprimiParereDialog = false;
        this.messageService.showMessage('success', 'Esprimi parere', 'Il parere è stato inviato con successo');
        const parere = data;
        this.isProtocollata = !!parere;
        if(this.isProtocollata && !!parere.codiceProtocollo) {
          this.numProtocollo = `${parere?.codiceProtocollo || '--|--'}`;
          this.showProtocolloDialog = true;
        } else {
          this.closeProtocolloDialog();
        }
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage('Aggiornamento pratica', err);
        this.numProtocollo = '--|--';
        this.showProtocolloDialog = true;
      });
  }

  richiediParereDialog(pratica: PraticaDto) {
    this.pratica = pratica;
    this.idSelectedPratica = this.pratica.id;

    if (this.pratica.richiestePareri) {
      let attoriPareri: GruppoDTO[] = this.tipoGruppi.filter(x=> x.flagGestRichiestaParere != false);
      let tempAttori: GruppoDTODisabled[] = [];

      attoriPareri.forEach(
        (gruppo: GruppoDTODisabled) => {
          let richiestaParere = this.pratica.richiestePareri.find((x: RichiestaParereDTO)=> x.idGruppoDestinatarioParere === gruppo.id);
          if (richiestaParere == null || richiestaParere.flagInseritaRisposta) {
            tempAttori.push(gruppo);
          } else{
            gruppo.flg_disabled = true;
            tempAttori.push(gruppo);
          }
        }
      );
      // Ordinamento alfabetico

      this.attoriPareriSchema = tempAttori.sort((obj1: GruppoDTODisabled, obj2: GruppoDTODisabled) => {
        let ret = 0;
        if (obj1.descrizione > obj2.descrizione) {
            ret = 1;
        }
        if (obj1.descrizione < obj2.descrizione) {
          ret = -1;
        }
        return ret;
      });

    }

    if(!this.isPoliziaLocale){
      this.showRichiediParereDialog = true;
    }
  }

  closeRichiediPareriDialog() {
    this.showRichiediParereDialog = false;
    this.selectedAttori = [];
  }

  inviaRichiestaPareri() {
    this.actionRiprotocollazione = 'inviaRichiestaPareri';
    this.spinnerService.showSpinner(true);

    this.praticaService.richiestaUlterioriPareri(this.idSelectedPratica, this.selectedAttori).subscribe(
      (data: RichiestaParereDTO[]) => {
        this.newEsprimiParereUploadedFiles = [];
        this.spinnerService.showSpinner(false);
        this.showRichiediParereDialog = false;
        this.messageService.showMessage('success', 'Richiesta pareri', 'Sono state inoltrate le nuove richieste di parere');
        const parere = data[0];
        this.isProtocollata = !!parere;
        if(this.isProtocollata && !!parere.codiceProtocollo) {
          this.numProtocollo = `${parere?.codiceProtocollo || '--|--'}`;
          this.showProtocolloDialog = true;
        } else {
          this.closeProtocolloDialog();
        }
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage('Aggiornamento pratica', err);
        this.numProtocollo = '--|--';
        this.showProtocolloDialog = true;
      });

  }

  variazioneDichiarazioneEsenzione(event?) {
    this.pratica.datiRichiesta.flagEsenzioneMarcaDaBollo = this.annullaEsenzioneMarcaDaBollo;

    if(event == 'pareri') {
      this.pratica.datiRichiesta.motivazioneEsenzioneMarcaDaBollo = this.annullaEsenzioneMarcaDaBollo ? 'Esenzione confermata da municipio' : '';
    }
  }

  generaTemplateElaborato(tipoTemplate: number): void {
    let testoNota : string = this.esprimiParereForm.get('nota').value;

    this.templateService.getTemplateElaborato(this.idSelectedPratica, tipoTemplate, testoNota).subscribe(
      (data: TemplateDTO) => {
        this.utilityService.downloadFile(data.nomeFile, data.mimeType, data.fileTemplate);
      },
      err => {this.messageService.showErrorMessage("Template determina", err);}
    );
  }

  selectedDestinatariOrdinanza: string[] = [];
  docsMap: Map<number, AllegatoDTO> = new Map<number, AllegatoDTO>();
  onDocUploaded(event: AllegatoDTO): void {
    this.docsMap.set(event.tipoAllegato.id, event);
  }
  onDocRemoved(event: number): void {
    this.docsMap.delete(event);
  }

  showUtentiOrdinanza(): boolean {
    let ret: boolean = false;
    this.docsMap.forEach((val, key) => {
        if (environment.tipologiaAllegati.ordinanza === key) {
          ret = true;
        }
    });

    return ret;
  }

  closeProtocolloDialog(event?) {
    this.resetEsprimiParereForm();
    this.selectedAttori = [];
    this.showProtocolloDialog = false;
    this.isProtocollata = false;
    this.numProtocollo = '';
    this.actionRiprotocollazione = '';
    this.cercaPratiche();
  }

  fileInCheckout = (statusKey) => (status_checkout) => {
    this[statusKey] = status_checkout;
  }

  onEsprimiParereUploadedFile(file) {
    this.newEsprimiParereUploadedFiles.push(file);
  }

  onRichiestaIntegrazioneUploadedFile(file) {
    this.newRichiestaIntegrazioneUploadedFiles.push(file);
  }

  onRigettaUploadedFile(file) {
    this.newRigettaUploadedFiles.push(file);
  }

  isUploadMandatory() {
    return this.esprimiParereForm.get('competenza').value;
  }

}
