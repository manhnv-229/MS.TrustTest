import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePasswordHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String password = "Admin@123";
        String hash = encoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        
        // Verify the hash
        boolean matches = encoder.matches(password, hash);
        System.out.println("Matches: " + matches);
        
        // Test with existing hash from database
        String existingHash = "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lk3K3Z7.n8Nu";
        boolean matchesExisting = encoder.matches(password, existingHash);
        System.out.println("Matches existing hash: " + matchesExisting);
    }
}
