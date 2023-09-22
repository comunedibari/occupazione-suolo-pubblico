ALTER TABLE t_pratica ADD motivazione_richiesta varchar(512);
ALTER TABLE t_pratica ADD codice_determina_rinuncia varchar(255);
ALTER TABLE t_pratica ADD data_emissione_determina_rin date;

ALTER TABLE t_pratica_aud ADD motivazione_richiesta varchar(512);
ALTER TABLE t_pratica_aud ADD codice_determina_rinuncia varchar(255);
ALTER TABLE t_pratica_aud ADD data_emissione_determina_rin date;

-- inserimento processo rinuncia
INSERT INTO tp_tipo_processo
(id, descrizione)
VALUES(3, 'Rinuncia concessione temporanea');

-- inserimento stato rinunciata
INSERT INTO tp_stato_pratica
(id, descrizione)
VALUES(26, 'Rinunciata');

-- inserimento nuovi tipi allegato
INSERT INTO tp_tipo_allegato
(id, descrizione, descrizione_estesa)
VALUES(25, 'Documentazione ripristino stato dei luoghi', 'Documentazione tecnica ripristino stato dei luoghi da tecnico abilitato');
INSERT INTO tp_tipo_allegato
(id, descrizione, descrizione_estesa)
VALUES(26, 'Foto ripristino stato dei luoghi', 'Foto di ripristino dello stato dei luoghi redatte da tecnico abilitato');
INSERT INTO tp_tipo_allegato
(id, descrizione, descrizione_estesa)
VALUES(27, 'Determina di rinuncia', 'Determina di rinuncia occupazione temporanea');

-- inserimento configurazioni allegati processo rinuncia
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(202, true, false, 2, 0, 5, 3);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(203, false, false, 2, 0, 25, 3);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(204, false, false, 2, 0, 26, 3);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(205, false, false, 2, 0, 8, 3);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(206, false, false, 2, 0, 10, 3);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(207, false, true, 2, 0, 12, 3);

INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(208, true, false, 4, 0, 5, 3);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(209, false, false, 4, 0, 25, 3);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(210, false, false, 4, 0, 26, 3);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(211, false, false, 4, 0, 8, 3);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(212, false, false, 4, 0, 10, 3);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(213, false, true, 4, 0, 12, 3);

INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(214, true, false, 20, 0, 5, 3);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(215, false, false, 20, 0, 25, 3);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(216, false, false, 20, 0, 26, 3);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(217, false, false, 20, 0, 8, 3);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(218, false, false, 20, 0, 10, 3);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(219, false, true, 20, 0, 12, 3);

INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(220, true, false, 2, 4, 5, 3);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(221, false, false, 2, 4, 25, 3);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(222, false, false, 2, 4, 26, 3);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(223, false, false, 2, 4, 8, 3);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(224, false, false, 2, 4, 10, 3);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(225, false, true, 2, 4, 12, 3);

INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(226, true, false, 4, 4, 5, 3);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(227, false, false, 4, 4, 25, 3);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(228, false, false, 4, 4, 26, 3);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(229, false, false, 4, 4, 8, 3);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(230, false, false, 4, 4, 10, 3);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(231, false, true, 4, 4, 12, 3);

INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(232, true, false, 4, 6, 27, 3);

INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(233, true, false, 5, 26, 13, 3);

