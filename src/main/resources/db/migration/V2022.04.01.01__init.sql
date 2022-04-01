CREATE TABLE client_group (
	id smallint NOT NULL,
	code varchar(64) NOT NULL,
	CONSTRAINT group_pk PRIMARY KEY (id)
);

CREATE TABLE deletion_process (
	id bigserial NOT NULL,
	updated timestamp NOT NULL,
	stage_id smallint NOT NULL,
	client_id bigint NOT NULL,
	CONSTRAINT deletion_process_pk PRIMARY KEY (id)
);

CREATE TABLE stage (
	id smallint NOT NULL,
	code varchar(128) NOT NULL,
	description varchar(512) NOT NULL,
	CONSTRAINT stage_pk PRIMARY KEY (id)
);

ALTER TABLE deletion_process ADD CONSTRAINT stage_fk FOREIGN KEY (stage_id)
REFERENCES stage (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;

CREATE TABLE action (
	id smallint NOT NULL,
	code varchar(128) NOT NULL,
	description varchar(512) NOT NULL,
	CONSTRAINT action_pk PRIMARY KEY (id)
);

CREATE TABLE process_stage_action (
	id bigserial NOT NULL,
	error_count smallint NOT NULL DEFAULT 0,
	updated timestamp NOT NULL DEFAULT now(),
	error_description text,
	deletion_process_id bigint NOT NULL,
	action_id smallint NOT NULL,
	action_status_id smallint NOT NULL,
	CONSTRAINT process_stage_action_pk PRIMARY KEY (id)
);

ALTER TABLE process_stage_action ADD CONSTRAINT deletion_process_fk FOREIGN KEY (deletion_process_id)
REFERENCES deletion_process (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE process_stage_action ADD CONSTRAINT action_fk FOREIGN KEY (action_id)
REFERENCES action (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;

CREATE TABLE action_status (
	id smallint NOT NULL,
	code varchar(128) NOT NULL,
	description varchar(512) NOT NULL,
	CONSTRAINT action_status_pk PRIMARY KEY (id)
);

ALTER TABLE process_stage_action ADD CONSTRAINT action_status_fk FOREIGN KEY (action_status_id)
REFERENCES action_status (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;

CREATE TABLE client (
	id bigint NOT NULL,
	client_group_id smallint NOT NULL,
	CONSTRAINT client_pk PRIMARY KEY (id)
);

ALTER TABLE client ADD CONSTRAINT client_group_fk FOREIGN KEY (client_group_id)
REFERENCES client_group (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE deletion_process ADD CONSTRAINT client_fk FOREIGN KEY (client_id)
REFERENCES client (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;

CREATE TABLE process_completion_log (
	id uuid NOT NULL,
	completion jsonb NOT NULL,
	CONSTRAINT process_completion_log_pk PRIMARY KEY (id)
);

