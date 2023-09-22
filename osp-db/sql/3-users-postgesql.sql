CREATE USER osp WITH password 'passosp';
GRANT CONNECT ON DATABASE osp TO osp;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA "public" TO osp;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA "public" TO osp;
