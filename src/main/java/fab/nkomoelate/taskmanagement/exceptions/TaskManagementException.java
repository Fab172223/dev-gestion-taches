package fab.nkomoelate.taskmanagement.exceptions;

public class TaskManagementException extends RuntimeException {

    protected TaskManagementException(String message) {
        super(message);
    }

    /***
     * 409 - conflict
     * Déclecnché quand un email existe déjà en base de données
     */
    public static class EmailAlreadyExistsException extends TaskManagementException{
        public EmailAlreadyExistsException(String email) {
            super("Email déjà utilisé " + email);
        }
    }

    /***
     * 404 - erreur serveur
     * Déclenché quand un utilisateur n'est pas trouvé
     */
    public static class UserNotFoundException extends TaskManagementException{
        public  UserNotFoundException(Long id){
            super("Utilisateur non trouvé avec l'id "+id);
        }
    }

}
