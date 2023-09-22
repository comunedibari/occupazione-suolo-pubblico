export class Pageable {
   sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
   };
   offset: number;
   pageSize: number;
   pageNumber: number;
   paged: boolean;
   unpaged: boolean;
}
