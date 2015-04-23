package net.iwtp.blackboard;

/**
 * Interface which should be implemented by BlackBoard subscriber
 *
 * @param <T> type of objects you want to receive
 */
public interface BlackBoardSubscriber<T> {
    /**
     * Callback which is called when object of type T is published to board
     *
     * @param obj object, published to board
     */
    public void receive(T obj);
}
