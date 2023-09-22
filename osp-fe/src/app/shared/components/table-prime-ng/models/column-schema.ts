//const sanitizeHtml = require('sanitize-html');
import * as sanitizeHtml from 'sanitize-html';

export class ColumnSchema {
  field: string;
  header: string;
  type: string;
  pipe?: string;
  selector?: string = '*';
  show?: Function;
  inactive?: boolean;
  customSort?: boolean;
  customSortFunction?: any;

  constructor(field: string, header: string, type: string, pipe?: string, show?: Function, inactive?: boolean, customSortFunction?: any, selector?: string) {
    this.field = field;
    this.header = header;
    this.type = type;
    this.pipe = pipe;
    this.show = (data) => {
      const fields = this.field.split(".");
      const value = fields.reduce((prev, next) => {
        return prev[next];
      }, data);
      if (show instanceof Function || typeof show === 'function')
        return sanitizeHtml(show(value, data));
      else {
        return sanitizeHtml(value || value === 0 ? value : "--");
      }
    };
    this.inactive = inactive;
    this.customSortFunction = customSortFunction;
    this.selector = selector;
  }
}
