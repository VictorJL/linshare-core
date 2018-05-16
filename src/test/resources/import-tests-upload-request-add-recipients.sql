-- ENABLE UPLOAD REQUEST FUNCTIONALITY
UPDATE policy SET status = true where id = 63;

-- Upload requests notifier.
INSERT INTO upload_request_group (id, domain_abstract_id, account_id, subject, body, uuid, creation_date, modification_date, max_file, max_deposit_size, max_file_size, activation_date, notification_date, expiry_date, can_delete, can_close, can_edit_expiry_date, locale, secured, mail_message_id, enable_notification, restricted, status)
	VALUES (7, 2, 10, 'subject of upload request 4', 'body of upload request 4', 'f4j31b58-ef45-11e5-b506-c348d7a7b65a', DATEADD(month, -2, now()), DATEADD(month, -2, now()), 3, 31457280, 10485760, DATEADD(month, -2, now()), DATEADD(month, -2, now()),DATEADD(month, 1, now()), true, true, true, 'fr', true, null,true,true, 'ENABLED');
INSERT INTO upload_request (id, upload_request_group_id, uuid, max_file, max_deposit_size, max_file_size, status, activation_date, creation_date, modification_date, notification_date, expiry_date, upload_proposition_request_uuid, can_delete, can_close, can_edit_expiry_date, locale, secured, mail_message_id, notified, dirty, enable_notification)
	VALUES (7, 7, 'f547ac1c-ef45-11e5-a73f-4b811b25f11b', 3, 31457280, 10485760, 'ENABLED', DATEADD(day, -1, now()), DATEADD(month, -2, now()), now(), now(), DATEADD(month, 3, now()), null, true, true, true, 'fr', true, null, false, false, true);
INSERT INTO upload_request_group (id, domain_abstract_id, account_id, subject, body, uuid, creation_date, modification_date, max_file, max_deposit_size, max_file_size, activation_date, notification_date, expiry_date, can_delete, can_close, can_edit_expiry_date, locale, secured, mail_message_id, enable_notification, restricted, status)
	VALUES (8, 2, 10, 'subject of upload request 5', 'body of upload request 5', '29k0a264-ef46-11e5-a0c9-0b2742279c1a', DATEADD(month, -2, now()), DATEADD(month, -2, now()), 3, 31457280, 10485760, DATEADD(month, -2, now()), DATEADD(month, -2, now()),DATEADD(month, 1, now()), true, true, true, 'fr', true, null,true,true, 'ENABLED');
INSERT INTO upload_request (id, upload_request_group_id, uuid, max_file, max_deposit_size, max_file_size, status, activation_date, creation_date, modification_date, notification_date, expiry_date, upload_proposition_request_uuid, can_delete, can_close, can_edit_expiry_date, locale, secured, mail_message_id, notified, dirty, enable_notification)
	VALUES (8, 8, '2a368b50-ef46-11e5-98c3-673ead758c8f', 3, 31457280, 10485760, 'ENABLED', DATEADD(day, -1, now()), now(), now(), now(), DATEADD(month, 4, now()), null, true, true, true, 'fr', true, null, false, false, true);
INSERT INTO upload_request_group (id, domain_abstract_id, account_id, subject, body, uuid, creation_date, modification_date, max_file, max_deposit_size, max_file_size, activation_date, notification_date, expiry_date, can_delete, can_close, can_edit_expiry_date, locale, secured, mail_message_id, enable_notification, restricted, status)
	VALUES (9, 2, 10, 'subject of upload request 6', 'body of upload request 6', '39db8e98-ef46-11e5-800e-7f734472e0d0', DATEADD(month, -2, now()), DATEADD(month, -2, now()), 3, 31457280, 10485760, DATEADD(month, -2, now()), DATEADD(month, -2, now()),DATEADD(month, 1, now()), true, true, true, 'fr', true, null,true,true, 'ENABLED');
INSERT INTO upload_request (id, upload_request_group_id, uuid, max_file, max_deposit_size, max_file_size, status, activation_date, creation_date, modification_date, notification_date, expiry_date, upload_proposition_request_uuid, can_delete, can_close, can_edit_expiry_date, locale, secured, mail_message_id, notified, dirty, enable_notification)
	VALUES (9, 9, '39a407e4-ef46-11e5-b1d1-e3c21cdc7a0a', 3, 31457280, 10485760, 'ENABLED', DATEADD(day, -3, now()), now(), DATEADD(month, 3, now()), now(), DATEADD(month, 4, now()), null, true, true, true, 'fr', true, null, false, false, true);

-- ### Grouped Mode
	-- Upload requests .
INSERT INTO upload_request_group (id, domain_abstract_id, account_id, subject, body, uuid, creation_date, modification_date, max_file, max_deposit_size, max_file_size, activation_date, notification_date, expiry_date, can_delete, can_close, can_edit_expiry_date, locale, secured, mail_message_id, enable_notification, restricted, status)
	VALUES (10, 2, 11, 'subject of upload request 4', 'body of upload request 4', 'frj31b58-ef45-11e5-b506-c348d7a7b65c', DATEADD(month, -2, now()), DATEADD(month, -2, now()), 3, 31457280, 10485760, DATEADD(month, -2, now()), DATEADD(month, -2, now()),DATEADD(month, 1, now()), true, true, true, 'fr', true, null, true, false, 'ENABLED');
INSERT INTO upload_request (id, upload_request_group_id, uuid, max_file, max_deposit_size, max_file_size, status, activation_date, creation_date, modification_date, notification_date, expiry_date, upload_proposition_request_uuid, can_delete, can_close, can_edit_expiry_date, locale, secured, mail_message_id, notified, dirty, enable_notification)
	VALUES (10, 10, 'f447ac1c-ef45-11e5-a73f-4b811b25f11b', 3, 31457280, 10485760, 'ENABLED', DATEADD(day, -1, now()), DATEADD(month, -2, now()), now(), now(), DATEADD(month, 3, now()), null, true, true, true, 'fr', true, null, false, false, true);
