import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { PraticaDto } from '@models/dto/pratica-dto';
import { UtilityService } from '@services/utility.service';

@Component({
  selector: 'app-informazioni-aggiuntive',
  templateUrl: './informazioni-aggiuntive.component.html',
  styleUrls: ['./informazioni-aggiuntive.component.css']
})
export class InformazioniAggiuntiveComponent implements OnInit {
  @Input() pratica: PraticaDto = null;
  
  informazioniAggiuntiveForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private utilityService: UtilityService
  ) { }

  ngOnInit(): void {
    this.buildForm();
  }

  private buildForm() {
    this.informazioniAggiuntiveForm = this.fb.group({
      numeroProtocollo: [{value: this.getProtocollo(), disabled: true }],
      statoPratica: [{value: this.pratica.statoPratica.descrizione, disabled: true}],
      dataInserimento: [{value: new Date(this.pratica.dataInserimento), disabled: true}],
      dataScadenzaProcedimento: [{value: new Date(this.pratica.dataScadenzaPratica), disabled: true}],
      proprietarioPratica: [{value: this.getProprietarioPratica(), disabled: true}],
      idDetermina: [{value: this.pratica.codiceDetermina, disabled: true}],
      dataScadenzaPagamento: [{value: this.getDataScadenzaPagamento() == null ? null : new Date(this.getDataScadenzaPagamento()), disabled: true}],
      flagEsenzionePagamentoCUP: [{value: this.pratica.flagEsenzionePagamentoCUP, disabled: true}],
      motivazioneEsenzionePagamentoCup: [{value: this.pratica.motivazioneEsenzionePagamentoCup, disabled: true}],
    });
  }

  private getProtocollo(): string {
    return this.utilityService.getProtocolloPratica(this.pratica);
  }

  private getProprietarioPratica(): string {
    let ret = "";

    if (this.pratica.utentePresaInCarico) {
      ret = `${this.pratica.utentePresaInCarico.nome} ${this.pratica.utentePresaInCarico.cognome}`
    }
    return ret;
  }

  private getDataScadenzaPagamento(): Date|null {
    let ret: Date = null;
    if (this.pratica.dataScadenzaPagamento) {
      ret = this.pratica.dataScadenzaPagamento;
    }
    return ret;
  }
}
