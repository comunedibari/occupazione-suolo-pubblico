DELETE FROM t_template
WHERE id_tipo_template=12;

DELETE FROM tp_tipo_template
WHERE id=12;

ALTER TABLE tp_tipo_template ALTER COLUMN descrizione TYPE varchar(100) USING descrizione::varchar;

INSERT INTO tp_tipo_template (id, descrizione) VALUES (12, 'Istruttoria tecnica IVOOPP - Settore Urbanizzazioni Primarie');
INSERT INTO tp_tipo_template (id, descrizione) VALUES (13, 'Istruttoria tecnica IVOOPP - Settore Giardini');
INSERT INTO tp_tipo_template (id, descrizione) VALUES (14, 'Istruttoria tecnica IVOOPP - Settore Interventi sul Territorio');
INSERT INTO tp_tipo_template (id, descrizione) VALUES (15, 'Istruttoria tecnica IVOOPP - Settore Infrastrutture a Rete');
INSERT INTO tp_tipo_template (id, descrizione) VALUES (16, 'Istruttoria tecnica Ripartizione Urbanistica');
INSERT INTO tp_tipo_template (id, descrizione) VALUES (17, 'Istruttoria tecnica Ripartizione Patrimonio');

INSERT INTO t_template
(id, data_inserimento, data_modifica, file_template, mime_type, nome_file, id_tipo_template, id_utente_modifica)
VALUES((select nextval('seq_template')), (select CURRENT_TIMESTAMP), NULL, decode('CA','hex'), 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'TemplateIstruttoriaIVOOPP-UrbanizzazioniPrimarie.docx', 12, NULL);
INSERT INTO t_template
(id, data_inserimento, data_modifica, file_template, mime_type, nome_file, id_tipo_template, id_utente_modifica)
VALUES((select nextval('seq_template')), (select CURRENT_TIMESTAMP), NULL, decode('CA','hex'), 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'TemplateIstruttoriaIVOOPP-Giardini.docx', 13, NULL);
INSERT INTO t_template
(id, data_inserimento, data_modifica, file_template, mime_type, nome_file, id_tipo_template, id_utente_modifica)
VALUES((select nextval('seq_template')), (select CURRENT_TIMESTAMP), NULL, decode('CA','hex'), 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'TemplateIstruttoriaIVOOPP-InterventiTerritorio.docx', 14, NULL);
INSERT INTO t_template
(id, data_inserimento, data_modifica, file_template, mime_type, nome_file, id_tipo_template, id_utente_modifica)
VALUES((select nextval('seq_template')), (select CURRENT_TIMESTAMP), NULL, decode('CA','hex'), 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'TemplateIstruttoriaIVOOPP-InfrastruttureRete.docx', 15, NULL);
INSERT INTO t_template
(id, data_inserimento, data_modifica, file_template, mime_type, nome_file, id_tipo_template, id_utente_modifica)
VALUES((select nextval('seq_template')), (select CURRENT_TIMESTAMP), NULL, decode('CA','hex'), 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'TemplateIstruttoriaUrbanistica.docx', 16, NULL);
INSERT INTO t_template
(id, data_inserimento, data_modifica, file_template, mime_type, nome_file, id_tipo_template, id_utente_modifica)
VALUES((select nextval('seq_template')), (select CURRENT_TIMESTAMP), NULL, decode('CA','hex'), 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'TemplateIstruttoriaPatrimonio.docx', 17, NULL);