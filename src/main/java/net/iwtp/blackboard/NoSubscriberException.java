package net.iwtp.blackboard;

/**
 * Runtime Exception which is thrown when there is not subscribers for type in board
 */
public class NoSubscriberException extends RuntimeException {
    public NoSubscriberException(Class lookupClass){
        super("No subscribers for " + lookupClass);
    }
}
