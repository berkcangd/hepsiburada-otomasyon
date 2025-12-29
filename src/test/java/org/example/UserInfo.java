package org.example;

public class UserInfo {
    private String key;
    private String email;
    private String password;

    // Getter ve Setter metodları (Gson'un okuması için gerekli)
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}