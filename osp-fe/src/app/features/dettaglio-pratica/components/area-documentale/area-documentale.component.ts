import { Component, Input, OnInit } from '@angular/core';
import { PraticaDto } from '@models/dto/pratica-dto';
import { DestinazioneAllegato } from '@shared-components/upload-file/enums/destinazione-allegato.enum';
import { Mode } from '@shared-components/upload-file/enums/mode.enum';

@Component({
  selector: 'app-area-documentale',
  templateUrl: './area-documentale.component.html',
  styleUrls: ['./area-documentale.component.css']
})
export class AreaDocumentaleComponent implements OnInit {
  @Input() pratica: PraticaDto = null;

  mode = Mode.MULTIPLE;
  destinazioneAllegato = DestinazioneAllegato.PRATICA;
  
  constructor() { }

  ngOnInit(): void {
  }

}
