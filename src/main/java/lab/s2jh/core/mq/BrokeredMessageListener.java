package lab.s2jh.core.mq;

/**
 * Message ( reception ) Component listener interface definition
 */
public interface BrokeredMessageListener {

    /**
     * Open the message received listener
     */
    public void startup();

    /**
     * Close the message received listener
     */
    public void shutdown();
}
