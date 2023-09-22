import { DettaglioPraticaComponent } from "@features/dettaglio-pratica/dettaglio-pratica.component";
import { DettaglioPraticaDialogConfig } from "@features/dettaglio-pratica/model/dettaglio-pratica-dialog-config";
import { PraticaDto } from "@models/dto/pratica-dto";
import { UtilityService } from "@services/utility.service";
import { TableEvent } from "@shared-components/table-prime-ng/models/table-event";
import { DialogService } from "primeng/dynamicdialog";

export class BasePraticaAction {

    constructor(
        protected dialogService: DialogService,
        protected utilityService: UtilityService
    ){}

    protected getProtocollo(el: PraticaDto): string {
        return this.utilityService.getProtocolloPratica(el);
    }

    public onTableEvent(tableEvent: TableEvent) {
        this[tableEvent.actionKey](tableEvent.data);
    }

    protected dettaglioPratica(element: PraticaDto) {
        let config: DettaglioPraticaDialogConfig = {
          pratica: element,
          isPraticaOrigine: false,
          showStorico: true
        }
    
        this.dialogService.open(DettaglioPraticaComponent,  this.utilityService.configDynamicDialogFullScreen(config, "Pratica cittadino"));
    }
}
