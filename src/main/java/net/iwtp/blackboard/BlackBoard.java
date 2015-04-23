package net.iwtp.blackboard;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * BlackBoard is an implementation of Black Board pattern conception.
 * Basically, it is Observer pattern which can handle multiple types of objects
 *
 * @author Oleksandr Horobets
 */
public class BlackBoard {
    private static final Logger LOG = LoggerFactory.getLogger(BlackBoard.class);

    private Map<Class, List<BlackBoardSubscriber>> subscribers = new LinkedHashMap<Class, List<BlackBoardSubscriber>>();

    private ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("BlackBoard-Async-%d").build();
    protected ExecutorService executorService = Executors.newCachedThreadPool(threadFactory);

    /**
     * Publishes object to board. Every subscriber will receive the same instance of object.
     * Firstly trying to find subscribers by obj.getClass().
     * If not success looking for first subscriber with type that is parent for object.
     *
     * @param obj published object
     * @throws NoSubscriberException if there is no subscribers for such object
     */
    public void publish(Object obj) {
        doPublish(obj, false);
    }

    /**
     * Publishes object to board in new thread
     *
     * @param obj published object
     */
    public void asyncPublish(final Object obj) {
        doPublish(obj, true);
    }

    /**
     * Registers new subscriber using reflection to determine subscriber type.
     * Does not support lambda expressions
     * @param subscriber instance of subscriber class
     */
    public void subscribe(BlackBoardSubscriber<?> subscriber){
        subscribe(subscriber, getLookupClass(subscriber));
    }

    /**
     * Registers new subscriber.using implicit argument value as subscriber type
     * @param subscriber instance of subscriber class
     * @param lookupClass class of objects that subscriber wants to receive
     */
    public void subscribe(BlackBoardSubscriber<?> subscriber, Class<?> lookupClass){
        if(!subscribers.containsKey(lookupClass)){
            subscribers.put(lookupClass, new ArrayList<BlackBoardSubscriber>());
        }

        subscribers.get(lookupClass).add(subscriber);
    }

    /**
     * Register all subscribers in passed list using reflection to determine subscriber type.
     * Does not support lambda expressions. Does not remove previous subscribers
     * @param subscribers list of subscriber classes
     */
    public void setSubscribers(List<BlackBoardSubscriber<?>> subscribers) {
        for(BlackBoardSubscriber<?> subscriber : subscribers){
            subscribe(subscriber);
        }
    }

    private Class getLookupClass(BlackBoardSubscriber<?> subscriber) {
        for (Type type : subscriber.getClass().getGenericInterfaces()) {
            if (ParameterizedType.class.isInstance(type)) {
                ParameterizedType parameterizedType = (ParameterizedType) type;

                if (parameterizedType.getTypeName().startsWith(BlackBoardSubscriber.class.getCanonicalName())) {
                    return (Class) parameterizedType.getActualTypeArguments()[0];
                }
            }
        }

        throw new IllegalStateException("Could not determine actual type class. Use explicit class declaration instead. Subscriber: " + subscriber);
    }

    @SuppressWarnings("unchecked")
    private void doPublish(final Object obj, boolean async) {
        LOG.debug("Published {}", obj);

        for (final BlackBoardSubscriber subscriber : findSubscribers(obj)) {
            if (async) {
                executorService.submit(new Runnable() {
                    public void run() {
                        subscriber.receive(obj);
                    }
                });
            } else {
                subscriber.receive(obj);
            }
        }
    }

    private List<BlackBoardSubscriber> findSubscribers(Object obj) {
        Class lookupClass = obj.getClass();

        List<BlackBoardSubscriber> subscriberList = subscribers.get(lookupClass);

        if (subscriberList != null) {
            return subscriberList;
        } else {
            for (Class subscriberClass : subscribers.keySet()) {
                if (subscriberClass.isInstance(obj)) {
                    return subscribers.get(subscriberClass);
                }
            }
        }

        throw new NoSubscriberException(lookupClass);
    }
}
