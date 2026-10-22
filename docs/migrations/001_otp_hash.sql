-- Run once against an existing database before starting the updated application.
-- BCrypt OTP hashes require more than the old six-character plaintext column.
ALTER TABLE users MODIFY COLUMN otp_code VARCHAR(100) NULL;
