import { Component, Input, OnInit } from '@angular/core';
import { SpinnerDialogService } from '@core-components/layout/spinner-dialog/services/spinner-dialog.service';
import { DettaglioPraticaComponent } from '@features/dettaglio-pratica/dettaglio-pratica.component';
import { DettaglioPraticaDialogConfig } from '@features/dettaglio-pratica/model/dettaglio-pratica-dialog-config';
import { GruppoDTO } from '@models/dto/gruppo-dto';
import { PraticaDto } from '@models/dto/pratica-dto';
import { ProtocolloDTO } from '@models/dto/protocollo-dto';
import { RichiestaIntegrazioneDTO } from '@models/dto/richiesta-integrazione-dto';
import { RichiestaParereDTO } from '@models/dto/richiesta-parere-dto';
import { UtenteDTO } from '@models/utente-dto';
import { PageRequest } from '@services/base/page-request';
import { MessageService } from '@services/message.service';
import { PraticaService } from '@services/pratica.service';
import { TipologicheService } from '@services/tipologiche.service';
import { UtilityService } from '@services/utility.service';
import { Action } from '@shared-components/azioni-pratica/models/action';
import { ColumnSchema } from '@shared-components/table-prime-ng/models/column-schema';
import { TableEvent } from '@shared-components/table-prime-ng/models/table-event';
import { environment } from 'environments/environment';
import { SortEvent } from 'primeng/api';
import { DialogService } from 'primeng/dynamicdialog';
import { forkJoin } from 'rxjs';
import {parallel} from "@angular/cdk/testing";



@Component({
  selector: 'app-storico-pratica',
  templateUrl: './storico-pratica.component.html',
  styleUrls: ['./storico-pratica.component.css']
})
export class StoricoPraticaComponent implements OnInit {
  @Input() pratica: PraticaDto = null;
  @Input() isPraticaOrigine: boolean = false;

  statiRettificaPratica = new Set([
    environment.statiPratica.attesaPagamento,
    environment.statiPratica.prontaRilascio,
    environment.statiPratica.concessioneValida
  ]);

  tipoGruppi: GruppoDTO[] = [];
  dataSource: PraticaDto[] = [];
  initSortColumn = 'dataModifica';
  directionSortColumn = "-1"; //1=asc  0=rand   -1=desc

  columnSchema: ColumnSchema[] = [
    {
      field: "$protocollo$",
      header: "Num. Protocollo Operazione",
      type: "text",
      pipe: "html",
      show: (value, data) => {
        return this.getProtocollo(data)
      }
    },
    {
      field: "stato",
      header: "Stato",
      type: "text",
      pipe: 'html',
      show: (value, data) => {
        return this.getStatoPratica(data);
      }
    },
    {
      field: "descrizioneOperazione",
      header: "Descrizione operazione",
      type:"text",
      show: (value, data) => {
        return this.getInfoPassaggioStato(data);
      }
    },
    {
      field: "operazioneEseguitaDa",
      header: "Operazione eseguita da",
      type:"text",
      show: (value, data) => {
        return this.getUtenteOperazione(data);
      }
    },
    {
      field: "sortDate",
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
    }
  ];
  pageRequest: PageRequest = null;

  constructor(
    private praticaService: PraticaService,
    private messageService: MessageService,
    private spinnerService: SpinnerDialogService,
    private dialogService: DialogService,
    private utilityService: UtilityService,
    private tipologicheService: TipologicheService
  ) {}

  ngOnInit(): void {
    this.loadStorico();
  }

  private loadStorico() {
    this.spinnerService.showSpinner(true);
    forkJoin([
      this.praticaService.storicoPratica(this.pratica.id, this.pageRequest),
      this.tipologicheService.getGruppi()
    ]).subscribe(
      next => {
        this.dataSource = next[0].content.map(el => Object.setPrototypeOf(el, PraticaDto.prototype));
        this.tipoGruppi = next[1];
        this.spinnerService.showSpinner(false);
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage("Caricamento storico pratica", err);
      }
    );
  }

  onTableEvent(tableEvent: TableEvent) {
    this[tableEvent.actionKey](tableEvent.data);
  }

  dettaglioPratica(element: PraticaDto) {
    let data: DettaglioPraticaDialogConfig = {
      pratica: element,
      isPraticaOrigine: this.isPraticaOrigine,
      showStorico: false
    };

    this.dialogService.open(DettaglioPraticaComponent,  this.utilityService.configDynamicDialogFullScreen(data, "Pratica cittadino"));
  }

  private getStatoPratica(praticaRow: PraticaDto): string {
    if (praticaRow.infoPassaggioStato != null && praticaRow.infoPassaggioStato === 'L\'istante ha modificato le date di occupazione') {
      return praticaRow.statoPratica.descrizione;
    }
    if (praticaRow.statoPratica.id == environment.statiPratica.verificaFormale &&
      praticaRow.datiRichiesta.tipoOperazioneVerificaOccupazione != null &&
      praticaRow.tipoProcesso.id == environment.processes.concessioneTemporanea &&
      praticaRow.richiesteIntegrazioni.length == 0) {
        return `${praticaRow.statoPratica.descrizione}<br>(Verifica occupazione)`;
      }

    if (praticaRow.statoPratica.id == environment.statiPratica.richiestaPareri &&
      praticaRow.datiRichiesta.tipoOperazioneVerificaOccupazione != null &&
      praticaRow.tipoProcesso.id == environment.processes.concessioneTemporanea &&
      praticaRow.richiestePareri.length == 0
    ) {
        return `${praticaRow.statoPratica.descrizione}<br>(Verifica occupazione)`;
      }

    if(this.statiRettificaPratica.has(praticaRow.statoPratica.id) && praticaRow.protocolli.length > 0){
      for (let i = praticaRow.protocolli.length -1; i >= 0; i--) {
        if(praticaRow.protocolli[i].tipoOperazione == "RETTIFICA_ERRORI_MATERIALI" && praticaRow.statoPratica.id == praticaRow.protocolli[i].idStatoPratica){
          return `${praticaRow.statoPratica.descrizione}<br>(Rettifica)`;
        }
      }
    }

    return praticaRow.statoPratica.descrizione;
  }

  private getProtocollo(praticaRow: PraticaDto): string {
    let ret = "";
    let showProtocolloPratica = true;
    if (praticaRow.infoPassaggioStato != null && praticaRow.infoPassaggioStato === 'L\'istante ha modificato le date di occupazione') {
      return this.utilityService.getLastProtocolByDate(praticaRow.protocolli);
    }
    if (praticaRow.statoPratica.id == environment.statiPratica.richiestaPareri &&
      praticaRow.richiestePareri && praticaRow.richiestePareri.length > 0) {
      praticaRow.richiestePareri.forEach((elem: RichiestaParereDTO) => {
        if (!elem.flagInseritaRisposta) {
          ret += `Richiesta a ${this.tipoGruppi.find((x: GruppoDTO)=>x.id === elem.idGruppoDestinatarioParere).descrizione}: ${elem.codiceProtocollo}<br>`;
        } else {
          showProtocolloPratica = false;
          ret += `Parere ${this.tipoGruppi.find((x: GruppoDTO)=>x.id === elem.idGruppoDestinatarioParere).descrizione}: ${elem.parere.codiceProtocollo}<br>`;
        }
      });
    }

    if (
        (
          praticaRow.statoPratica.id == environment.statiPratica.necessariaIntegrazione ||
          praticaRow.statoPratica.id == environment.statiPratica.verificaFormale
        ) &&
        praticaRow.richiesteIntegrazioni &&
        praticaRow.richiesteIntegrazioni.length > 0
      ) {
      praticaRow.richiesteIntegrazioni.forEach((elem: RichiestaIntegrazioneDTO) => {
        if (!elem.integrazione) {
          ret += `Richiesta integrazione: ${elem.codiceProtocollo}<br>`;
        } else {
          showProtocolloPratica = false;
          ret += `Integrazione: ${elem.integrazione.codiceProtocollo}<br>`;
        }
      });
    }

    if(praticaRow.statoPratica.id == environment.statiPratica.rettificaDate){
      return `${praticaRow.richiesteIntegrazioni[0].codiceProtocollo}<br>`;
    }

    if(this.statiRettificaPratica.has(praticaRow.statoPratica.id) && praticaRow.protocolli.length > 0){
      for (let i = praticaRow.protocolli.length -1; i >= 0; i--) {
        if(praticaRow.protocolli[i].tipoOperazione == "RETTIFICA_ERRORI_MATERIALI" && praticaRow.statoPratica.id == praticaRow.protocolli[i].idStatoPratica){
          return `${praticaRow.protocolli[i].codiceProtocollo }<br>`;
        }
      }
    }

    if (praticaRow.protocolli && showProtocolloPratica) {
      let protocollo = praticaRow.protocolli.find((elem: ProtocolloDTO)=>elem.idStatoPratica === praticaRow.statoPratica.id);
      if (protocollo) {
        ret = `${protocollo.codiceProtocollo}<br>`;
      }
    }

    if (praticaRow.statoPratica.id === environment.statiPratica.archiviata) {
      if (praticaRow.richiestePareri != null && praticaRow.richiestePareri.length > 0) {
        for (const richiestaParere of praticaRow.richiestePareri) {
          if (
            [
              environment.statiPratica.archiviata,
              environment.statiPratica.annullata,
              environment.statiPratica.revocata
            ].includes(
              richiestaParere.idStatoPratica
            ) && richiestaParere.parere != null)
          {
            return richiestaParere.parere.codiceProtocollo;
          }
        }
      }
    }
    return (ret.length === 0) ? '--' : ret;
  }

  private getUtenteOperazione(praticaRow: PraticaDto): string {
    let user: UtenteDTO;
    if(praticaRow.nomeCittadinoEgov && praticaRow.cognomeCittadinoEgov && praticaRow.cfCittadinoEgov){
      return `${praticaRow.nomeCittadinoEgov} ${praticaRow.cognomeCittadinoEgov} - ${praticaRow.cfCittadinoEgov}`;
    } else {
      user = praticaRow.utenteModifica;
      if (user === null) {
        user = praticaRow.utenteCreazione;
      }

      return `${user.nome} ${user.cognome}`;
    }
  }

  private getInfoPassaggioStato(praticaRow: PraticaDto): string {
    return praticaRow && praticaRow.infoPassaggioStato ? praticaRow.infoPassaggioStato : '';
  }

  private getDataOperazione(praticaRow: PraticaDto): Date {
    let ret: Date = null;
    if (praticaRow) {
      ret = praticaRow.dataModifica;
      if (ret === null) {
        ret = praticaRow.dataCreazione;
      }
    }

    return ret;
  }
}
