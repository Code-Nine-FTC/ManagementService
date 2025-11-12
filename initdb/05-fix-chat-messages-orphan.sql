-- Fix orphan chat messages (messages without valid sender)
-- This script deletes messages where the sender no longer exists

-- First, delete orphan messages
DELETE FROM chat_messages 
WHERE sender_id NOT IN (SELECT id FROM users);

-- Optionally, if you want to keep messages but allow null sender in the future:
-- ALTER TABLE chat_messages DROP CONSTRAINT fk_sender;
-- ALTER TABLE chat_messages ALTER COLUMN sender_id DROP NOT NULL;
-- ALTER TABLE chat_messages ADD CONSTRAINT fk_sender 
--     FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE SET NULL;

