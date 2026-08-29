-- Lets a supplier_registration_attachment row BE the answer to a dynamic file_upload question
-- (Form Studio's questions.id), instead of only ever being a free-form extra attachment.
-- Reuses the existing attachment table/preview/download flow rather than adding a new one.
ALTER TABLE supplier_registration_attachment ADD COLUMN question_id INT NULL;
