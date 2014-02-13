package info.bigdatahowto.core;

/**
 * Roadie class tying together a Resource implementation, User storage and
 * retrieval.
 *
 * @author timfulmer
 */
public class UserRoadie {

    private Resource resource;

    public UserRoadie() {

        super();
    }

    public UserRoadie(Resource resource) {

        this();

        this.resource = resource;
    }

    /**
     * Accesses a user for an authentication.  If no user exists, one is
     * created.
     *
     * @param authentication    Identifies a user.
     * @return User instance.
     */
    public User getUser(String authentication) {

        User user= this.resource.get( new User( authentication));
        if( user== null){

            user= new User( authentication);
            this.resource.put( user);
        }

        return user;
    }

    /**
     * Register a user.
     *
     * TODO: Tie this into a registration request, with strong encryption.
     *
     * @param authentication Identifies a user.
     */
    public void register(String authentication, String context) {

        if( context!= null){

            UserContext userContext= this.resource.get(
                    new UserContext( context));
            assert userContext== null
                    || userContext.getAuthentication().equals( authentication):
                    String.format( "Context '%s' already created with " +
                            "another authentication.", context);
            if( userContext== null){

                userContext= new UserContext( context);
                userContext.setAuthentication( authentication);
                this.resource.put( userContext);
            }
        }

        User user= this.getUser( authentication);
        user.register();
        this.resource.put( user);
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public UserContext accessUserContext(String userContextString) {

        UserContext userContext= new UserContext( userContextString);
        userContext= this.resource.get( userContext);
        assert userContext!= null: String.format(
                "UserContext '%s' does not exist.", userContextString);

        return userContext;
    }
}
