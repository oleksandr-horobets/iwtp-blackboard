package net.iwtp.blackboard;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doAnswer;

/**
 * @author Oleksandr Horobets
 */
public class BlackBoardTest {
    private static final String TEST_STRING = "hello world!";

    private BlackBoard blackBoard;

    @Before
    public void setUp() {
        blackBoard = new BlackBoard();
    }

    @Test
    public void testPublishNoSubscribers() {
        //given
        Exception expectedException = null;

        //when
        try {
            blackBoard.publish(TEST_STRING);
        } catch (NoSubscriberException exception){
            expectedException = exception;
        }

        //then
        assertThat(expectedException, not(isNull()));
        assertThat(expectedException, is(instanceOf(NoSubscriberException.class)));
        assertThat(expectedException.getMessage(), equalTo("No subscribers for class java.lang.String"));
    }

    @Test
    public void testPublishOneSubscriber() {
        //given
        final List<String> output = createAndSubscribeTestList();

        //when
        blackBoard.publish(TEST_STRING);

        //then
        assertThat(output, contains(TEST_STRING));
    }

    @Test
    public void testPublishMultipleSubscribers() {
        //given
        final List<String> output1 = createAndSubscribeTestList();
        final List<String> output2 = createAndSubscribeTestList();

        //when
        blackBoard.publish(TEST_STRING);

        //then
        assertListsContainsElement(output1, output2);
    }

    @Test
    public void testPublishBaseClass() {
        //given
        final List<Number> received = new LinkedList<Number>();

        blackBoard.subscribe(getSubscriber(new LinkedList<String>()));
        blackBoard.subscribe(new BlackBoardSubscriber<Number>() {
            public void receive(Number obj) {
                received.add(obj);
            }
        });


        //when
        blackBoard.publish(TEST_STRING);
        blackBoard.publish(123);
        blackBoard.publish(321l);

        //then
        assertThat(received.size(), is(2));
    }

    @Test
    public void testAsyncPublish() {
        //given
        BlackBoardSubscriber<String> subscriber = Mockito.mock(BlackBoardSubscriber.class);
        blackBoard.subscribe(subscriber, String.class);

        blackBoard.executorService = Mockito.mock(ExecutorService.class);

        doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Exception {
                Object[] args = invocation.getArguments();
                Runnable runnable = (Runnable) args[0];
                runnable.run();

                return null;
            }
        }).when(blackBoard.executorService).submit(isA(Runnable.class));

        //when
        blackBoard.asyncPublish(TEST_STRING);

        //then
        Mockito.verify(subscriber).receive(TEST_STRING);
    }

    @Test
    public void testSetSubscribers(){
        //given
        final List<String> output1 = new LinkedList<String>();
        final List<String> output2 = new LinkedList<String>();

        List<BlackBoardSubscriber<?>> subscribers = new LinkedList<BlackBoardSubscriber<?>>();

        subscribers.add(getSubscriber(output1));
        subscribers.add(getSubscriber(output2));

        //when
        blackBoard.setSubscribers(subscribers);
        blackBoard.publish(TEST_STRING);

        //then
        assertListsContainsElement(output1, output2);
    }

    @Test
    public void testPublishAnotherClass(){
        //given
        final List<String> output = createAndSubscribeTestList();

        blackBoard.subscribe(new BlackBoardSubscriber<Integer>() {
            public void receive(Integer obj) {}
        });

        //when
        blackBoard.publish(new Integer(0));

        //then
        assertThat(output.isEmpty(), is(true));
    }

    @Test
    public void testImplicitClassDeclaration(){
        //given
        final List<String> output = new LinkedList<String>();

        blackBoard.subscribe(getSubscriber(output), String.class);

        //when
        blackBoard.publish(TEST_STRING);

        //then
        assertThat(output, contains(TEST_STRING));
    }

    @Test(expected = IllegalStateException.class)
    public void testIllegalSubscriber() {
        //given
        //a blackboard

        //when
        blackBoard.subscribe(new BlackBoardSubscriber() {
            public void receive(Object obj) {
            }
        });

        //then
        //exception is thrown
    }

    private List<String> createAndSubscribeTestList() {
        final List<String> output = new LinkedList<String>();

        blackBoard.subscribe(getSubscriber(output));

        return output;
    }

    private BlackBoardSubscriber<String> getSubscriber(final List<String> output) {
        return new BlackBoardSubscriber<String>() {
            public void receive(String obj) {
                output.add(obj);
            }
        };
    }

    private void assertListsContainsElement(List<String>... outputs) {
        for(List<String> output : outputs){
            assertThat(output, contains(TEST_STRING));
        }
    }
}
