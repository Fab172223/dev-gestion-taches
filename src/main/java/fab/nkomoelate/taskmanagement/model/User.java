package fab.nkomoelate.taskmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.util.List;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String lastName;
    private String  firstName;

    @Column(nullable = false, unique = true)
    @NotNull(message="l'email est obligatoire")
    @Email(message="Format d'email invalide")
    private String email;

    @OneToMany(mappedBy = "assignedUser")
    private List<Task> tasks;
}
