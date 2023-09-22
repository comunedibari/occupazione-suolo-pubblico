import { Component, Inject, Input, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidatorFn, Validators } from '@angular/forms';
import { SpinnerDialogService } from '@core-components/layout/spinner-dialog/services/spinner-dialog.service';
import { CiviliarioResponse } from '@models/civiliario-response';
import { PraticaDto } from '@models/dto/pratica-dto';
import { TipoManufatto } from '@models/dto/tipo-manufatto';
import { TipologicaDTO } from '@models/dto/tipologica-dto';
import { PraticaRequest } from '@models/pratica-request';
import { AuthService } from '@services/auth/auth.service';
import { CiviliarioService } from '@services/civiliario.service';
import { MessageService } from '@services/message.service';
import { PraticaService } from '@services/pratica.service';
import { TipologicheService } from '@services/tipologiche.service';
import { UtilityService } from '@services/utility.service';
import { UploadDialogConfig } from '@shared-components/upload-file/model/upload-dialog-config';
import { UploadFileComponent } from '@shared-components/upload-file/upload-file.component';
import { environment } from 'environments/environment';
import { ConfirmationService } from 'primeng/api';
import { DialogService, DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Observable } from 'rxjs';
import { first } from 'rxjs/operators';
import { GestionePraticaDialogConfig } from './model/gestione-pratica-dialog-config';
import { DateTime } from 'luxon';
import { Mode } from '@shared-components/upload-file/enums/mode.enum';
import { DestinazioneAllegato } from '@shared-components/upload-file/enums/destinazione-allegato.enum';
import { TipologicaTestoLibero } from '@models/dto/tipologica-testo-libero-dto';
import { MappaComponent } from '@shared-components/mappa/mappa.component';
import { Router } from '@angular/router';
import { EventEmitter } from "@angular/core";

import { TemplateService } from '@services/template.service';
import { TemplateDTO } from '@models/dto/template-dto';
import { AllegatoDTO } from '@models/dto/allegato-dto';
import { AllegatiService } from '@services/allegati.service';

@Component({
  selector: 'app-gestione-pratica',
  templateUrl: './gestione-pratica.component.html',
  styleUrls: ['./gestione-pratica.component.css']
})
export class GestionePraticaComponent implements OnInit {
  @Input() pratica: PraticaDto = null;
  @Input() readonly: boolean;
  @Input() isDettaglio: boolean = false;
  isRichiestaProroga: boolean = false;
  isRinunciaConcessione: boolean = false;
  isAvviaRettifica: boolean = false;
  isProtocollata: boolean = false;

  private mappaElements: PraticaDto[] = [];
  inserisciPraticaForm: FormGroup;

  numProtocollo: string = '';
  actionConfirmNumProtocollo: string = '';
  insertResponse: any;
  loading: boolean = false;
  initialFormValues: any = {};
  actionRiprotocollazione: string = '';
  minDataPeriodo: Date = new Date();

  showProtocolloDialog: boolean = false;
  showUploadDeterminaDialog: boolean = false;
  identificativoDetermina: string = '';
  dataDetermina: Date;
  limitData: Date = new Date();
  confermaDatiDetermina: boolean = false

  destinazioneAllegato = DestinazioneAllegato.PRATICA;
  uploadMode = Mode.SINGLE;
  filtroStatoPratica : number = null;
  mandatoryDocumentsAttached: boolean = false;
  idRettificaConcessione: number = environment.processes.rettificaErrori;

  ragsocDestinatario: boolean = false;

  tipoAttivitaDaSvolgere$: Observable<TipologicaTestoLibero[]>;
  tipoTitoloEdilizio$: Observable<TipologicaTestoLibero[]>;
  tipoManufatto$: Observable<TipoManufatto[]>;
  tipoDocRicFirmatario$: Observable<TipologicaDTO[]>;
  tipoDocRicDestinatario$: Observable<TipologicaDTO[]>;
  tipoRuoloFirmatario$: Observable<TipologicaDTO[]>;

  nominatim_option: any = {
    nome_via: '',
    numero: null,
    esponente: '',
    municipio: null,
    localita: '',
    lat: null,
    lon: null
  };

  //Autocomplete civico
  civilarioResults: CiviliarioResponse[] = [];
  addInserisciIndirizzoManualmente: boolean = false;
  showInserisciIndirizzoManualmenteDialog: boolean = false;

  uploadedFiles: AllegatoDTO[] = [];
  newRettificaUploadedFiles: AllegatoDTO[] = [];

  showAnagraficaDestinatario: boolean = false;
  isRuoloAltro: boolean = false;
  isRuoloPersonaFisica: boolean = false;

  constructor(
    private fb: FormBuilder,
    private messageService: MessageService,
    private utilityService: UtilityService,
    public dialogService: DialogService,
    public confirmationService: ConfirmationService,
    public authService: AuthService,
    private praticaService: PraticaService,
    private tipologicheService: TipologicheService,
    private civiliarioService: CiviliarioService,
    private spinnerService: SpinnerDialogService,
    private dialogRef: DynamicDialogRef,
    private router: Router,
    private allegatiService: AllegatiService,

    private templateService: TemplateService,
    @Inject(DynamicDialogConfig) config: DynamicDialogConfig
  ) {
    if (config.data) {
      let myConfig: GestionePraticaDialogConfig = config.data;
      this.pratica = myConfig.pratica;
      this.readonly = myConfig.readonly;

      if (myConfig.isRichiestaProroga) {
        this.isRichiestaProroga = myConfig.isRichiestaProroga;
      }
      if (myConfig.isRinunciaConcessione) {
        this.isRinunciaConcessione = myConfig.isRinunciaConcessione;
      }
      this.isRichiestaProroga =
        myConfig.isRichiestaProroga ||
        (
          this.pratica.tipoProcesso != null &&
          this.pratica.tipoProcesso.id === environment.processes.prorogaConcessioneTemporanea
        );
      this.isRinunciaConcessione =
        myConfig.isRinunciaConcessione ||
        (
          this.pratica.tipoProcesso != null &&
          this.pratica.tipoProcesso.id === environment.processes.rinunciaConcessione
        );
      if (myConfig.isAvviaRettifica) {
        this.isAvviaRettifica = myConfig.isAvviaRettifica;
      }
      if (myConfig.isDettaglio) {
        this.isDettaglio = myConfig.isDettaglio;
      }
    }
  }

  ngOnInit(): void {
    this.spinnerService.showSpinner(true);
    this.mappaElements.push(this.pratica);
    this.buildForm();
    this.initialFormValues = this.inserisciPraticaForm.value;
    this.tipoAttivitaDaSvolgere$ = this.tipologicheService.getTipoAttivitaDaSvolgere();
    this.tipoTitoloEdilizio$ = this.tipologicheService.getTipoTitoloEdilizio();
    this.tipoManufatto$ = this.tipologicheService.getTipoManufatto();
    this.tipoDocRicFirmatario$ = this.tipologicheService.getTipoDocumento();
    this.tipoDocRicDestinatario$ = this.tipologicheService.getTipoDocumento();
    this.tipoRuoloFirmatario$ = this.tipologicheService.getTipiRuoliRichiedenti();
    if (this.pratica) {
      this.initForm(this.pratica);
      this.changeEsenzione();
    }
    this.spinnerService.showSpinner(false);
  }

  private buildForm() {
    this.inserisciPraticaForm = this.fb.group({
      firmatario: this.fb.group({
        id: [null],
        denominazione: [null],
        flagFirmatario: [null],
        flagDestinatario: [null],
        dataDiNascita: [null],

        nome: [{value: null, disabled: this.readonly}, [Validators.required]],
        cognome: [{value: null, disabled: this.readonly}, [Validators.required]],
        dataDiNascitaForm: [{value: null, disabled: (this.isRinunciaConcessione || this.isRichiestaProroga) && !this.isStatoBozza() ? true : this.readonly}, [Validators.required]],
        comuneDiNascita: [{value: null, disabled: this.readonly}, [Validators.required]],
        provinciaDiNascita: [{value: null, disabled: this.readonly}, [Validators.required]],
        nazionalita: [{value: null, disabled: this.readonly}, [Validators.required]],
        codiceFiscalePartitaIva: [{value: null, disabled: this.readonly}, [Validators.required, this.validCodFiscaleOrPartivaIva]],
        citta: [{value: null, disabled: this.readonly}, [Validators.required]],
        indirizzo: [{value: null, disabled: this.readonly}, [Validators.required]],
        civico: [{value: null, disabled: this.readonly}, [Validators.required]],
        provincia: [{value: null, disabled: this.readonly}, [Validators.required]],
        cap: [{value: null, disabled: this.readonly}, [Validators.required, Validators.pattern('^([0-9]){5}$')]],
        recapitoTelefonico: [{value: null, disabled: this.readonly}, [Validators.required, Validators.minLength(9), Validators.maxLength(10)]],
        email: [{value: null, disabled: this.readonly}, [Validators.required, Validators.email]],
        idTipoDocumentoAllegato: [{value: null, disabled: this.readonly}, [Validators.required]],
        numeroDocumentoAllegato: [{value: null, disabled: this.readonly}, [Validators.required]],
        amministrazioneDocumentoAllegato: [{value: null, disabled: this.readonly}, [Validators.required]],
        idTipoRuoloRichiedente: [{value: null, disabled: this.readonly}, [Validators.required]],
        descrizioneRuolo: [{value: null, disabled: (this.isRinunciaConcessione || this.isRichiestaProroga) && !this.isStatoBozza() ? true : this.readonly}],
        qualitaRuolo: [{value: null, disabled: (this.isRinunciaConcessione || this.isRichiestaProroga) && !this.isStatoBozza() ? true : this.readonly}]
      }),
      destinatario: this.fb.group({
        id: [null],
        idTipoRuoloRichiedente: [null],
        descrizioneRuolo: [null],
        qualitaRuolo: [null],
        flagFirmatario: [null],
        flagDestinatario: [null],
        dataDiNascita: [null],
        dataDiNascitaForm: [null],
        comuneDiNascita: [null],
        provinciaDiNascita: [null],
        nazionalita: [null],
        cap: [null],

        nome: [{value: null, disabled: this.readonly}, []],
        cognome: [{value: null, disabled: this.readonly}, []],
        denominazione: [{value: null, disabled: this.readonly}, []],
        codiceFiscalePartitaIva: [{value: null, disabled: this.readonly}, []],
        citta: [{value: null, disabled: this.readonly}, []],
        indirizzo: [{value: null, disabled: this.readonly}, []],
        civico: [{value: null, disabled: this.readonly}, []],
        provincia: [{value: null, disabled: this.readonly}, []],
        recapitoTelefonico: [{value: null, disabled: this.readonly}, []],
        email: [{value: null, disabled: this.readonly}, []],
        idTipoDocumentoAllegato: [{value: null, disabled: this.readonly}, []],
        numeroDocumentoAllegato: [{value: null, disabled: this.readonly}, []],
        amministrazioneDocumentoAllegato: [{value: null, disabled: this.readonly}, []]
      }),
      datiRichiesta: this.fb.group({
        id: [null],
        nome_via: [null],
        numero: [null],
        cod_via: [null],
        idMunicipio: [null],
        localita: [null],
        coordUbicazioneTemporanea: {
          points: [
            {
              lat: [null],
              lon: [null],
            }
          ]
        },
        coordUbicazioneDefinitiva: [null],
        dataInizioOccupazione: [null],
        dataScadenzaOccupazione: [null],
        oraInizioOccupazione: [null],
        oraScadenzaOccupazione: [null],
        tipoOperazioneVerificaOccupazione: [null],

        ubicazioneOccupazione: [{value: null, disabled: this.readonly}, []], // Rimuovere ?
        datoCiviliario: [{value: null, disabled: this.readonly}, [Validators.required, this.objectControl]],
        noteUbicazione: [{value: null, disabled: this.readonly}, [Validators.required]],
        flagNumeroCivicoAssente: [{value: false, disabled: this.readonly}, []],
        superficieAreaMq: [{value: null, disabled: this.readonly}, [Validators.required]],
        larghezzaM:[{value: null, disabled: this.readonly}, [Validators.required]],
        lunghezzaM: [{value: null, disabled: this.readonly}, [Validators.required]],
        superficieMarciapiedeMq: [{value: null, disabled: this.readonly}, []],
        larghezzaMarciapiedeM: [{value: null, disabled: this.readonly}, []],
        lunghezzaMarciapiedeM: [{value: null, disabled: this.readonly}, []],
        larghezzaCarreggiataM: [{value: null, disabled: this.readonly}, []],
        lunghezzaCarreggiataM: [{value: null, disabled: this.readonly}, []],
        stalloDiSosta: [{value: false, disabled: this.readonly}, []],
        presScivoliDiversamenteAbili:[{value: false, disabled: this.readonly}, []],
        presPassiCarrabiliDiversamenteAbili: [{value: false, disabled: this.readonly}, []],
        dataInizioOccupazioneForm:[{value: null, disabled: this.checkDisabled() || this.isAvviaRettifica}, [Validators.required]],
        oraInizioOccupazioneForm: [{value: null, disabled: this.checkDisabled() || this.isAvviaRettifica}, []],
        dataScadenzaOccupazioneForm:[{value: null, disabled: (this.isRinunciaConcessione) && !this.isStatoBozza() || this.isAvviaRettifica ? true : this.readonly}, [Validators.required]],
        oraScadenzaOccupazioneForm: [{value: null, disabled: (this.isRinunciaConcessione) && !this.isStatoBozza() || this.isAvviaRettifica ? true : this.readonly}, []],
        idAttivitaDaSvolgere: [{value: null, disabled: this.readonly}, [Validators.required]],
        idTipologiaTitoloEdilizio: [{value: null, disabled: this.readonly}, []],
        riferimentoTitoloEdilizio: [{value: null, disabled: this.readonly}, []],
        descrizioneTitoloEdilizio: [{value: null, disabled: this.readonly}, []],
        descrizioneAttivitaDaSvolgere: [{value: null, disabled: this.readonly}, []],
        idManufatto: [{value: null, disabled: this.readonly}, []],
        descrizioneManufatto: [{value: null, disabled: this.readonly}, []],
        flagAccettazioneRegSuoloPubblico: [{value: false, disabled: this.readonly}, [Validators.requiredTrue]],
        flagRispettoInteresseTerzi: [{value: false, disabled: this.readonly}, [Validators.requiredTrue]],
        flagObbligoRiparazioneDanni: [{value: false, disabled: this.readonly}, [Validators.requiredTrue]],
        flagRispettoDisposizioniRegolamento: [{value: false, disabled: this.readonly}, [Validators.requiredTrue]],
        flagConoscenzaTassaOccupazione: [{value: false, disabled: this.readonly}, [Validators.requiredTrue]],
        flagNonModificheRispettoConcessione: {value: false, disabled: this.readonly},
        flagEsenzioneMarcaDaBollo: {value: false, disabled: this.readonly},
        motivazioneEsenzioneMarcaBollo: [{value: '', disabled: this.readonly}, [Validators.required]],
        flagEsenzioneMarcaDaBolloModificato: {value: false, disabled: this.readonly}
      }),
      motivazioneRichiesta: [{value: null, disabled: this.readonly}],
    });

    this.inserisciPraticaForm.controls['datiRichiesta'].setValidators([
      this.validDateRange('dataInizioOccupazioneForm', 'dataScadenzaOccupazioneForm'),
      this.validOrariRange('oraInizioOccupazioneForm', 'oraScadenzaOccupazioneForm', 'dataInizioOccupazioneForm', 'dataScadenzaOccupazioneForm')
    ]);

    if(this.isRichiestaProroga && !this.readonly){
      this.inserisciPraticaForm.controls['datiRichiesta'].get('flagNonModificheRispettoConcessione').enable();
      this.inserisciPraticaForm.controls['datiRichiesta'].get('flagNonModificheRispettoConcessione').setValidators([Validators.requiredTrue]);
      this.inserisciPraticaForm.controls['datiRichiesta'].get('flagNonModificheRispettoConcessione').patchValue(null);
    }

    if(this.isRinunciaConcessione && !this.readonly){
      this.inserisciPraticaForm.get('motivazioneRichiesta').enable();
      this.inserisciPraticaForm.get('motivazioneRichiesta').setValidators([Validators.required]);
    }

    this.changeEsenzione();
  }

  private initForm(pratica: PraticaDto): void {
    if (pratica.id != null) {
      this.inserisciPraticaForm.get('firmatario').get('idTipoRuoloRichiedente').disable();
    }
    pratica.firmatario.dataDiNascitaForm = new Date(pratica.firmatario.dataDiNascita);
    this.inserisciPraticaForm.controls['firmatario'].setValue(pratica.firmatario);
    if (this.pratica.destinatario)
    {
      pratica.destinatario.dataDiNascitaForm = new Date();
      this.inserisciPraticaForm.controls['destinatario'].setValue(pratica.destinatario);
    } else {
      this.inserisciPraticaForm.controls['destinatario'].disable();
    }
    let datoCiviliario: CiviliarioResponse = new CiviliarioResponse();
    datoCiviliario.indirizzo = pratica.datiRichiesta.ubicazioneOccupazione;
    datoCiviliario.nome_via = pratica.datiRichiesta.nome_via;
    datoCiviliario.numero = pratica.datiRichiesta.numero;
    datoCiviliario.cod_via = pratica.datiRichiesta.cod_via;
    datoCiviliario.localita = pratica.datiRichiesta.localita;
    datoCiviliario.lat = null;
    datoCiviliario.lon = null;
    if (pratica.datiRichiesta.coordUbicazioneTemporanea && pratica.datiRichiesta.coordUbicazioneTemporanea.points) {
      datoCiviliario.lat = pratica.datiRichiesta.coordUbicazioneTemporanea.points[0].lat;
      datoCiviliario.lon = pratica.datiRichiesta.coordUbicazioneTemporanea.points[0].lon;
    }

    pratica.datiRichiesta["datoCiviliario"] = datoCiviliario;
    pratica.datiRichiesta.dataInizioOccupazioneForm = new Date(pratica.datiRichiesta.dataInizioOccupazione);
    pratica.datiRichiesta.dataScadenzaOccupazioneForm = pratica.datiRichiesta.dataScadenzaOccupazione == null ? null : new Date(pratica.datiRichiesta.dataScadenzaOccupazione);
    pratica.datiRichiesta.oraInizioOccupazioneForm = this.utilityService.getTimeForFE(pratica.datiRichiesta.oraInizioOccupazione);
    pratica.datiRichiesta.oraScadenzaOccupazioneForm = this.utilityService.getTimeForFE(pratica.datiRichiesta.oraScadenzaOccupazione);

    //pratica.datiRichiesta.flagNumeroCivicoAssente = this.inserisciPraticaForm.get('datiRichiesta.flagNumeroCivicoAssente').value;
    //pratica.datiRichiesta.coordUbicazioneDefinitiva = null;
    this.inserisciPraticaForm.controls['datiRichiesta'].setValue(pratica.datiRichiesta);
    this.inserisciPraticaForm.get('motivazioneRichiesta').setValue(pratica.motivazioneRichiesta);

    // if (pratica.firmatario.idTipoRuoloRichiedente != environment.roles.idRuoloPersonaFisica) {
      this.tipoRuloChanged({value: pratica.firmatario.idTipoRuoloRichiedente}) ;
    // }

    if(this.isRichiestaProroga || this.isRinunciaConcessione){
      this.destinatarioCheck();
    } else if(!!this.pratica.destinatario?.denominazione) {
      this.destinatarioCheck(true)
    }

    this.tipologicheService.getTipoAttivitaDaSvolgere().pipe(first()).subscribe(
      (data: TipologicaTestoLibero[]) => {
        let tipoAttivita: TipologicaTestoLibero = data.find((x:TipologicaTestoLibero)=>x.id===pratica.datiRichiesta.idAttivitaDaSvolgere);
        if (tipoAttivita) {
          this.setTipoAttivitaDaSvolgere(tipoAttivita);
        }
      }
    );

    this.tipologicheService.getTipoManufatto().pipe(first()).subscribe(
      (data: TipoManufatto[]) => {
        let tipoManufatto: TipoManufatto = data.find((x:TipoManufatto)=>x.id===pratica.datiRichiesta.idManufatto);
        if (tipoManufatto) {
          this.setTipoManufatto(tipoManufatto);
        }
      }
    )
    this.onTipoTitoloEdilizioChanged(pratica.datiRichiesta.idTipologiaTitoloEdilizio);
  }

  public closeDialog(): void {
    if (this.router.url === '/gestione_richieste/inserisci_richiesta') {
      this.router.navigate(['home']);
    }
    else {
      this.dialogRef.close(true);
    }
  }

  closeProtocolloDialog(event?) {
    this.showProtocolloDialog = false;
    this.isProtocollata = false;
    this.numProtocollo = '';
    if (this.insertResponse.error && this.pratica.statoPratica.id === environment.statiPratica.bozza) {
      this.messageService.showMessage('info', 'Pratica in bozza', 'Si ricorda che è possibile continuare la lavorazione della pratica nella sezione pratiche in bozza');
    }
    if (
      this.router.url === '/gestione_richieste/inserisci_richiesta'
    ) {
      this.router.navigate(['home']);
    } else {
      this.dialogRef.close(true);
    }
  }

  public isNewPratica(): boolean {
    return this.pratica == null;
  }
  public isStatoBozza(): boolean {
    return this.pratica && this.pratica.statoPratica?.id == environment.statiPratica.bozza;
  }
  public isStatoConcessioneValida(): boolean {
    return this.pratica && this.pratica.statoPratica?.id == environment.statiPratica.concessioneValida;
  }

  public showButtonAvanti(): boolean {
    return (this.isNewPratica() || this.isStatoBozza() || this.isStatoConcessioneValida) && !this.isDettaglio && !this.isAvviaRettifica;
  }

  tipoRinunciaConcessione(){
    if(this.isRinunciaConcessione && !this.isStatoBozza()){
      return true;
    }

    return false;
  }

  checkDisabled(){
    if((this.isRichiestaProroga || this.isRinunciaConcessione) && !this.isStatoBozza()){
      return true;
    }

    return this.readonly;
  }

  isDiffMinutesInvalid: boolean = false;

  isNotValid(key: string, group: string) {
    return this.inserisciPraticaForm.controls[group].get(key)?.dirty
      && this.inserisciPraticaForm.controls[group].get(key)?.touched
      && !this.inserisciPraticaForm.controls[group].get(key)?.valid ? true : false;
  }

  isNotValidFormGroup(key: string, group: string) {
    return this.inserisciPraticaForm.controls[group]?.dirty
      && this.inserisciPraticaForm.controls[group]?.touched
      && this.inserisciPraticaForm.controls[group].hasError(key) ? true : false;
  }

  objectControl(control: AbstractControl): { [key: string]: boolean } | null {
    if (typeof control.value !== 'object') {
      return { 'objectControl': true };
    }
    return null;
  }

  validCodFiscaleOrPartivaIva(control: AbstractControl): { [key: string]: boolean } | null {
    let ret = null;
    if (!control.disabled) {
      var regexCf = /^[a-zA-Z]{6}[0-9]{2}[abcdehlmprstABCDEHLMPRST]{1}[0-9]{2}([a-zA-Z]{1}[a-zA-Z0-9]{3})[a-zA-Z]{1}$/;
      let testCf = regexCf.test(control.value);

      var regexIva = /^[0-9]{11}$/;
      let testIva = regexIva.test(control.value);

      if (!testCf && !testIva) {
        return { 'codiceFiscaleControl': true };
      }
    }

    return ret;
  }

  validDenominazione(nomeControlName: string, cognomeControlName: string, ragSocContrlName: string): ValidatorFn | null{
    return (formGroup: FormGroup) => {
      let ret = null;
      let check = 0;
      const value1 = formGroup.get(nomeControlName)?.value;
      const value2 = formGroup.get(cognomeControlName)?.value;
      const value3 = formGroup.get(ragSocContrlName)?.value;

      if (value1 && value2) {
        check++;
      }
      if ((value1 && !value2)||(!value1 && value2)) {
        check = 2;
      }
      if (value3) {
        check++;
      }

      if (check != 1)
      {
        ret = {validDenominazione: true};
      }
      return ret;
    }
  }

  validDatiDocumento(): ValidatorFn | null {
    return (formGroup: FormGroup) => {
      let ret = null;
      const nome = formGroup.get('nome')?.value;
      const cognome = formGroup.get('cognome')?.value;

      let compIdTipoDoc = formGroup.get('idTipoDocumentoAllegato');
      let compNumeroDoc = formGroup.get('numeroDocumentoAllegato');
      let compAmmDoc = formGroup.get('amministrazioneDocumentoAllegato');
      if (!compIdTipoDoc.disabled && !compNumeroDoc.disabled && !compAmmDoc.disabled) {
        if (compIdTipoDoc.touched || compNumeroDoc.touched || compAmmDoc.touched) {
          const idTipoDocumentoAllegato = compIdTipoDoc?.value;
          const numeroDocumentoAllegato = compNumeroDoc?.value;
          const amministrazioneDocumentoAllegato = compAmmDoc?.value;

          if (nome && cognome) {
            if (!idTipoDocumentoAllegato || !numeroDocumentoAllegato || !amministrazioneDocumentoAllegato) {
              ret = {validDatiDocumento: true};
            }
          }
        }
      }

      return ret;
    }
  }

  private getDateDiff(date1: Date, date2: Date): number {
    let inizio = DateTime.fromISO(date1.toISOString()).startOf('day');
    let fine = DateTime.fromISO(date2.toISOString()).startOf('day');
    return fine.diff(inizio, 'days').values.days;
  }

  private getHourDiff(date1: Date, date2: Date): number {
    date1.setSeconds(0);
    date2.setSeconds(0);
    let inizio = DateTime.fromISO(date1.toISOString()).startOf('minute');
    let fine = DateTime.fromISO(date2.toISOString()).startOf('minute');
    return fine.diff(inizio, 'minutes').values.minutes;
  }

  validDateRange(date1ControlName: string, date2ControlName: string): ValidatorFn | null {
    return (formGroup: FormGroup) => {
      let ret = null;
      const date1: Date = formGroup.get(date1ControlName)?.value;
      const date2: Date = formGroup.get(date2ControlName)?.value;

      if (date1 && date2) {
        let days = this.getDateDiff(date2, date1);
        if (days > 0) {
          ret = {validDateRange: true};
        }
      }

      return ret;
    }
  }

  validOrariRange(hour1ControlName: string, hour2ControlName: string, date1ControlName: string, date2ControlName: string): ValidatorFn | null {
    return (formGroup: FormGroup) => {
      let ret = null;
      const hour1: Date = formGroup.get(hour1ControlName)?.value;
      const hour2: Date = formGroup.get(hour2ControlName)?.value;
      const date1: Date = formGroup.get(date1ControlName)?.value;
      const date2: Date = formGroup.get(date2ControlName)?.value;

      if (hour1 && hour2 && date1 && date2) {
        let days = this.getDateDiff(date2, date1);
        if (days == 0) {
          let minutes = this.getHourDiff(hour1, hour2);
          if (minutes < environment.diffMininimaOreOccupazione ) {
            ret = {validOrariRange: true};
          }
        }
      }

      return ret;
    }
  }

  calculateYearBirth(): string {
    let min = new Date().getFullYear() - 100;
    let max = new Date().getFullYear() + 1;
    return min.toString() + ":" + max.toString();
  }

  calculateYearRange(): string {
    let min = new Date().getFullYear() - 10;
    let max = new Date().getFullYear() + 20;
    return min.toString() + ":" + max.toString();
  }


  destinatarioCheck(ragsociale?: boolean){
    const nome: string | null = this.inserisciPraticaForm.controls['destinatario'].get('nome').value;
    const cognome: string | null = this.inserisciPraticaForm.controls['destinatario'].get('cognome').value;
    const denominazione: string | null = this.inserisciPraticaForm.controls['destinatario'].get('denominazione').value;

    //Caso che gestisce le richieste di Proroga e Rinuncia
    if(ragsociale === undefined){
      if(nome || cognome){
        ragsociale = false;
      } else if(denominazione) {
        ragsociale = true;
      }
    }

    if (!ragsociale) {
      this.inserisciPraticaForm.get('destinatario.nome').setValidators([Validators.required]);
      this.inserisciPraticaForm.get('destinatario.nome').updateValueAndValidity();
      this.inserisciPraticaForm.get('destinatario.cognome').setValidators([Validators.required]);
      this.inserisciPraticaForm.get('destinatario.cognome').updateValueAndValidity();
      this.inserisciPraticaForm.get('destinatario.idTipoDocumentoAllegato').setValidators([Validators.required]);
      this.inserisciPraticaForm.get('destinatario.idTipoDocumentoAllegato').updateValueAndValidity();
      this.inserisciPraticaForm.get('destinatario.numeroDocumentoAllegato').setValidators([Validators.required]);
      this.inserisciPraticaForm.get('destinatario.numeroDocumentoAllegato').updateValueAndValidity();
      this.inserisciPraticaForm.get('destinatario.amministrazioneDocumentoAllegato').setValidators([Validators.required]);
      this.inserisciPraticaForm.get('destinatario.amministrazioneDocumentoAllegato').updateValueAndValidity();
      this.inserisciPraticaForm.get('destinatario.denominazione').clearValidators();
      this.inserisciPraticaForm.get('destinatario.denominazione').patchValue(null);
      this.ragsocDestinatario = false;
    } else {
      this.inserisciPraticaForm.get('destinatario.nome').clearValidators();
      this.inserisciPraticaForm.get('destinatario.cognome').clearValidators();
      this.inserisciPraticaForm.get('destinatario.nome').patchValue(null);
      this.inserisciPraticaForm.get('destinatario.cognome').patchValue(null);

      this.inserisciPraticaForm.get('destinatario.idTipoDocumentoAllegato').clearValidators();
      //this.inserisciPraticaForm.get('destinatario.idTipoDocumentoAllegato').setErrors(null);
      this.inserisciPraticaForm.get('destinatario.numeroDocumentoAllegato').clearValidators();
      this.inserisciPraticaForm.get('destinatario.amministrazioneDocumentoAllegato').clearValidators();
      this.inserisciPraticaForm.get('destinatario.idTipoDocumentoAllegato').patchValue(null);
      this.inserisciPraticaForm.get('destinatario.numeroDocumentoAllegato').patchValue(null);
      this.inserisciPraticaForm.get('destinatario.amministrazioneDocumentoAllegato').patchValue(null);

      this.inserisciPraticaForm.get('destinatario.denominazione').setValidators([Validators.required]);
      this.ragsocDestinatario = true;
    }

    this.inserisciPraticaForm.updateValueAndValidity();
  }

  tipoRuloChanged($event) {
    this.showAnagraficaDestinatario = $event.value !== environment.roles.idRuoloPersonaFisica;
    this.isRuoloAltro = $event.value === environment.roles.idRuoloAltro;
    this.isRuoloPersonaFisica = $event.value === environment.roles.idRuoloPersonaFisica;
    if (this.showAnagraficaDestinatario) {
      this.inserisciPraticaForm.controls['destinatario'].enable();
      this.inserisciPraticaForm.controls['destinatario'].get('nome').setValidators([Validators.required]);
      this.inserisciPraticaForm.controls['destinatario'].get('cognome').setValidators([Validators.required]);
      this.inserisciPraticaForm.controls['destinatario'].get('codiceFiscalePartitaIva').setValidators([Validators.required, this.validCodFiscaleOrPartivaIva]);
      this.inserisciPraticaForm.controls['destinatario'].get('citta').setValidators([Validators.required]);
      this.inserisciPraticaForm.controls['destinatario'].get('indirizzo').setValidators([Validators.required]);
      this.inserisciPraticaForm.controls['destinatario'].get('civico').setValidators([Validators.required]);
      this.inserisciPraticaForm.controls['destinatario'].get('provincia').setValidators([Validators.required]);
      this.inserisciPraticaForm.controls['destinatario'].get('recapitoTelefonico').setValidators([Validators.required, Validators.minLength(9), Validators.maxLength(10)]);
      this.inserisciPraticaForm.controls['destinatario'].get('email').setValidators([Validators.required, Validators.email]);
      this.inserisciPraticaForm.controls['destinatario'].get('idTipoDocumentoAllegato').setValidators([Validators.required]);
      this.inserisciPraticaForm.controls['destinatario'].get('numeroDocumentoAllegato').setValidators([Validators.required]);
      this.inserisciPraticaForm.controls['destinatario'].get('amministrazioneDocumentoAllegato').setValidators([Validators.required]);
      this.inserisciPraticaForm.controls['destinatario'].setValidators([
        //this.validDenominazione('nome', 'cognome', 'denominazione'),
        //this.validDatiDocumento()
      ]);

      this.inserisciPraticaForm.controls['destinatario'].enable();

    } else {
      this.inserisciPraticaForm.controls['destinatario'].get('nome').clearValidators();
      this.inserisciPraticaForm.controls['destinatario'].get('cognome').clearValidators();
      this.inserisciPraticaForm.controls['destinatario'].get('denominazione').clearValidators();
      this.inserisciPraticaForm.controls['destinatario'].get('codiceFiscalePartitaIva').clearValidators();
      this.inserisciPraticaForm.controls['destinatario'].get('citta').clearValidators();
      this.inserisciPraticaForm.controls['destinatario'].get('indirizzo').clearValidators();
      this.inserisciPraticaForm.controls['destinatario'].get('civico').clearValidators();
      this.inserisciPraticaForm.controls['destinatario'].get('provincia').clearValidators();
      this.inserisciPraticaForm.controls['destinatario'].get('recapitoTelefonico').clearValidators();
      this.inserisciPraticaForm.controls['destinatario'].get('email').clearValidators();
      this.inserisciPraticaForm.controls['destinatario'].get('idTipoDocumentoAllegato').clearValidators();
      this.inserisciPraticaForm.controls['destinatario'].get('numeroDocumentoAllegato').clearValidators();
      this.inserisciPraticaForm.controls['destinatario'].get('amministrazioneDocumentoAllegato').clearValidators();

      this.inserisciPraticaForm.controls['destinatario'].disable();
    }
    if (this.isRuoloAltro) {
      this.inserisciPraticaForm.get('firmatario').get('descrizioneRuolo').setValidators([Validators.required]);
    } else {
      this.inserisciPraticaForm.get('firmatario').get('descrizioneRuolo').clearValidators();
      this.inserisciPraticaForm.get('firmatario').get('descrizioneRuolo').reset();
    }
    if (this.isRuoloPersonaFisica) {
      this.inserisciPraticaForm.get('firmatario').get('qualitaRuolo').setValidators([Validators.required]);
    } else {
      this.inserisciPraticaForm.get('firmatario').get('qualitaRuolo').clearValidators();
      this.inserisciPraticaForm.get('firmatario').get('qualitaRuolo').reset();
    }
    this.inserisciPraticaForm.updateValueAndValidity();
  }

  showAttivitaDaSvolgereAltro: number = 0;

  setTipoAttivitaDaSvolgere(element: TipologicaTestoLibero) {
    this.showAttivitaDaSvolgereAltro = element.flagTestoLibero?2:1;

    if (element.flagTestoLibero) {
      this.inserisciPraticaForm.get('datiRichiesta.idTipologiaTitoloEdilizio').patchValue(null);
      this.inserisciPraticaForm.get('datiRichiesta.riferimentoTitoloEdilizio').patchValue(null);
      this.inserisciPraticaForm.get('datiRichiesta.idTipologiaTitoloEdilizio').disable();
      this.inserisciPraticaForm.get('datiRichiesta.riferimentoTitoloEdilizio').disable();
      this.inserisciPraticaForm.get('datiRichiesta.idTipologiaTitoloEdilizio').clearValidators();
      this.inserisciPraticaForm.get('datiRichiesta.riferimentoTitoloEdilizio').clearValidators();
      this.inserisciPraticaForm.get('datiRichiesta.descrizioneAttivitaDaSvolgere').enable();
      this.inserisciPraticaForm.get('datiRichiesta.descrizioneAttivitaDaSvolgere').setValidators([Validators.required]);
      this.inserisciPraticaForm.get('datiRichiesta.descrizioneTitoloEdilizio').patchValue(null);
      this.inserisciPraticaForm.get('datiRichiesta.descrizioneTitoloEdilizio').disable();
      this.inserisciPraticaForm.get('datiRichiesta.descrizioneTitoloEdilizio').clearValidators();
      this.showDescrizioneTitoloEdilizio = false;
    }
    else {
      this.inserisciPraticaForm.get('datiRichiesta.descrizioneAttivitaDaSvolgere').patchValue(null);
      this.inserisciPraticaForm.get('datiRichiesta.descrizioneAttivitaDaSvolgere').disable();
      this.inserisciPraticaForm.get('datiRichiesta.descrizioneAttivitaDaSvolgere').clearValidators();
      this.inserisciPraticaForm.get('datiRichiesta.idTipologiaTitoloEdilizio').enable();
      this.inserisciPraticaForm.get('datiRichiesta.idTipologiaTitoloEdilizio').setValidators([Validators.required]);
      this.inserisciPraticaForm.get('datiRichiesta.riferimentoTitoloEdilizio').enable();
      this.inserisciPraticaForm.get('datiRichiesta.riferimentoTitoloEdilizio').setValidators([Validators.required]);
    }
    this.inserisciPraticaForm.updateValueAndValidity();
  }

  showManufattoAltro: boolean = false;
  previuousManufatto: number = null;

  setTipoManufatto(element: TipoManufatto) {
    if (element.id === this.previuousManufatto) {
      this.inserisciPraticaForm.get('datiRichiesta.idManufatto').patchValue(null);
      this.hideDescrizioneManufatto();
      this.previuousManufatto = null;
    }
    else {
      this.previuousManufatto = element.id;

      if (element.flagTestoLibero) {
        this.showManufattoAltro = true;
        this.inserisciPraticaForm.get('datiRichiesta.descrizioneManufatto').enable();
        this.inserisciPraticaForm.get('datiRichiesta.descrizioneManufatto').setValidators([Validators.required]);
      }
      else {
        this.hideDescrizioneManufatto();
      }
    }
    this.inserisciPraticaForm.updateValueAndValidity();
  }

  private hideDescrizioneManufatto(): void {
    this.inserisciPraticaForm.get('datiRichiesta.descrizioneManufatto').patchValue(null);
    this.inserisciPraticaForm.get('datiRichiesta.descrizioneManufatto').clearValidators();
    this.inserisciPraticaForm.get('datiRichiesta.descrizioneManufatto').disable();
    this.showManufattoAltro = false;
  }

  orarioRequired: boolean = false;
  onPeriodOccupazioneChange() {
    let dataInizio: Date = this.inserisciPraticaForm.get('datiRichiesta.dataInizioOccupazioneForm').value;
    let dataFine: Date = this.inserisciPraticaForm.get('datiRichiesta.dataScadenzaOccupazioneForm').value;
    if (dataInizio && dataFine) {
      let days = this.getDateDiff(dataFine, dataInizio);

      if (days == 0) {
        this.orarioRequired = true;
      } else {
        this.orarioRequired = false;
      }
      this.inserisciPraticaForm.updateValueAndValidity();
    }
  }

  onSelectOraInizioOccupazione($event) {
    this.onPeriodOccupazioneChange();
    //this.inserisciPraticaForm.get('datiRichiesta.oraInizioOccupazione').patchValue(this.getTime($event));
  }

  onSelectOraScadenzaOccupazione($event) {
    this.onPeriodOccupazioneChange();
    //this.inserisciPraticaForm.get('datiRichiesta.oraScadenzaOccupazione').patchValue(this.getTime($event));

  }

  tickflagNumeroCivicoAssente(event){
    if(event.numeroCivico == null){
      this.inserisciPraticaForm.get('datiRichiesta.flagNumeroCivicoAssente').patchValue(true);
      this.inserisciPraticaForm.get('datiRichiesta').get('flagNumeroCivicoAssente').disable();
    } else{
      this.inserisciPraticaForm.get('datiRichiesta.flagNumeroCivicoAssente').patchValue(false);
      this.inserisciPraticaForm.get('datiRichiesta').get('flagNumeroCivicoAssente').enable();
    }
  }

  showDescrizioneTitoloEdilizio = false;
  onTipoTitoloEdilizioChanged(idTipoTitoloEdilizio: number) {

    if (idTipoTitoloEdilizio === environment.tipologiaTitoloEdilizio.altro) {
      this.showDescrizioneTitoloEdilizio = true;
      this.inserisciPraticaForm.get('datiRichiesta.descrizioneTitoloEdilizio').enable();
      this.inserisciPraticaForm.get('datiRichiesta.descrizioneTitoloEdilizio').setValidators([Validators.required]);
    }
    else {
      this.inserisciPraticaForm.get('datiRichiesta.descrizioneTitoloEdilizio').patchValue(null);
      this.inserisciPraticaForm.get('datiRichiesta.descrizioneTitoloEdilizio').disable();
      this.inserisciPraticaForm.get('datiRichiesta.descrizioneTitoloEdilizio').clearValidators();
      this.showDescrizioneTitoloEdilizio = false;
    }
    this.inserisciPraticaForm.updateValueAndValidity();
  }

  private getTime($event): string {
    let hour = new Date($event).getHours();
    let min: string = '' + new Date($event).getMinutes();
    if (min.length == 1) {
      min = `0${min}`;
    }
    return `${hour}:${min}`;
  }

  changeEsenzione() {
    if (!this.inserisciPraticaForm.get('datiRichiesta.flagEsenzioneMarcaDaBollo').value) {
      this.inserisciPraticaForm.get('datiRichiesta.motivazioneEsenzioneMarcaBollo').reset();
      this.inserisciPraticaForm.get('datiRichiesta.motivazioneEsenzioneMarcaBollo').disable();
    }
    else {
      this.inserisciPraticaForm.get('datiRichiesta.motivazioneEsenzioneMarcaBollo').enable();
    }
  }

  isEsente(value): boolean {
    return value ? true : false;
  }

  searchCivilario(event) {
    this.addInserisciIndirizzoManualmente = false;
    return this.civiliarioService.civico(event.query, this.authService.getMunicipio())
      .toPromise()
      .then((data: CiviliarioResponse[]) => {
        if(data && data.length) {
          this.civilarioResults = data.map(
            (dato: CiviliarioResponse) => {
              dato.indirizzo = this.civiliarioService.addressListFormatter(dato);
              dato.municipio = dato.municipio?dato.municipio.replace(/\D/g, ''): null;
              return dato;
              /*
              return {
                indirizzo: this.addressListFormatter(dato),
                location: { lat: dato.lat, lon: dato.lon },
                municipio_id: dato.municipio ? parseInt(dato.municipio.replace(/\D/g, '')) : null,
                localita: dato.localita
              }*/
            });
        }
        else {
          this.civilarioResults = [];
          this.messageService.showMessage('warn', 'Ricerca indirizzo', 'Indirizzo non presente. Rivolgersi alla toponomastica per il censimento'); // o inserirlo manualmente
        }
    });
  }

  confermaDatiDialog() {
    if(this.inserisciPraticaForm.valid && !this.isAvviaRettifica) {
      this.confirmationService.confirm({
        icon: "pi pi-exclamation-triangle",
        acceptLabel: "Conferma",
        rejectLabel: "Annulla",
        acceptButtonStyleClass: "btn-custom-style btn-dialog-confirm",
        rejectButtonStyleClass: "btn-custom-style btn-dialog-confirm",
        header: this.isStatoBozza()?"Modifica richiesta": "Inserisci richiesta",
        message: "Confermi i dati inseriti prima di continuare con l'upload dei documenti?",
        accept: () => {
          this.inserisciPratica();
        }
      });
    } else if(this.inserisciPraticaForm.valid && this.isAvviaRettifica){
      this.inserisciPratica();
    }
    else {
      this.utilityService.markAsDirtied(this.inserisciPraticaForm);
      this.messageService.showMessage('error', 'Inserimento pratica', 'Si prega di inserire tutte le informazioni obbligatorie prima di proseguire');
    }
  }

  private inserisciPratica(){
    this.spinnerService.showSpinner(true);
    this.inserisciPraticaForm.get('firmatario').get('idTipoRuoloRichiedente').enable();
    this.inserisciPraticaForm.get('firmatario').get('qualitaRuolo').enable();
    this.inserisciPraticaForm.get('firmatario').get('descrizioneRuolo').enable();
    let request: PraticaRequest = this.inserisciPraticaForm.value;

    if (this.pratica) {
      request.id = this.pratica.id;
      request.idPraticaOriginaria = this.pratica.idPraticaOriginaria;
      request.datiRichiesta.id = this.pratica.datiRichiesta.id;
      request.firmatario.id = this.pratica.firmatario.id;
      if (this.pratica.destinatario && this.pratica.destinatario.id) {
        request.destinatario.id = this.pratica.destinatario.id;
      }
    }
    request.idUtente = this.authService.getLoggedUser().userLogged.id;
    request.idMunicipio = this.authService.getMunicipio();
    if(this.isRichiestaProroga){
      request.idTipoProcesso = environment.processes.prorogaConcessioneTemporanea;
    } else if(this.isRinunciaConcessione){
      request.idTipoProcesso = environment.processes.rinunciaConcessione;
    } else {
      request.idTipoProcesso = environment.processes.concessioneTemporanea;
    }
    //request.idTipoProcesso = this.isRichiestaProroga ? environment.processes.prorogaConcessioneTemporanea : environment.processes.concessioneTemporanea;
    request.firmatario.dataDiNascita = this.utilityService.formatDateForBe(this.inserisciPraticaForm.controls['firmatario'].get('dataDiNascitaForm').value);
    request.datiRichiesta.dataInizioOccupazione = this.utilityService.formatDateForBe(this.inserisciPraticaForm.controls['datiRichiesta'].get('dataInizioOccupazioneForm').value);
    request.datiRichiesta.dataScadenzaOccupazione = this.utilityService.formatDateForBe(this.inserisciPraticaForm.controls['datiRichiesta'].get('dataScadenzaOccupazioneForm').value);
    request.datiRichiesta.oraInizioOccupazione = this.utilityService.formatTimeForBe(this.inserisciPraticaForm.get('datiRichiesta.oraInizioOccupazioneForm').value);
    request.datiRichiesta.oraScadenzaOccupazione = this.utilityService.formatTimeForBe(this.inserisciPraticaForm.get('datiRichiesta.oraScadenzaOccupazioneForm').value);
    if (request.firmatario.idTipoRuoloRichiedente == environment.roles.idRuoloPersonaFisica) {
      request.destinatario = null;
    }
    else {
      request.destinatario.idTipoRuoloRichiedente = environment.roles.idRuoloPersonaFisica;
    }

    let datoCiviliario: CiviliarioResponse = this.inserisciPraticaForm.controls['datiRichiesta'].get('datoCiviliario').value;
    request.datiRichiesta.ubicazioneOccupazione = datoCiviliario.indirizzo;
    request.datiRichiesta.nome_via = datoCiviliario.nome_via;
    request.datiRichiesta.numero = datoCiviliario.numero;
    request.datiRichiesta.cod_via = datoCiviliario.cod_via;
    request.datiRichiesta.idMunicipio = this.authService.getMunicipio(); //parseInt(datoCiviliario.municipio);
    request.datiRichiesta.localita = datoCiviliario.localita;
    request.datiRichiesta.coordUbicazioneTemporanea.points[0].lat = datoCiviliario.lat;
    request.datiRichiesta.coordUbicazioneTemporanea.points[0].lon = datoCiviliario.lon;
    request.datiRichiesta.flagNumeroCivicoAssente = this.inserisciPraticaForm.controls['datiRichiesta'].get("flagNumeroCivicoAssente").value;
    if (
        this.isNewPratica() ||
        (this.isRichiestaProroga && !this.isStatoBozza() && !this.isAvviaRettifica) ||
        (this.isRinunciaConcessione && !this.isStatoBozza() && !this.isAvviaRettifica)
    ) {
      this.praticaService.inserisciPratica(request).subscribe(
        (resp: PraticaDto) => {
          this.pratica = resp;
          this.spinnerService.showSpinner(false);
          this.showUploadAllegati();
        },
        err => {
          console.log("Error: ", err);
          this.spinnerService.showSpinner(false);
          this.messageService.showErrorMessage('Inserisci richiesta', err);
        }
      );
    } else if (this.isAvviaRettifica){
      request.codiceDeterminaRettifica = this.identificativoDetermina;
      request.dataEmissioneDeterminaRettifica = this.utilityService.formatDateForBe( '' + this.dataDetermina);
      this.praticaService.rettificaConcessione(request).subscribe(
        (resp: PraticaDto) => {
          this.newRettificaUploadedFiles = [];
          this.insertResponse = resp;
          this.spinnerService.showSpinner(false);
          this.messageService.showMessage('success', 'Rettifica pratica',"La rettifica della pratica è avvenuta con successo.");
          if(this.insertResponse !== null) {
            this.isProtocollata = this.insertResponse.protocolli.length > 0 ;
          } else {
            this.isProtocollata = false;
          }
          this.numProtocollo = this.utilityService.getLastProtocol(this.insertResponse.protocolli);
          this.showUploadDeterminaDialog = false;
          this.showProtocolloDialog = true;
          // this.closeDialog();
        },
        err => {
          console.log("Error: ", err);
          this.closeUploadDeterminaDialog();
          this.spinnerService.showSpinner(false);
          this.messageService.showErrorMessage('Rettifica pratica', err);
          this.insertResponse = {
            error: err
          };
          /*this.numProtocollo = '--|--';
          this.showProtocolloDialog = true;*/
        }
      );
    }
    else {
      this.praticaService.modificaPratica(request).subscribe(
        (resp: any) => {
          this.spinnerService.showSpinner(false);
          this.showUploadAllegati();
        },
        err => {
          console.log("Error: ", err);
          this.spinnerService.showSpinner(false);
          this.messageService.showErrorMessage('Modifica richiesta', err);
        }
      );
    }
  }

  private showUploadAllegati(): void {
    const emitter = new EventEmitter();
    emitter.subscribe((next) => {
      this.onUploadedFile(next);
    })
    let config: UploadDialogConfig = {
      pratica: this.pratica,
      mode: Mode.MULTIPLE,
      readonly: false,
      destinazioneAllegato: DestinazioneAllegato.PRATICA,
      onDocUploaded: emitter
    }
    this.uploadDialog(config);
  }

  uploadDialog(config: UploadDialogConfig) {
    let dialogRef = this.dialogService.open(
      UploadFileComponent,
      this.utilityService.configDynamicDialogFullScreen(config, "Allega documenti alla pratica")
    );
    dialogRef.onClose.subscribe((success: boolean) => {
      if (success) {
        this.inserimentoPratica();
        // this.router.url === '/gestione_richieste/inserisci_richiesta' ? this.router.navigate(['/home']) : this.closeDialog();
      } else {
        if (this.pratica.statoPratica.id === environment.statiPratica.bozza) {
          this.closeDialog();
          this.messageService.showMessage('info', 'Pratica in bozza', 'Si ricorda che è possibile continuare la lavorazione della pratica nella sezione pratiche in bozza');
        }
        /*if(this.uploadedFiles.length > 0) {
          const promises = this.uploadedFiles.map((allegato, index) => {
            return this.allegatiService.deleteAllegato(allegato.id).toPromise();
          });
          Promise.all(promises).finally(() => {
            this.uploadedFiles = [];
          });
        }*/
      }
    });
  }

  inserimentoPratica() {
    this.spinnerService.showSpinner(true);
    this.praticaService.statoInserita(this.pratica.id, this.authService.getLoggedUser().userLogged.id).subscribe(
      (data: PraticaDto)=>{
        this.insertResponse = data;
        this.inserisciPraticaForm.clearValidators();
        this.inserisciPraticaForm.disable();
        this.spinnerService.showSpinner(false);
        this.messageService.showMessage('success', 'Inserisci richiesta', this.isRinunciaConcessione ? 'La richiesta di rinuncia concessione è avvenuta con successo' : 'La pratica è stata inoltrata al municipio di appartenenza');
        this.isProtocollata = this.insertResponse.protocolli.length > 0 ;
        this.numProtocollo = this.insertResponse.protocolli.length ? `${this.insertResponse.protocolli[0].numeroProtocollo}|${this.insertResponse.protocolli[0]?.anno}` : ` --|--`;
        this.showProtocolloDialog = true;
      },
      err=> {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage("Inserisci richiesta", err);
        this.insertResponse = {
          error: err
        };
        this.numProtocollo = '--|--';
        this.showProtocolloDialog = true;
      }
    );
  }

  onApriMappaClicked(): void {
    let data = {
      elements: this.mappaElements,
      fullScreen: true,
      enableHeatmap: true,
      showDetailButton: false,
      visualizzaMappaClicked: true
    }

    let dialogRef = this.dialogService.open(MappaComponent, this.utilityService.configDynamicDialogFullScreen(data, "Ubicazione pratica cittadino"));

    dialogRef.onClose.subscribe((element) => {
      if (element) {
        //this.dettaglioPratica(el);
      }
    });
  }

  inserisciIndirizzoManualmente(){
    this.showInserisciIndirizzoManualmenteDialog = true;
  }

  confermaInserimentoIndirizzoManualmente(){
    this.nominatim_option.nome_via = this.nominatim_option.nome_via.toUpperCase();
    this.nominatim_option.esponente = this.nominatim_option.esponente.toUpperCase();
    this.nominatim_option.localita = this.nominatim_option.localita.toUpperCase();
    this.nominatim_option.municipio = `MUNICIPIO N.${this.nominatim_option.municipio}`;

    this.closeInserisciIndirizzoManualmenteDialog();
  }

  closeInserisciIndirizzoManualmenteDialog(event?) {
    this.showInserisciIndirizzoManualmenteDialog = false;
    this.addInserisciIndirizzoManualmente = false;

    this.nominatim_option = {
      nome_via: '',
      numero: null,
      esponente: '',
      municipio: null,
      localita: '',
      lat: null,
      lon: null
    };
  }

  onMandatoryDocuments(event: boolean) {
    this.mandatoryDocumentsAttached = event;
  }

  determinaOptions: any[] = [
    { label: "Download template determina di rettifica", value: 2 },
    { label: "Upload determina di rettifica", value: 1 }
  ];

  determinaActions(el){
    if(el.value == 1){
      this.showUploadDeterminaDialog = true
      this.filtroStatoPratica = this.pratica.statoPratica.id;
    } else {
      this.downloadDetermina();
    }
  }

  isConfirmDisabled(): boolean {
    return !(this.identificativoDetermina.trim().length > 0 && this.mandatoryDocumentsAttached);
  }

  downloadDetermina(): void {
    this.templateService.getTemplateElaborato(this.pratica.id, environment.template.determinaRettifica).subscribe(
      (data: TemplateDTO) => {
        this.utilityService.downloadFile(data.nomeFile, data.mimeType, data.fileTemplate);
      },
      err => {this.messageService.showErrorMessage("Template determina", err);}
    );
  }

   closeUploadDeterminaDialog(): void {
    this.showUploadDeterminaDialog = false;
    this.identificativoDetermina = '';
    this.dataDetermina = null;
    this.confermaDatiDetermina = false;
    if(this.newRettificaUploadedFiles.length > 0) {
      const promises = this.newRettificaUploadedFiles.map((allegato, index) => {
        return this.allegatiService.deleteAllegato(allegato.id).toPromise();
      });
      Promise.all(promises).finally(() => {
        this.newRettificaUploadedFiles = [];
      });
    }
  }


  resetProtocollo() {
    this.showProtocolloDialog = false;
    this.isProtocollata = false;
    this.numProtocollo = '';
  }

  onUploadedFile(event): void {
    this.uploadedFiles.push(event);
  }

  onRettificaUploadedFile(event): void {
    this.newRettificaUploadedFiles.push(event);
  }

  onNameKeyup(event?) {
    const id =  event.target.attributes.id.value;
    const value = event.target.value;
    switch(id) {
      case("txtDestinatarioNome"): {
        this.inserisciPraticaForm.get("destinatario").get("nome").patchValue(this.utilityService.toTitleCase(value));
        break;
      }
      case("txtDestinatarioCognome"): {
        this.inserisciPraticaForm.get("destinatario").get("cognome").patchValue(this.utilityService.toTitleCase(value));
        break;
      }
      case("txtFirmatarioNome"): {
        this.inserisciPraticaForm.get("firmatario").get("nome").patchValue(this.utilityService.toTitleCase(value));
        break;
      }
      case("txtFirmatarioCognome"): {
        this.inserisciPraticaForm.get("firmatario").get("cognome").patchValue(this.utilityService.toTitleCase(value));
        break;
      }
    }
  }
}
