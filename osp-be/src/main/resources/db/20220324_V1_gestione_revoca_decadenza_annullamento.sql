INSERT INTO tp_tipo_processo
(id, descrizione)
VALUES(5, 'Revoca della concessione');

INSERT INTO tp_tipo_processo
(id, descrizione)
VALUES(6, 'Decadenza della concessione');

INSERT INTO tp_tipo_processo
(id, descrizione)
VALUES(7, 'Annullamento della concessione');

ALTER TABLE t_pratica ADD nota_al_cittadino_rda varchar(512);
ALTER TABLE t_pratica ADD codice_determina_rda varchar(255);
ALTER TABLE t_pratica ADD data_emissione_determina_rda date;

ALTER TABLE t_pratica_aud ADD nota_al_cittadino_rda varchar(512);
ALTER TABLE t_pratica_aud ADD codice_determina_rda varchar(255);
ALTER TABLE t_pratica_aud ADD data_emissione_determina_rda date;

INSERT INTO tp_tipo_allegato
(id, descrizione, descrizione_estesa)
VALUES(29, 'Determina di revoca', 'Determina di revoca occupazione temporanea');

INSERT INTO tp_tipo_allegato
(id, descrizione, descrizione_estesa)
VALUES(30, 'Determina di decadenza', 'Determina di decadenza occupazione temporanea');

INSERT INTO tp_tipo_allegato
(id, descrizione, descrizione_estesa)
VALUES(31, 'Determina di annullamento', 'Determina di annullamento occupazione temporanea');


INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(237, true, false, 3, 10, 29, 5);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(238, true, false, 4, 10, 29, 5);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(239, true, false, 3, 10, 30, 6);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(240, true, false, 4, 10, 30, 6);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(241, true, false, 3, 10, 31, 7);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(242, true, false, 4, 10, 31, 7);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(243, true, false, 5, 21, 13, 1);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(244, true, false, 5, 22, 13, 1);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(245, true, false, 5, 23, 13, 1);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(246, true, false, 5, 21, 13, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(247, true, false, 5, 22, 13, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(248, true, false, 5, 23, 13, 2);
