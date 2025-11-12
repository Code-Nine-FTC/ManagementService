package com.codenine.managementservice.dto.chat;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateGuestRequest {

  @NotBlank(message = "Nome é obrigatório")
  @Size(min = 3, max = 255, message = "Nome deve ter entre 3 e 255 caracteres")
  private String name;

  @NotBlank(message = "CPF é obrigatório")
  @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos")
  private String cpf;

  @NotNull(message = "Idade é obrigatória")
  @Min(value = 1, message = "Idade deve ser maior que 0")
  @Max(value = 120, message = "Idade deve ser menor que 120")
  private Integer age;

  @NotBlank(message = "Sexo é obrigatório")
  @Pattern(regexp = "M|F|O", message = "Sexo deve ser M, F ou O")
  private String gender;

  @NotBlank(message = "E-mail é obrigatório")
  @Email(message = "E-mail inválido")
  private String email;

  @NotBlank(message = "Senha é obrigatória")
  @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
  private String password;
}
