package com.codenine.managementservice.dto.user;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserRequest(
    @Schema(description = "Nome completo do usuário", example = "João da Silva")
    String name,
    @Schema(description = "E-mail do usuário", example = "joao@email.com")
    String email,
    @Schema(description = "Senha do usuário", example = "senhaSegura123")
    String password,
    @Schema(description = "Papel do usuário no sistema", example = ("ADMIN/ASSISTANT/MANAGER"))
    Role role,
    @Schema(description = "Lista de IDs das seções às quais o usuário pertence", example = "[1,2,3]")
    List<Long> sectionIds
) {
}
