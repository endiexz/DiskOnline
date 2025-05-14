package top.endiexz.DiskOnline.entity;

public class User {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String userRank;
    private Long availCapacity;
    private Long usedCapacity;
    public Long getId(){
        return this.id;
    }
    public String getUserRank() {
        return userRank;
    }
    public void setUserRank(String userRank) {
        this.userRank = userRank;
    }
    public void setId(Long id){
        this.id = id;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public Long getAvailCapacity() {
        return availCapacity;
    }
    public void setAvailCapacity(Long availCapacity) {
        this.availCapacity = availCapacity;
    }
    public Long getUsedCapacity() {
        return usedCapacity;
    }
    public void setUsedCapacity(Long usedCapacity) {
        this.usedCapacity = usedCapacity;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
