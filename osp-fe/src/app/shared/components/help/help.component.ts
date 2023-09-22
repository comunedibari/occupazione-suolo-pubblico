import { Component, Inject, OnInit, Input, OnDestroy, Renderer2 } from '@angular/core';
import { TipologicaDTO } from '@models/dto/tipologica-dto';
import { TipologicheService } from '@services/tipologiche.service';
import { TableEvent } from '@shared-components/table-prime-ng/models/table-event';
import { DynamicDialogConfig, DialogService, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-help',
  templateUrl: './help.component.html',
  styleUrls: ['./help.component.css']
})
export class HelpComponent implements OnInit {

  constructor(
    private dialogRef: DynamicDialogRef,
    private renderer: Renderer2,
    public dialogService: DialogService,
    private tipologicheService: TipologicheService,
    @Inject(DynamicDialogConfig) data: any
  ) {
    this.renderer.addClass(document.body, 'modal-open');
  }

  dataSource: Observable<TipologicaDTO[]>;


  initSortColumn = 'descrizione';
  directionSortColumn = "1"; //1=asc  0=rand   -1=desc 
  rowsPerPageOptions = [20];
  titleTable: string = 'Spiegazione stati pratica';
  exportName = 'Spiegazione stati pratica'; 
  globalFilters: any[] = [
    {value:'descrizione',label:'Stato pratica'},
  ];

  columnSchema = [
    {
      field: "descrizione",
      header: "Stato pratica",
      type: "text",
      inactive: true
    },
    {
      field: "descrizioneEstesa",
      header: "Spiegazione stato",
      type: "text"
    }
  ];

  actions = [];

  onTableEvent(tableEvent: TableEvent) {
    this[tableEvent.actionKey](tableEvent.data);
  }

  ngOnInit(): void {
    this.dataSource = this.tipologicheService.getStatiPratiche();
  }

  ngOnDestroy() {
    this.renderer.removeClass(document.body, 'modal-open');
  }

  closeDialog(event?) {
    this.dialogRef.close(event);
  }

}
