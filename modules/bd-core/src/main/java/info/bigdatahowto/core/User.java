package info.bigdatahowto.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a user on the system, identified by an authentication string.
 *
 * @author timfulmer
 */
public class User extends AggregateRoot {

    private String authentication;
    private Boolean registered;
    private String userContext;

    public User() {

        super();
    }

    public User(String authentication) {

        this();

        this.setAuthentication( authentication);
    }

    public String getAuthentication() {
        return authentication;
    }

    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    @JsonProperty
    public Boolean getRegistered() {
        return registered;
    }

    public void setRegistered(Boolean registered) {
        this.registered = registered;
    }

    @JsonIgnore
    public boolean isRegistered(){

        return this.getRegistered()!= null && this.getRegistered();
    }

    public String getUserContext() {
        return userContext;
    }

    public void setUserContext(String userContext) {
        this.userContext = userContext;
    }

    public void register(){

        this.setRegistered( true);
    }

    @Override
    public String resourceKey() {
        return String.format("users/%s", this.getAuthentication());
    }
}
