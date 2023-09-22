alter table public.t_richiedente
    add qualita_ruolo varchar(50);
alter table public.t_richiedente
    add descrizione_ruolo varchar(50);
delete from a_tipo_allegato_grup_stat_proc where id_tipo_processo = 2 and id_gruppo = 4 and id_stato_pratica in (4,7);
insert into a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES (277, false, false, 4, 4, 22, 2),
       (278, false, false, 4, 4, 5, 2),
       (279, false, false, 4, 4, 6, 2),
       (280, false, false, 4, 4, 23, 2),
       (281, false, false, 4, 4, 10, 2),
       (282, false, false, 4, 4, 24, 2),
       (283, false, false, 4, 4, 12, 2),

       (284, false, false, 4, 7, 22, 2),
       (285, false, false, 4, 7, 5, 2),
       (286, false, false, 4, 7, 6, 2),
       (287, false, false, 4, 7, 23, 2),
       (288, false, false, 4, 7, 10, 2),
       (289, false, false, 4, 7, 24, 2),
       (290, false, false, 4, 7, 12, 2);
INSERT INTO public.a_tipo_allegato_grup_stat_proc (id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo) VALUES (292, true, false, 5, 25, 13, 2);
INSERT INTO public.a_tipo_allegato_grup_stat_proc (id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo) VALUES (291, true, false, 5, 25, 13, 1);


INSERT INTO public.a_tipo_allegato_grup_stat_proc (id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica,
                                                   id_tipo_allegato, id_tipo_processo)
VALUES (293, false, false, 4, 0, 33, 1);
INSERT INTO public.a_tipo_allegato_grup_stat_proc (id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica,
                                                   id_tipo_allegato, id_tipo_processo)
VALUES (294, false, false, 4, 0, 33, 2);
INSERT INTO public.a_tipo_allegato_grup_stat_proc (id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica,
                                                   id_tipo_allegato, id_tipo_processo)
VALUES (295, false, false, 4, 0, 33, 3);


-- necessaria integrazione
INSERT INTO public.a_tipo_allegato_grup_stat_proc (id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica,
                                                   id_tipo_allegato, id_tipo_processo)
VALUES (297, false, false, 4, 4, 33, 2);

-- preavvido di diniego

INSERT INTO public.a_tipo_allegato_grup_stat_proc (id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica,
                                                   id_tipo_allegato, id_tipo_processo)
VALUES (299, false, false, 4, 7, 33, 1);
INSERT INTO public.a_tipo_allegato_grup_stat_proc (id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica,
                                                   id_tipo_allegato, id_tipo_processo)
VALUES (300, false, false, 4, 7, 33, 2);
INSERT INTO public.a_tipo_allegato_grup_stat_proc (id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica,
                                                   id_tipo_allegato, id_tipo_processo)
VALUES (301, false, false, 4, 7, 33, 3);

INSERT INTO public.tp_tipo_notifica_scadenzario (id, descrizione)
VALUES (8, 'Scadenza rettifica date');


