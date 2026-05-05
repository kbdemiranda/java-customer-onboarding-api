UPDATE customer_documents
SET document_type = 'IDENTITY_REGISTER'
WHERE document_type = 'RG';

UPDATE customer_documents
SET document_type = 'DRIVER_LICENSE'
WHERE document_type = 'CNH';
