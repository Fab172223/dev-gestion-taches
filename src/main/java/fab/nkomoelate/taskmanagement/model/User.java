package fab.nkomoelate.taskmanagement.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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
    private String email;
    @OneToMany(mappedBy = "assignedUser")
    private List<Task> tasks;
}
