import { Component, EventEmitter, Input, OnChanges, OnInit, Output, QueryList, ViewChild, ViewChildren } from '@angular/core';
import { FormatDatePipe } from '@pipes/format-date.pipe';
import { ColumnFilter, Table } from 'primeng/table';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { ActionColumnSchema } from './models/action-column-schema';
import { ColumnSchema } from './models/column-schema';
import { TableEvent } from './models/table-event';
import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';
import * as xlsx from 'xlsx';
import { saveAs } from 'file-saver';
import {FilterService, SortEvent} from 'primeng/api';

@Component({
  selector: 'app-table-prime-ng',
  templateUrl: './table-prime-ng.component.html',
  styleUrls: ['./table-prime-ng.component.css']
})
export class TablePrimeNGComponent implements OnInit, OnChanges {


  get isAutoLayout(): boolean {
    return window.innerWidth > 1036 ? true : false;
  }

  displayedColumns: Array<string>;
  _columnSchema: Array<ColumnSchema> = [];
  _actions: Array<ActionColumnSchema> = [];
  _dataSource: any[] = [];
  _bkpDataSource: any[] = [];

  @ViewChild('dt') table: Table;
  @ViewChildren(ColumnFilter) columnFilter: QueryList<ColumnFilter>;

  _selectedColumns: any[];

  selectedRows: any[];

  _globalFilters: any[];

  globalfilterUpdate: Subject<any> = new Subject();
  globalFilterValue = '';

  get thereIsActions(): boolean {
    return this._actions && this._actions.length ? true : false;
  }

  constructor(
    private datePipe: FormatDatePipe,
    private filterService: FilterService
  ) {
    this.globalfilterUpdate.pipe(debounceTime(600),
    distinctUntilChanged())
    .subscribe(value => {
      this.globalFilterValue = value;
      this.table.filterGlobal(this.globalFilterValue, 'contains');
    });
    this.filterService.register(
      'in',
      (value: any, filter: any[]): boolean => {
        if (filter == null || filter.length === 0) {
          return true;
        }
        if (value instanceof Array) {
          return filter.some(f => value.includes(f));
        } else {
          return filter.some(f => f.toString() === value.toString());
        }
      }
    );
  }

  private originalColumnSchema: Array<ColumnSchema> = [];
  @Input() set columnSchema(value: Array<ColumnSchema>) {
    this.originalColumnSchema = value;
    this._columnSchema = value.map(
      el => new ColumnSchema(
        el.field, el.header, el.type, el.pipe, el.show, el.inactive, el.customSortFunction, el.selector
      )
    );
  }

  @Input() set actions(value: Array<ActionColumnSchema>) {
    this._actions = value.map(el => new ActionColumnSchema(el.key, el.name, el.icon, el.tooltip, el.hidden, el.disabled));
  }

  @Input() titleTable = '';
  @Input() inserisciFeature = '';
  @Input() initSortColumn = '';
  @Input() directionSortColumn = '';
  @Input() dataSource: any[] = [];
  @Input() showToolbar = true;
  @Input() showCaption = true;
  @Input() exportName = '';
  @Input() globalFilters: any[] = [];
  @Input() defaultRows = 5;
  @Input() rowsPerPageOptions: number[] = [5, 10, 20];
  @Input() paginator = true;
  // if customSort is enabled use show function to
  // get field value (see _sortFunction)
  @Input() customSort: boolean;

  @Output() onEvent = new EventEmitter<TableEvent>();

  dropDownFilters: any = {};

  ngOnInit(): void {
    this._selectedColumns = this._columnSchema;
    this._globalFilters = this.globalFilters.map(el => el.value);
  }

  ngOnChanges() {
    const __this = this;
    this._dataSource = this.dataSource;

    if (this.dataSource && this.dataSource.length && this.columnFilter) {
      const dataFields = this.columnFilter.filter(x => x.type == 'date').map(y => y.field);
      dataFields.forEach(el => {
        let field = '';
        let dominio = '';

        if (el.indexOf('.') != -1){
          const splitted = el.split('.');
          dominio = splitted[0];
          field = splitted[1];
        }
        else {
          field = el;
        }

        this.dataSource.forEach(row => {
          if (dominio) {
            row[dominio][field] = row[dominio][field] ? new Date(row[dominio][field]) : null;
          }
          else {
            row[field] = row[field] ? new Date(row[field]) : null;
          }
        });
      });
      this._bkpDataSource = this._dataSource;

      this.columnFilter.forEach(cf => {
        cf.clearFilter = function() {
          this.initFieldFilterConstraint();
          this.dt._filter();

          const dropDownFields = __this._columnSchema.filter(x => x.type == 'dropdown').map(y => y.field);
          if (dropDownFields.indexOf(this.field) != -1) {
            __this.resetDropDown(this.field);
          }
        };
      });

      this.initializeDropDownFields();
    }
  }

  _sortFunction(event: SortEvent) {
    const schema = this._columnSchema.find(el => el.field === event.field);
    const show = schema && schema.show ? schema.show : null;

    event.data.sort((data1, data2) => {
      let value1 = show ? show(data1) : data1[event.field];
      let value2 = show ? show(data2) : data2[event.field];
      let result = null;

      if (schema && schema.pipe === 'date') {
        if (!!value1) {
          value1 = new Date(value1);
        }
        if (!!value2) {
          value2 = new Date(value2);
        }
      }

      if (schema && typeof schema.customSortFunction === 'function') {
        result = schema.customSortFunction(data1, data2);
      } else if (value1 == null && value2 != null) { result = -1; }
      else if (value1 != null && value2 == null) { result = 1; }
      else if (value1 == null && value2 == null) { result = 0; }
      else if (typeof value1 === 'string' && typeof value2 === 'string') { result = value1.localeCompare(value2); }
      else { result = value1 < value2 ? -1 : value1 > value2 ? 1 : 0; }

      return event.order * result;
    });
  }

  getTooltipGlobalFilters(): string {
    return `Ricerca per ${this.globalFilters.map(el => el.label).join(', ')} `;
  }

  initializeDropDownFields(){
    const dropDownFields = this._columnSchema.filter(x => x.type == 'dropdown');

    dropDownFields.forEach(x => {
      let field = '';
      let dominio = '';

      if (x.field.indexOf('.') > -1) {
        const splitted = x.field.split('.');
        dominio = splitted[0];
        field = splitted[1];
      }

      this.dropDownFilters[x.field] = [];
      this.dropDownFilters['selected_' + x.field] = [];

      const loadedFields = [];
      this._dataSource.forEach(row => {
        if (dominio) {
          if (row[dominio][field] instanceof Array) {
            const actualFields: Array<any> = row[dominio][field];
            actualFields.forEach(
              el => {
                if (loadedFields.indexOf(el) === -1) {
                  loadedFields.push(el);
                  this.dropDownFilters[x.field].push({label: el, value: el});
                }
              }
            );
          } else {
            if (loadedFields.indexOf(row[dominio][field]) === -1) {
              loadedFields.push(row[dominio][field]);
              this.dropDownFilters[x.field].push({label: x.show(row), value: row[dominio][field]});
            }
          }
        }
        else {
          if (row[x.field] instanceof Array) {
            const actualFields: Array<any> = row[x.field];
            actualFields.forEach(
              el => {
                if (loadedFields.indexOf(el) === -1) {
                  loadedFields.push(el);
                  this.dropDownFilters[x.field].push({label: el, value: el});
                }
              }
            );
          } else {
            if (loadedFields.indexOf(row[x.field]) === -1) {
              loadedFields.push(row[x.field]);
              this.dropDownFilters[x.field].push({label: x.show(row), value: row[x.field]});
            }
          }
        }
      });
    });
  }

  resetDropDown(field){
    this.dropDownFilters['selected_' + field] = [];
  }

  emitAction(event, data, actionKey) {
    this.onEvent.emit(new TableEvent(event, data, actionKey));
  }

  // gestisce l'input nel filtro globale
  onFilterGlobal(event: any) {
    // this.table.filterGlobal(event.target.value, 'contains');
    this.globalfilterUpdate.next(event.target.value);
  }

  @Input() get selectedColumns(): any[] {
    return this._selectedColumns;
  }

  set selectedColumns(val: any[]) {
    // ristora l'ordine originale
    this._selectedColumns = this._columnSchema.filter(col => val.includes(col));
  }

  // npm install jspdf jspdf-autotable
  exportPdf() {
    const doc = new jsPDF();
    const _schema: any = (this._selectedColumns.length === this._columnSchema.length) ? this._columnSchema : this._selectedColumns;
    const columns: any = _schema.map(col => ({ title: col.header, dataKey: `${col.field}${col.header}_formatted` }));
    const data_source = (this.table.filteredValue && this.table.filteredValue.length) ? this.table.filteredValue : this._dataSource;
    const rows: any[] = JSON.parse(JSON.stringify((this.selectedRows === undefined || this.selectedRows.length === 0) ? data_source : this.selectedRows));
    this._columnSchema.forEach(x => {
      const field = x.field;
      const header = x.header;
      rows.forEach(row => {
        if (x.pipe) {
          switch (x.pipe) {
            case 'date': {
              row[`${field}${header}_formatted`] = this.datePipe.transform(x.show(row), false);
              break;
            }
            case 'onlyDate': {
              row[`${field}${header}_formatted`] = this.datePipe.transform(x.show(row), true);
              break;
            }
            case 'html': {
              row[`${field}${header}_formatted`] = this.extractContentFromHtml(x.show(row), x.selector);
              break;
            }
          }
        }
        else {
          row[`${field}${header}_formatted`] = x.show(row);
        }
      });
    });

    autoTable(doc, {
      columns,
      body: rows,
      styles: { overflow: 'visible', minCellWidth: 18 },
      headStyles: { lineWidth: 0.5, overflow: 'linebreak' },
      bodyStyles: { overflow: 'linebreak' }
    });

    doc.save(`${this.exportName}_export_` + new Date().getTime());
  }

  // npm install xlsx
  exportExcel() {
    const data_to_export = [];
    const rows_to_export = this.selectedRows === undefined || this.selectedRows.length === 0 ? this._dataSource : this.selectedRows;

    const _schema: any = (this._selectedColumns.length === this._columnSchema.length) ? this._columnSchema : this._selectedColumns;
    const columns: any = _schema.map(col => ({ title: col.header, field: col.field }));

    rows_to_export.forEach(row => {
      const obj = {};

      columns.forEach(x => {
        const columnSchema = this._columnSchema.find(col => col.field === x.field && col.header === x.title);

        if (columnSchema.pipe) {
          switch (columnSchema.pipe) {
            case 'date': {
              obj[x.title] = this.datePipe.transform(columnSchema.show(row), false);
              break;
            }
            case 'onlyDate': {
              obj[x.title] = this.datePipe.transform(columnSchema.show(row), true);
              break;
            }
            case 'html': {
              obj[x.title] = this.extractContentFromHtml(columnSchema.show(row), columnSchema.selector);
              break;
            }
          }
        }
        else {
          obj[x.title] = columnSchema.show(row);
        }
      });

      data_to_export.push(obj);
    });

    const worksheet = xlsx.utils.json_to_sheet(data_to_export);
    const workbook = { Sheets: { data: worksheet }, SheetNames: ['data'] };
    const excelBuffer: any = xlsx.write(workbook, { bookType: 'xlsx', type: 'array' });
    this.saveAsExcelFile(excelBuffer, `${this.exportName}`);
  }

  /*
    npm install @types/file-saver --save-dev
    npm install file-saver --save
  */
  saveAsExcelFile(buffer: any, fileName: string): void {
    const EXCEL_TYPE = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8';
    const EXCEL_EXTENSION = '.xlsx';
    const data: Blob = new Blob([buffer], {
      type: EXCEL_TYPE
    });
    saveAs(data, fileName + '_export_' + new Date().getTime() + EXCEL_EXTENSION);
  }
  extractContentFromHtml(html: string, selector: string = '*'): string {
    const span = document.createElement('span');
    span.innerHTML = html;
    const children = span.querySelectorAll(selector);
    for (let i = 0 ; i < children.length - 1; i++) {
      if (children[i].textContent) {
        children[i].textContent += ' | ';
      } else {
        (children[i] as any).innerText += ' | ';
      }
    }
    return [span.textContent || span.innerText].toString().replace(/ +/g, ' ');
  }
}
