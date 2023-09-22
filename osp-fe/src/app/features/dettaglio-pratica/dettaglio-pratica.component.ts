import { Component, Inject, Input, OnDestroy, OnInit, Renderer2 } from '@angular/core';
import { PraticaDto } from '@models/dto/pratica-dto';
import { DynamicDialogConfig } from 'primeng/dynamicdialog';
import { DettaglioPraticaDialogConfig } from './model/dettaglio-pratica-dialog-config';

@Component({
  selector: 'app-dettaglio-pratica',
  templateUrl: './dettaglio-pratica.component.html',
  styleUrls: ['./dettaglio-pratica.component.scss']
})
export class DettaglioPraticaComponent implements OnInit, OnDestroy {
  @Input() pratica: PraticaDto = null;
  @Input() isPraticaOrigine: boolean = false;
  @Input() showStorico: boolean = true;
  constructor(
    private renderer: Renderer2,
    @Inject(DynamicDialogConfig) config: DynamicDialogConfig
  ) {
    this.renderer.addClass(document.body, 'modal-open');
    if (config.data) {
      const myConfig: DettaglioPraticaDialogConfig = config.data;
      this.pratica = myConfig.pratica;
      this.isPraticaOrigine = myConfig.isPraticaOrigine;
      this.showStorico = myConfig.showStorico;
    }
/*
    if (this.showStorico) {
      config.header += ' - dettaglio storico';
    }*/
    if (this.isPraticaOrigine) {
      config.header += ' - dettaglio pratica d\'origine';
    }
  }

  ngOnInit(): void {

  }

  ngOnDestroy(): void {
    this.renderer.removeClass(document.body, 'modal-open');
  }

}
