-- inserimento stato rettifica date
INSERT INTO tp_stato_pratica
(id, descrizione)
VALUES(27, 'Rettifica date');

ALTER TABLE t_integrazione ADD data_inizio_occupazione date;
ALTER TABLE t_integrazione ADD ora_inizio_occupazione time;
ALTER TABLE t_integrazione ADD data_scadenza_occupazione date;
ALTER TABLE t_integrazione ADD ora_scadenza_occupazione time;

ALTER TABLE t_integrazione_aud ADD data_inizio_occupazione date;
ALTER TABLE t_integrazione_aud ADD ora_inizio_occupazione time;
ALTER TABLE t_integrazione_aud ADD data_scadenza_occupazione date;
ALTER TABLE t_integrazione_aud ADD ora_scadenza_occupazione time;

UPDATE t_richiesta_integrazione SET tipo_richiesta = 'PROCEDURA_DINIEGO' WHERE tipo_richiesta = 'Procedura di diniego';
UPDATE t_richiesta_integrazione_aud SET tipo_richiesta = 'PROCEDURA_DINIEGO' WHERE tipo_richiesta = 'Procedura di diniego';