import { Component, Input, OnInit } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { GruppoDTO } from '@models/dto/gruppo-dto';
import { PraticaDto } from '@models/dto/pratica-dto';
import { RichiestaIntegrazioneDTO } from '@models/dto/richiesta-integrazione-dto';
import { RichiestaParereDTO } from '@models/dto/richiesta-parere-dto';
import { MessageService } from '@services/message.service';
import { TipologicheService } from '@services/tipologiche.service';
import { environment } from 'environments/environment';
import { DateTime } from 'luxon';


@Component({
  selector: 'app-pareri',
  templateUrl: './pareri.component.html',
  styleUrls: ['./pareri.component.css']
})
export class PareriComponent implements OnInit {
  statoPraticaArchiviata = environment.statiPratica.archiviata;
  @Input() pratica: PraticaDto = null;

  pareriForm: FormGroup;
  tipoGruppi: GruppoDTO[] = [];

  competenza: any[] = [
    { label: "Competenza", value: true },
    { label: "Non Competenza", value: false }
  ];

  parere: any[] = [
    { label: "Positivo", value: true },
    { label: "Negativo", value: false },
    { label: "Non di Competenza", value: null }
  ];

  constructor(
    private fb: FormBuilder,
    private tipologicheService: TipologicheService,
    private messageService: MessageService
  ) {
    this.buildForm();
  }

  ngOnInit(): void {
    this.tipologicheService.getGruppi().subscribe(
      (data: GruppoDTO[]) => {
        this.tipoGruppi = data;
      },
      err => {
        this.messageService.showErrorMessage("Recupero gruppi", err);
      }
    );
    this.initForm();
  }

  private buildForm() {
    this.pareriForm = this.fb.group({
      dataScadenzaDiniego:[{value: null, disabled: true }],
      pareriMunicipio: this.fb.array([]),
      pareriPoliziaLocale: this.fb.array([]),
      pareriAltri: this.fb.array([]),
    });
  }

  get pareriMunicipio() {
    return this.pareriForm.controls["pareriMunicipio"] as FormArray;
  }

  get pareriPoliziaLocale() {
    return this.pareriForm.controls["pareriPoliziaLocale"] as FormArray;
  }

  get pareriAltri() {
    return this.pareriForm.controls["pareriAltri"] as FormArray;
  }

  getDescrizioneGruppo(id: number): string {
    return this.tipoGruppi.find((x: GruppoDTO)=> x.id===id)?.descrizione;
  }

  private initForm() {
    if (this.pratica.dataScadenzaPreavvisoDiniego) {
      this.pareriForm.get('dataScadenzaDiniego').setValue(new Date(this.pratica.dataScadenzaPreavvisoDiniego));
    }
    let pareriMunicipio: RichiestaIntegrazioneDTO[] = this.findPareriMunicipio();
    if (pareriMunicipio && pareriMunicipio.length>0) {
      pareriMunicipio.forEach((parere:RichiestaIntegrazioneDTO)=> {
        this.pareriMunicipio.push(this.fb.group({
          motivoRichiesta: [parere.motivoRichiesta],
          dataScadenzaIntegrazione: [new Date(parere.dataScadenza)],
          notaCittadino: [parere.integrazione?parere.integrazione.motivoIntegrazione:null]
        }));
      });
    }
    let pareriPoliziaLocale: RichiestaParereDTO[] = this.findPareriPoliziaLocale();
    if (pareriPoliziaLocale && pareriPoliziaLocale.length>0) {
      pareriPoliziaLocale.forEach((parere:RichiestaParereDTO) => {
        this.pareriPoliziaLocale.push(this.fb.group({
          idStatoPratica: [parere.idStatoPratica],
          idGruppoDestinatarioParere: [parere.idGruppoDestinatarioParere],
          competenza: [parere.parere.flagCompetenza],
          parere: [parere.parere.esito],
          note: [parere.parere.nota],
        }));
      });
    }
    let pareriAltri: RichiestaParereDTO[] = this.findAltriPareri();
    if (pareriAltri && pareriAltri.length>0) {
      pareriAltri.forEach((parere: RichiestaParereDTO) => {
        this.pareriAltri.push(this.fb.group({
          idGruppoDestinatarioParere: [parere.idGruppoDestinatarioParere],
          competenza: [parere.parere.flagCompetenza],
          parere: [parere.parere.esito],
          note: [parere.parere.nota],
        }));
      });
    }
  }

  showParereMunicipio(): boolean {
    return this.findPareriMunicipio().length>0;
  }

  showPareriPoliziaLocale(): boolean {
    return this.findPareriPoliziaLocale().length>0;
  }

  showAltriPareri(): boolean {
    return this.findAltriPareri().length>0;
  }

  private cachePareriMunicipio: RichiestaIntegrazioneDTO[] = undefined;
  private cachePareriPoliziaLocale: RichiestaParereDTO[] = undefined;
  private cacheParereAltri: RichiestaParereDTO[] = undefined;

  private findPareriMunicipio(): RichiestaIntegrazioneDTO[] {
    let ret: RichiestaIntegrazioneDTO[] = [];
    if (this.cachePareriMunicipio !== undefined) {
      ret = this.cachePareriMunicipio;
    }
    else {
      if (this.pratica.richiesteIntegrazioni && this.pratica.richiesteIntegrazioni.length > 0) {
        //ret = this.pratica.richiesteIntegrazioni.filter((x: RichiestaIntegrazioneDTO) => x.integrazione == null);
        // Ordinamento per data discendente per selezionare il parere più recente
        let pareriMunicipio = this.pratica.richiesteIntegrazioni;
        pareriMunicipio = pareriMunicipio.sort((e1:RichiestaIntegrazioneDTO, e2:RichiestaIntegrazioneDTO) => {
          let inizio = DateTime.fromISO(new Date(e1.dataInserimento).toISOString()).startOf('day');
          let fine = DateTime.fromISO(new Date(e2.dataInserimento).toISOString()).startOf('day');
          return fine.diff(inizio, 'days').values.days;
        });
        ret = pareriMunicipio;
        this.cachePareriMunicipio = ret;
      }
    }
    return ret;
  }

  private findPareriPoliziaLocale(): RichiestaParereDTO[] {
    let ret: RichiestaParereDTO[] = [];
    if (this.cachePareriPoliziaLocale !== undefined) {
      ret = this.cachePareriPoliziaLocale;
    }
    else {
      if (this.pratica.richiestePareri && this.pratica.richiestePareri.length > 0) {
        // estraggo i pareri della polizia locale
        let pareri: RichiestaParereDTO[] = this.pratica.richiestePareri.filter(
          (x: RichiestaParereDTO)=>
            x.idGruppoDestinatarioParere === environment.groups.idGruppoPoliziaLocale &&
            x.parere != null
        );
        // ottengo l'ultimo parere
        if (pareri && pareri.length > 0) {
          // Ordinamento per data discendente per selezionare il parere più recente
          pareri = pareri.sort((e1:RichiestaParereDTO, e2:RichiestaParereDTO) => {
            let inizio = DateTime.fromISO(new Date(e1.dataInserimento).toISOString()).startOf('day');
            let fine = DateTime.fromISO(new Date(e2.dataInserimento).toISOString()).startOf('day');
            return fine.diff(inizio, 'days').values.days;
          });
          //ret= pareri[0]; // Estrazione ultimo parere
          ret = pareri;
          this.cachePareriPoliziaLocale = ret;
        }
      }
    }
    return ret;
  }

  private findAltriPareri(): RichiestaParereDTO[] {
    let ret: RichiestaParereDTO[] = [];

    if (this.cacheParereAltri !== undefined) {
      ret = this.cacheParereAltri;
    }
    else {
      let pareriMap: Map<number, RichiestaParereDTO> = new Map<number, RichiestaParereDTO>();
      if (this.pratica.richiestePareri && this.pratica.richiestePareri.length > 0) {
        // estraggo i pareri NON della polizia locale
        let pareri: RichiestaParereDTO[] = this.pratica.richiestePareri.filter(
          (x: RichiestaParereDTO)=>
            x.idGruppoDestinatarioParere !== environment.groups.idGruppoPoliziaLocale &&
            x.parere != null
        );
        // ottengo l'ultimo parere per ogni destinatario
        if (pareri && pareri.length > 0) {
          // Ordinamento per data discendente per selezionare i pareri più recenti
          pareri = pareri.sort((e1:RichiestaParereDTO, e2:RichiestaParereDTO) => {
            let inizio = DateTime.fromISO(new Date(e1.dataInserimento).toISOString()).startOf('day');
            let fine = DateTime.fromISO(new Date(e2.dataInserimento).toISOString()).startOf('day');
            return fine.diff(inizio, 'days').values.days;
          });
          /* Estrazione ultimo parere
          pareri.forEach((elem: RichiestaParereDTO) => {
            if (pareriMap.get(elem.idGruppoDestinatarioParere) == null) {
              pareriMap.set(elem.idGruppoDestinatarioParere, elem);
            }
          });
          ret = Array.from(pareriMap.values());*/
          ret = pareri;
          this.cacheParereAltri = ret;
        }
      }
    }

    return ret;
  }
}
