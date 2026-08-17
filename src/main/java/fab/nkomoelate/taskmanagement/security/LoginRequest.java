// LoginRequest.java
package fab.nkomoelate.taskmanagement.security;

public record LoginRequest(
        String email,
        String password
) {}