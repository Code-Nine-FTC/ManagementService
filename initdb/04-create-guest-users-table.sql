-- Create guest_users table
CREATE TABLE IF NOT EXISTS guest_users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    age INTEGER NOT NULL,
    gender VARCHAR(1) NOT NULL CHECK (gender IN ('M', 'F', 'O')),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    chat_room_id BIGINT,
    CONSTRAINT fk_guest_chat_room FOREIGN KEY (chat_room_id) REFERENCES chat_rooms(id) ON DELETE SET NULL
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_guest_users_email ON guest_users(email);
CREATE INDEX IF NOT EXISTS idx_guest_users_cpf ON guest_users(cpf);
CREATE INDEX IF NOT EXISTS idx_guest_users_chat_room_id ON guest_users(chat_room_id);
CREATE INDEX IF NOT EXISTS idx_guest_users_is_active ON guest_users(is_active);

-- Add comment to table
COMMENT ON TABLE guest_users IS 'Tabela para armazenar usuários convidados (guests) com acesso restrito apenas ao chat';
COMMENT ON COLUMN guest_users.gender IS 'M = Masculino, F = Feminino, O = Outro';
