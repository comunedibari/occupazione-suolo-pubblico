import { Component, OnInit, Input, Output, EventEmitter, Inject } from '@angular/core';
import { SpinnerDialogService } from '@core-components/layout/spinner-dialog/services/spinner-dialog.service';
import { GeoPointDTO } from '@models/dto/geo-point-dto';
import { PraticaDto } from '@models/dto/pratica-dto';
import { AuthService } from '@services/auth/auth.service';
import { MessageService } from '@services/message.service';
import { PraticaService } from '@services/pratica.service';
import { environment } from 'environments/environment';
import { Router } from '@angular/router';

import { DynamicDialogRef, DynamicDialogConfig } from 'primeng/dynamicdialog';
import { UtilityService } from '@services/utility.service';
import { GeoMultiPointDTO } from '@models/dto/geo-multi-point-dto';
declare let L;
// declare let window;
declare var HeatmapOverlay: any;

interface MarkerModel {
  label: string;
  indirizzo: string;
  latitudine: number;
  longitudine: number;
  id: string;
}

interface polylineModel {
  label: string;
  indirizzo: string;
  points: GeoPointDTO[];
  id: string;
}


@Component({
  selector: 'app-mappa',
  templateUrl: './mappa.component.html',
  styleUrls: ['./mappa.component.css']
})
export class MappaComponent implements OnInit {

  @Input() elements: PraticaDto[];
  @Input() fullScreen: boolean = false;
  @Input() enableHeatmap: boolean = false;
  @Input() showDetailButton: boolean = true;
  @Output() dettaglioFeature: EventEmitter<any> = new EventEmitter<any>();
  @Output() skipCheckOccupazioneChange: EventEmitter<any> = new EventEmitter<any>();

  heatmap: any;
  visualizzaMappaClicked: boolean = false;

  constructor(
    private dialogRef: DynamicDialogRef,
    private praticaService: PraticaService,
    public authService: AuthService,
    private spinnerService: SpinnerDialogService,
    private messageService: MessageService,
    private utilityService: UtilityService,
    private router: Router,

    @Inject(DynamicDialogConfig) data: any) {
    if (data && data.data && data.data.elements) {
      this.elements = data.data.elements;
      this.fullScreen = data.data.fullScreen;
      this.enableHeatmap = data.data.enableHeatmap;
      this.visualizzaMappaClicked = data.data.visualizzaMappaClicked;
      this.skipCheckOccupazioneChange = data.data.skipCheckOccupazioneChange;

      if (data.data.showDetailButton != null && data.data.showDetailButton != undefined) {
        this.showDetailButton = data.data.showDetailButton;
      }
    }
  }

  thereAreCoordinates(element) {
    return this.getCoordinates(element) ? true : false;
  }

  private getCoordinates(element: PraticaDto): GeoMultiPointDTO {
    let ret: GeoMultiPointDTO = {
      points: []
    };
    if (element.datiRichiesta.coordUbicazioneDefinitiva) {
      element.datiRichiesta.coordUbicazioneDefinitiva.points.forEach(element => {
        ret.points.push(element);
      });
    }
    else if (element.datiRichiesta.coordUbicazioneTemporanea) {
      ret.points.push(element.datiRichiesta.coordUbicazioneTemporanea.points[0]);
    }
    return ret;
  }

  currentPage: string = '';
  errorDialog: boolean = false;
  errorMessage: string = "";
  skipCheckOccupazione: boolean;
  coordUbicazioneDefinitiva: any;
  ngOnInit() {
    const __this = this;
    let drawLatLng: any = '';
    let drawType: string = '';
    let keys = this.elements ? Object.keys(this.elements) : [];
    let thereAreCoordinates: boolean = keys.length == 0 ? false : (keys.length == 1 ? this.thereAreCoordinates(this.elements[0]) : true);

    let polylineCoordinates: polylineModel[] = [];
    let coordinates: MarkerModel[] = [];

    keys.forEach(x => {
      if (this.getCoordinates(this.elements[x]).points.length == 1) {
        coordinates.push({
          label: this.elements[x].firmatario.nome + ' ' + this.elements[x].firmatario.cognome,
          indirizzo: this.elements[x].datiRichiesta.ubicazioneOccupazione,
          latitudine: this.getCoordinates(this.elements[x])?.points[0].lat || null,
          longitudine: this.getCoordinates(this.elements[x])?.points[0].lon || null,
          id: this.elements[x].id
        })
      } else {
        polylineCoordinates.push({
          label: this.elements[x].firmatario.nome + ' ' + this.elements[x].firmatario.cognome,
          indirizzo: this.elements[x].datiRichiesta.ubicazioneOccupazione,
          points: this.elements[x].datiRichiesta.coordUbicazioneDefinitiva.points,
          id: this.elements[x].id
        })
      }
    });

    coordinates = coordinates.filter(x => x.latitudine != null && x.longitudine != null);

    let verticalShift = !this.fullScreen && keys.length == 1 ? 0.014 : 0;

    let startPoint: number[] = [(41.1255301 - 0.014), 16.8618679];
    if (thereAreCoordinates) {
      if (polylineCoordinates.length > 0) {
        startPoint = [(polylineCoordinates[0].points[0].lat - verticalShift), polylineCoordinates[0].points[0].lon];
      } else {
        startPoint = [(coordinates[0].latitudine - verticalShift), coordinates[0].longitudine];
      }
    }

    var tiles = L.tileLayer.colorFilter('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
      filter: ['grayscale:100%']
    });

    var cfg = {
      "radius": .02,
      "maxOpacity": .8,
      "scaleRadius": true,
      "useLocalExtrema": true,
      latField: 'lat',
      lngField: 'lng',
      valueField: 'count'
    };

    this.heatmap = new HeatmapOverlay(cfg);

    var map = L.map('map', {
      fullscreenControl: true,
      fullscreenControlOptions: {
        position: 'topright',
        title: "Attiva Fullscreen",
        titleCancel: "Disattiva Fullscreen"
      },
      editable: !thereAreCoordinates && !this.fullScreen,
      center: startPoint,
      zoom: keys.length > 1 ? 11 : 14,
      layers: [tiles]
    });

    let customIcon = L.icon({
      iconUrl: '/assets/marker-icon.png',
      shadowUrl: '/assets/marker-shadow.png',
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34],
      tooltipAnchor: [16, -28],
      shadowSize: [41, 41]
    });

    //Visualizza le pratiche all'interno della Mappa
    if (thereAreCoordinates) {
      if (polylineCoordinates) {
        polylineCoordinates.forEach(praticaPolyline => {
          var polygon = L.polyline(praticaPolyline.points);

          if (this.fullScreen && this.enableHeatmap && this.showDetailButton) {
            polygon.bindPopup(this.getPolylinePopup(praticaPolyline))
              .on("popupopen", (a) => {
                a.target.getPopup().getElement()
                  .querySelector(".info")
                  .addEventListener("click", e => {
                    __this.openDetailsModal(praticaPolyline);
                  });
              });
          } else {
            polygon.bindPopup(this.getPolylinePopup(praticaPolyline));
          }
          polygon.addTo(map);
        });
      }

      if (coordinates) {
        var markers = L.markerClusterGroup();
        coordinates.forEach(pratica => {
          var marker = L.marker([pratica.latitudine, pratica.longitudine], { icon: customIcon });

          if (this.fullScreen && this.enableHeatmap && this.showDetailButton) {
            marker.bindPopup(this.getContentPopup(pratica))
              .on("popupopen", (a) => {
                a.target.getPopup().getElement()
                  .querySelector(".info")
                  .addEventListener("click", e => {
                    __this.openDetailsModal(pratica);
                  });
              });
          }
          else {
            marker.bindPopup(this.getContentPopup(pratica));
          }

          markers.addLayer(marker);
        });

        map.addLayer(markers);
      }


      if (keys.length > 1 && this.enableHeatmap) {
        this.setHeatmapData(coordinates, polylineCoordinates);
        map.on('zoomend', function (data) {
          switch (map._zoom) {
            case 8:
            case 9:
            case 10: {
              __this.heatmap.cfg.radius = .02;
              break;
            }
            case 11: {
              __this.heatmap.cfg.radius = .01;
              break;
            }
            case 12: {
              __this.heatmap.cfg.radius = .005;
              break;
            }
            case 13: {
              __this.heatmap.cfg.radius = .003;
              break;
            }
            case 14: {
              __this.heatmap.cfg.radius = .002;
              break;
            }
            case 15: {
              __this.heatmap.cfg.radius = .001;
              break;
            }
            case 16: {
              __this.heatmap.cfg.radius = .0007;
              break;
            }
            case 17: {
              __this.heatmap.cfg.radius = .0005;
              break;
            }
            case 18:
            case 19: {
              __this.heatmap.cfg.radius = .0003;
              break;
            }
            default: {
              __this.heatmap.cfg.radius = .06;
              break;
            }
          }
        });

        L.EditControl = L.Control.extend({
          options: {
            position: 'topright',
            callback: null,
            kind: 'HeatMap',
            html: '<a class="heatmap-icon"></a>'
          },

          onAdd: function (map) {
            var container = L.DomUtil.create('div', 'leaflet-control leaflet-bar'),
              link = L.DomUtil.create('a', '', container);

            link.href = '#';
            link.title = 'Abilita/Disabilita ' + this.options.kind;
            link.innerHTML = this.options.html;
            L.DomEvent.on(link, 'click', L.DomEvent.stop)
              .on(link, 'click', function () {
                if (map.hasLayer(__this.heatmap))
                  map.removeLayer(__this.heatmap);
                else
                  map.addLayer(__this.heatmap);
              }, this);

            return container;
          }
        });

        map.addControl(new L.EditControl());
      }

      //Show edit controls
      if (this.elements[0].datiRichiesta.coordUbicazioneDefinitiva == null && !this.visualizzaMappaClicked) {
        var drawnItems = new L.FeatureGroup();
        map.addLayer(drawnItems);

        var drawControl = new L.Control.Draw({
          draw: {
            position: 'topleft',
            polygon: true,
            marker: {
              icon: customIcon,
            },
            polyline: true,
            rectangle: false,
            circle: false,
            circlemarker: false
          },
          edit: {
            featureGroup: drawnItems
          }
        });

        //settaggio traduzioni bottoni
        L.drawLocal.draw.toolbar.buttons.marker = "Inserisci segnale indicatore";
        L.drawLocal.draw.toolbar.actions.text = "Annulla";
        L.drawLocal.edit.toolbar.actions.save.title = "Salva";
        L.drawLocal.edit.toolbar.actions.save.text = "Salva";
        L.drawLocal.edit.toolbar.actions.cancel.title = "Annulla";
        L.drawLocal.edit.toolbar.actions.cancel.text = "Annulla";
        L.drawLocal.edit.toolbar.actions.clearAll.title = "Elimina tutto";
        L.drawLocal.edit.toolbar.actions.clearAll.text = "Elimina tutto";
        L.drawLocal.edit.toolbar.buttons.edit = "Modifica";
        L.drawLocal.edit.toolbar.buttons.editDisabled = "Nessun marker da modificare";
        L.drawLocal.edit.toolbar.buttons.remove = "Elimina";
        L.drawLocal.edit.toolbar.buttons.removeDisabled = "Nessun marker da eliminare";

        map.addControl(drawControl);

        map.on('draw:created', function (e) {
          map.eachLayer(function (layerMap) {
            drawType = __this.getShapeType(layerMap);
            if (drawType)
              map.removeLayer(layerMap);
          });
          var layer = e.layer;
          drawType = __this.getShapeType(layer);

          if (drawType === 'marker') {
            drawLatLng = layer.getLatLng();
            let msg = __this.getContentPopupNewMarker(drawLatLng);
            layer.bindPopup(msg);
          }

          if (drawType === 'polygon') {
            drawLatLng = layer.getLatLngs()[0];
          }

          if (drawType === 'polyline') {
            drawLatLng = layer.getLatLngs();
          }

          drawnItems.addLayer(layer);
        });

        map.on('draw:editstop', function (e) {
          map.closePopup();
        });

        map.on('draw:edited', function (e) {
          var layers = e.layers;
          layers.eachLayer(function (layer) {
            var type = __this.getShapeType(layer);
            if (type === 'marker') {
              drawLatLng = layer.getLatLng();
              let msg = __this.getContentPopupNewMarker(drawLatLng);
              layer.bindPopup(msg);
            }
            if (drawType === 'polygon') {
              drawLatLng = layer.getLatLngs()[0];
            }
            if (drawType === 'polyline') {
              drawLatLng = layer.getLatLngs();
            }
          });
        });

        map.on('draw:deleted', function (e) {
          var layers = e.layers;
          layers.eachLayer(function (layer) {
            var type = __this.getShapeType(layer);
          });
        });

        L.EditControl = L.Control.extend({
          options: {
            position: 'topright',
            callback: null,
            kind: 'Salva',
            html: '<a class="pi pi-save"></a>'
          },

          onAdd: function (map) {
            var container = L.DomUtil.create('div', 'leaflet-control leaflet-bar'),
              link = L.DomUtil.create('a', '', container);

            link.href = '#';
            link.title = this.options.kind;
            link.innerHTML = this.options.html;
            L.DomEvent.on(link, 'click', L.DomEvent.stop)
              .on(link, 'click', function () {
                //Deep clone per evitare problemi con il delete nel foreach del cambio nome
                if (drawLatLng == '') {
                  __this.messageService.showMessage('warn', 'Attenzione', "Non è stata apportata alcuna modifica sull'ubicazione.");
                } else {
                  let drawClone = JSON.parse(JSON.stringify(drawLatLng));

                  if (drawType == 'polygon' || drawType == 'polyline') {
                    __this.coordUbicazioneDefinitiva = {
                      points: drawClone
                    }
                  } else {
                    __this.coordUbicazioneDefinitiva = {
                      points: [
                        drawClone
                      ]
                    }
                  }

                  //Cambio nome per compatibilità con il BE
                  __this.coordUbicazioneDefinitiva.points.forEach(e => {
                    e.lat = e.lat.toFixed(7);
                    e.lon = e.lng.toFixed(7);
                    delete e.lng;
                  })

                  //Il BE si aspetta di ricevere l'ultima coordinata uguale alla prima
                  if (drawType == 'polygon') {
                    __this.coordUbicazioneDefinitiva.points.push(__this.coordUbicazioneDefinitiva.points[0]);
                  }

                  __this.salvaCoordinate();

                  if (__this.router.url === '/gestione_richieste/ricerca_pratiche') {
                    __this.currentPage = 'Ricerca Pratiche'
                  } else {
                    __this.currentPage = 'Validazione pratiche'
                  }
                }
              }, this);
            return container;
          }
        });

        map.addControl(new L.EditControl());
      }
    }
  }

  getShapeType(layer): string {
    let shape = '';
    if (layer instanceof L.Circle) {
      shape = 'circle';
    }
    if (layer instanceof L.Marker) {
      shape = 'marker';
    }
    if (layer instanceof L.CircleMarker) {
      shape = 'circleMarker';
    }
    if ((layer instanceof L.Polyline) && !(layer instanceof L.Polygon)) {
      shape = 'polyline';
    }
    if ((layer instanceof L.Polygon) && !(layer instanceof L.Rectangle)) {
      shape = 'polygon';
    }
    if (layer instanceof L.Rectangle) {
      shape = 'rectangle';
    }
    return shape;
  };

  getContentPopup(el): string {
    let label = el.label.trim() ? el.label : 'N.D.';
    let id = el.id ? el.id : 'N.D.';
    let text = '<strong>' + label + ' - ID: ' + id + '</strong><br />' +
      '<strong>Indirizzo: </strong>' + el.indirizzo + '<br />' +
      '<strong>Latitudine: </strong>' + el.latitudine + ' - ' +
      '<strong>Longitudine: </strong>' + el.longitudine;
    if (this.fullScreen && this.enableHeatmap && this.showDetailButton)
      text += "<br/> <button class=\"info btn-custom-style btn-dettagli\">Dettagli</button>"
    return text;
  }

  getPolylinePopup(el: polylineModel): String {
    let label = el.label.trim() ? el.label : 'N.D.';
    let id = el.id ? el.id : 'N.D.';
    let locazionePratica = [];

    for (let i = 0; i < el.points.length; i++) {
      locazionePratica.push(
        `
          <strong>Latitudine: </strong> ${el.points[i].lat} - 
          <strong>Longitudine: </strong> ${el.points[i].lon}<br />
        `
      )
    }

    let text = '<strong>' + label + ' - ID: ' + id + '</strong><br />' +
      '<strong>Indirizzo: </strong>' + el.indirizzo + '<br />' + locazionePratica;
    if (this.fullScreen && this.enableHeatmap && this.showDetailButton)
      text += "<br/> <button class=\"info btn-custom-style btn-dettagli\">Dettagli</button>"
    return text;
  }

  getContentPopupNewMarker(latlng): string {
    let text = '<strong>Nuovo segnale indicatore</strong><br/>' +
      '<strong>Latitudine: </strong>' + latlng.lat.toFixed(6) + ' - ' +
      '<strong>Longitudine: </strong>' + latlng.lng.toFixed(6);
    return text;
  }

  setHeatmapData(coordinates, polylineCoordinates) {
    var heatmapData = {
      max: 0,
      data: []
    };

    let maxCount = 0;
    let tmpCoordinates = [];

    if(coordinates){
      tmpCoordinates = coordinates.map(occupazioneSuolo => {
        return { lat: Number(occupazioneSuolo.latitudine), lng: Number(occupazioneSuolo.longitudine) }
      });
    }

    if(polylineCoordinates){
      polylineCoordinates.forEach(occupazioneSuolo => {
        for (let i = 0; i < occupazioneSuolo.points.length; i++) {
          tmpCoordinates.push({
            lat: Number(occupazioneSuolo.points[i].lat), lng: Number(occupazioneSuolo.points[i].lon)
          })
        }
      });
    }

    coordinates = tmpCoordinates;

    while (coordinates.length > 0) {
      let currCoordinates = coordinates[0];
      let count = 0;
      let indexToDelete = [];

      for (let index = 0; index < coordinates.length; index++) {
        if (coordinates[index].lat == currCoordinates.lat && coordinates[index].lng == currCoordinates.lng) {
          count++;
          indexToDelete.push(index);
        }
      }

      maxCount = Math.max(maxCount, count);
      heatmapData.data.push({ lat: currCoordinates.lat, lng: currCoordinates.lng, count: count });
      indexToDelete.forEach(index => coordinates.splice(index, 1));
    }

    heatmapData.max = maxCount;
    this.heatmap.setData(heatmapData);
  }

  openDetailsModal(element) {
    this.dettaglioFeature.emit(element);
    this.closeDialog(element);
  }

  closeDialog(event?) {
    this.dialogRef.close(event);
  }

  inviaVerificaOccupazione() {
    this.spinnerService.showSpinner(true);
    this.praticaService.verificaOccupazione(this.elements[0].id,
      this.authService.getLoggedUser().userLogged.id,
      environment.validaPratica.verificaOccupazione,
      this.coordUbicazioneDefinitiva, this.skipCheckOccupazione).subscribe(
        data => {
          this.spinnerService.showSpinner(false);
          //In Ricerca Pratiche, per evitare che la verifica occupazione venga effettuata due volte sulla stessa pratica,
          //il valore viene settato temporaneamente su undefined dal momento che la action non scompare dinamicamente dopo
          //aver effettuato la prima verifica
          if (this.currentPage == 'Ricerca Pratiche') {
            this.elements[0].datiRichiesta.coordUbicazioneDefinitiva = undefined;
          }
          this.skipCheckOccupazione = false;
          this.closeDialog();
        },
        err => {
          this.skipCheckOccupazione = false;
          this.spinnerService.showSpinner(false);
          this.messageService.showErrorMessage('Errore verifica occupazione', err);
        });
  }

  salvaCoordinate() {
    this.praticaService.checkSovrapposizioneOccupazione(this.elements[0].id,
      this.authService.getLoggedUser().userLogged.id,
      environment.validaPratica.verificaOccupazione,
      this.coordUbicazioneDefinitiva).subscribe(
        (data: any) => {
          this.messageService.showMessage('success', 'Ubicazione pratica', 'Controllo sovrapposizione eseguito correttamente.');
          if (this.currentPage == 'Ricerca Pratiche') {
            if (this.elements[0].datiRichiesta.coordUbicazioneDefinitiva === undefined) {
              this.messageService.showMessage('error', 'Ubicazione pratica', 'Operazione di verifica ubicazione già effettuata.');
            } else {
              this.inviaVerificaOccupazione();
              this.closeDialog();
            }
          } else {
            this.utilityService.drawCoordinates = this.coordUbicazioneDefinitiva;
          }
        },
        err => {
          if (err.error.code == 'E19') {
            this.errorDialog = true;
            this.errorMessage = this.utilityService.accapoMessage(err.error.message.slice(0, 300)) + "<br> Vuoi proseguire comunque confermando la verifica di occupazione?";
            this.skipCheckOccupazione = true;
            if(this.skipCheckOccupazioneChange) {
              this.skipCheckOccupazioneChange.emit(true);
            }
          } else {
            this.messageService.showErrorMessage('Check pratica', err);
          }
        }
      );
  }

  inviaCoordinate() {
    this.utilityService.drawCoordinates = this.coordUbicazioneDefinitiva;
  }
}
