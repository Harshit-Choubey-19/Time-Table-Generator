package loginandsignup;
public class UserSession {
    private static UserSession instance;
    private String userId;
    private String fullName;
    private String email;
    private String dept;
    private String role;

    private UserSession(String userId, String fullName, String email, String dept, String role) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.dept = dept;
        this.role = role;
    }

    // Singleton instance getter
    public static void createSession(String userId, String fullName, String email, String dept, String role) {
        instance = new UserSession(userId, fullName, email, dept, role);
    }

    public static UserSession getInstance() {
        return instance;
    }

    public static void clearSession() {
        instance = null; // Log out user
    }

    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }
    public String getDept() {
        return dept;
    }
    public String getRole(){
        return role;
    }
}
