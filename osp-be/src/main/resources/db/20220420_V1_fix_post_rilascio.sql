alter table t_pratica add constraint UQ_PRATICA_DATI_RICHIESTA unique (id_dati_richiesta);

ALTER TABLE t_gruppo ADD flg_verifica_ripristino_luoghi boolean NOT NULL DEFAULT false;

UPDATE t_gruppo
SET flg_verifica_ripristino_luoghi=true
WHERE id=5;

