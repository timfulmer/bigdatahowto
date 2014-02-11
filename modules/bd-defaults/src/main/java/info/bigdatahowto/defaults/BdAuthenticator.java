package info.bigdatahowto.defaults;

import info.bigdatahowto.core.*;

/**
 * Authenticates access to messages according to domain rules.
 *
 * @author timfulmer
 */
public class BdAuthenticator implements Authenticator {

    private UserRoadie userRoadie;

    public BdAuthenticator() {

        super();
    }

    public BdAuthenticator(UserRoadie userRoadie) {

        this();

        this.setUserRoadie( userRoadie);
    }

    /**
     * Authenticates access to a resource identified by a key.
     *
     * @param message        Resource to authorize.
     * @param authentication Authentication identifying a user.
     * @param privilege      Privilege to authorize.
     * @return True if the access to the resource is granted.
     */
    @Override
    public boolean authorize(Message message, String authentication,
                             BehaviorType privilege) {

        switch( privilege){
            case Get: return !message.isSecure()
                    || isOwner( authentication, message);
            case Persist:
            case Delete: User user= this.userRoadie.getUser( authentication);
                return isOwner( authentication, message) && user.isRegistered();
            default: return false;
        }
    }

    /**
     * Provisions an authentication for access to a key.  Please note this
     * method modifies message state, but does not persist it.
     *
     * @param message        Resource to authorize.
     * @param authentication Authentication identifying a user.
     */
    @Override
    public void provision(Message message, String authentication) {

        UserContext context= this.userRoadie.accessUserContext(
                message.getMessageKey().getUserContext());
        if( context== null){

            throw new IllegalStateException( String.format(
                    "Context '%s' does not exist, please register this " +
                            "context to an authentication.",
                    message.getMessageKey().getUserContext()));
        }
        message.setContextOwner( context.getAuthentication());
        message.setMessageOwner( authentication);
    }

    public void setUserRoadie(UserRoadie userRoadie) {
        this.userRoadie = userRoadie;
    }

    private boolean isOwner( String authentication, Message message){

        return authentication.equals( message.getContextOwner())
                || authentication.equals( message.getMessageOwner());
    }
}
