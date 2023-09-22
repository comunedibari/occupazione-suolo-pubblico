INSERT INTO tp_tipo_processo
(id, descrizione)
VALUES(4, 'Rettifica per correzione errori materiali');

ALTER TABLE t_protocollo ADD tipo_operazione varchar(100);
ALTER TABLE t_protocollo ADD codice_determina_rettifica varchar(255);
ALTER TABLE t_protocollo ADD data_emissione_determina_ret date;

ALTER TABLE t_protocollo_aud ADD tipo_operazione varchar(100);
ALTER TABLE t_protocollo_aud ADD codice_determina_rettifica varchar(255);
ALTER TABLE t_protocollo_aud ADD data_emissione_determina_ret date;

ALTER TABLE t_protocollo
DROP CONSTRAINT UQ_PROTOCOLLO_PRATICA_STATO;

INSERT INTO tp_tipo_allegato
(id, descrizione, descrizione_estesa)
VALUES(28, 'Determina di rettifica', 'Determina di rettifica per risoluzioni errori materiali');

INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(234, true, false, 4, 8, 28, 4);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(235, true, false, 4, 9, 28, 4);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(236, true, false, 4, 10, 28, 4);

