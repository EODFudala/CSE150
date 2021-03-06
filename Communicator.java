package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {

    private Lock lock; //the lock for the communicators
    private Condition speakerSend; //used to send message
    private Condition listenerReceive; //used to receive message

    private int speaker; //used to tell if there is a speaker
    private int listener; //used to tell if there is a listener
    private boolean sent; //used to confirm speaker message has been sent

    private int messageSend; //what the speaker sends
    private int messageReceive; //what the listener receives

    /**
     * Allocate a new communicator.
     */
    public Communicator() {
        lock = new Lock();  
        speakerSend = new Condition(lock);
        listenerReceive = new Condition(lock);
        speaker = 0;
        listener = 0;

        sent = false; 
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param   word    the integer to transfer.
     */
    public void speak(int word) {
        lock.acquire();
        speaker++;

        while(listener == 0 || sent){
            speakerSend.sleep(); //puts current speaker to sleep if there are no more listeners
        }

        messageSend = word; //transfer the word   
        sent = true;//tells that the message is ready
        listenerReceive.wake(); //wakes next listener

        speaker--;
        lock.release(); 
    }


    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return  the integer transferred.
     */    
    public int listen() {

        lock.acquire();
        listener++; //listener 
        while(!sent){ //if sent is false, listeners are asleep. We will wake the speakers to send if they have anything. 
            speakerSend.wake();
            listenerReceive.sleep();
        }
        //listenerReceive.wake();

        messageReceive = messageSend; //listener has the message now
        sent = false; //tells that the message is not sent for the next speaker

        listener--; //listen has message so we can reduce it
        lock.release();
        return messageReceive;

    }
}
