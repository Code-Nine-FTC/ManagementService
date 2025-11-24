-- Fix orphan chat messages (messages without valid sender)
-- This script deletes messages where the sender no longer exists
-- AND adds support for guest senders

-- First, delete orphan messages
DELETE FROM chat_messages 
WHERE sender_id NOT IN (SELECT id FROM users);

-- Add guest_sender_id column if it doesn't exist
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='chat_messages' AND column_name='guest_sender_id') THEN
        ALTER TABLE chat_messages ADD COLUMN guest_sender_id BIGINT;
        ALTER TABLE chat_messages ADD CONSTRAINT fk_guest_sender 
            FOREIGN KEY (guest_sender_id) REFERENCES guest_users(id) ON DELETE SET NULL;
    END IF;
END $$;

-- Make sender_id nullable
ALTER TABLE chat_messages ALTER COLUMN sender_id DROP NOT NULL;

-- Change FK constraint to SET NULL on delete
ALTER TABLE chat_messages DROP CONSTRAINT IF EXISTS fk_sender;
ALTER TABLE chat_messages ADD CONSTRAINT fk_sender 
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE SET NULL;

-- Add check constraint to ensure either sender_id or guest_sender_id is set (but not both)
ALTER TABLE chat_messages DROP CONSTRAINT IF EXISTS chk_sender;
ALTER TABLE chat_messages ADD CONSTRAINT chk_sender 
    CHECK ((sender_id IS NOT NULL AND guest_sender_id IS NULL) OR (sender_id IS NULL AND guest_sender_id IS NOT NULL));

