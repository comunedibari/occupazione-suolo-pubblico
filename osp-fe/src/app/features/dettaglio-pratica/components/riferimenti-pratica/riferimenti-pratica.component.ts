import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { DettaglioPraticaComponent } from '@features/dettaglio-pratica/dettaglio-pratica.component';
import { DettaglioPraticaDialogConfig } from '@features/dettaglio-pratica/model/dettaglio-pratica-dialog-config';
import { PraticaDto } from '@models/dto/pratica-dto';
import { MessageService } from '@services/message.service';
import { PraticaService } from '@services/pratica.service';
import { UtilityService } from '@services/utility.service';
import { DialogService } from 'primeng/dynamicdialog';

@Component({
  selector: 'app-riferimenti-pratica',
  templateUrl: './riferimenti-pratica.component.html',
  styleUrls: ['./riferimenti-pratica.component.css']
})
export class RiferimentiPraticaComponent implements OnInit {
  @Input() pratica: PraticaDto = null;

  praticaOriginaria: PraticaDto = null;

  riferimentiForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private praticaService: PraticaService,
    private messageService: MessageService,
    private dialogService: DialogService,
    private utilityService: UtilityService
  ) {
    this.buildForm();
  }

  ngOnInit(): void {
    if (this.showRiferimentiPratica()) {
      let obs = this.praticaService.getPratica(this.pratica.idPraticaOriginaria);
      if (this.pratica.idProrogaPrecedente != null) {
        obs = this.praticaService.getPratica(this.pratica.idProrogaPrecedente);
      }
      obs.subscribe(
        (data: PraticaDto) => {
          this.praticaOriginaria = data;
          this.initForm(this.praticaOriginaria);
        },
        err => {
          this.messageService.showErrorMessage("Reperimento pratica", err);
      });

    }
  }

  private buildForm() {
    this.riferimentiForm = this.fb.group({
      numeroProtocollo: [{value: null, disable: true}],
      idDetermina: [{value: null, disabled: true }],
    });
  }

  private initForm(praticaOriginaria: PraticaDto) {
    this.riferimentiForm.get('numeroProtocollo').setValue(this.utilityService.getProtocolloPratica(praticaOriginaria));
    this.riferimentiForm.get('idDetermina').setValue(praticaOriginaria.codiceDetermina);
  }

  public showRiferimentiPratica(): boolean {
    let ret: boolean = this.pratica.idPraticaOriginaria !== null;
    return ret;
  }

  public onDettaglioPraticaOrigineClicked(): void {
      let config: DettaglioPraticaDialogConfig = {
        pratica: this.praticaOriginaria,
        isPraticaOrigine: true,
        showStorico: true
      };
      this.dialogService.open(DettaglioPraticaComponent,  this.utilityService.configDynamicDialogFullScreen(config, "Pratica cittadino"));
  }

}
