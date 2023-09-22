INSERT INTO tp_tipo_allegato
(id, descrizione, descrizione_estesa)
VALUES(32, 'Determina di proroga', 'Determina di proroga occupazione temporanea');

UPDATE a_tipo_allegato_grup_stat_proc
SET id_tipo_allegato=32
WHERE id=144;
