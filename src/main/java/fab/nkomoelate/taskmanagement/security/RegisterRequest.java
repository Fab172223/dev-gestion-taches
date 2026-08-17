// RegisterRequest.java
package fab.nkomoelate.taskmanagement.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        String lastName,
        String firstName,
        @NotBlank(message = "l'email est obligatoire")
        @Email(message = "Format d'email invalide")
        String email,
        @NotBlank(message = "le mot de passe est obligatoire")
        @Size(min = 8, message = "le mot de passe doit contenir au moins 8 caractères")
        String password
) {}