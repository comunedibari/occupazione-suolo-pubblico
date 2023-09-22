import { Pageable } from "./pageable";

export class Page {
    content: any[];
    pageable: Pageable;
    last: boolean;
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
    sort: {
       empty: boolean;
       sorted: boolean;
       unsorted: boolean;
    };
    first: boolean;
    numberOfElements: number;
    empty: boolean;
}
