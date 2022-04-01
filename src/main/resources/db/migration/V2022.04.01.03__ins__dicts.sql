insert into client_group ( id, code ) values ( 1, 'DEFAULT' );

insert into stage ( id, code, description )
values ( 1, 'CHECKING'   , 'checking' ),
       ( 2, 'WEB_REQUEST_STAGE'   , 'blocking' ),
       ( 4, 'COMPLETED'   , 'COMPLETED' ),
       ( 3, 'FIRST_STAGE', 'logging_out' );

INSERT INTO action ( id, code, description )
VALUES ( 1,  'WEB_REQUEST_ACTION'    , '' ),
       ( 2,  'FIRST_ACTION'   , '' );

INSERT INTO action_status ( id
                          , code
                          , description )
VALUES ( 1,  'IN_PROGRESS', 'in progress' ),
       ( 2,  'FAILED'     , 'failed'      ),
       ( 3,  'SUCCEEDED'  , 'succeeded'   );

INSERT INTO stage ( id
                  , code
                  , description )
VALUES ( 9, 'STARTED'   , 'Process has been just started' );

INSERT INTO action_status (id, code, description)
VALUES (4, 'REJECTED', 'Action has been rejected');
