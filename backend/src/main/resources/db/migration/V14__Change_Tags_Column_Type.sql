-- Change tags column from JSON to VARCHAR
-- CreatedBy: K24DTCN210-NVMANH (19/11/2025 12:16)

ALTER TABLE questions 
MODIFY COLUMN tags VARCHAR(500) NULL;
