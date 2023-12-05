package ch.cydcampus.hickup.model;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;

/*
 * This class is used to capture traffic from two interfaces at the same time.
 * It makes sure that the output is chronological.
 */
public class DoubleInterfaceSource implements DataSource {

    NetworkCaptureSource receiverInterface;
    NetworkCaptureSource senderInterface;
    boolean isProducing = true;
    boolean didSleep = false;

    public DoubleInterfaceSource(String receiverInterface, String senderInterface) throws PcapNativeException, NotOpenException  {
        this.receiverInterface = new NetworkCaptureSource(receiverInterface);
        this.senderInterface = new NetworkCaptureSource(senderInterface);
    }

    @Override
    public Token consume() throws InterruptedException {
        Token receiverToken = this.receiverInterface.peek();
        Token senderToken = this.senderInterface.peek();

        while(isProducing) {
            if(receiverToken == null && senderToken == null) {
                Thread.sleep(1);
                receiverToken = this.receiverInterface.peek();
                senderToken = this.senderInterface.peek();
            } else if(receiverToken != null && senderToken != null) {
                if(receiverToken.getTimeInterval().compareTo(senderToken.getTimeInterval()) < 0) {
                    return this.receiverInterface.consume();
                } else {
                    return this.senderInterface.consume();
                }
            } else if(didSleep) {
                didSleep = false;
                if(receiverToken != null) {
                    return this.receiverInterface.consume();
                } else {
                    return this.senderInterface.consume();
                }
            } else {
                Thread.sleep(1);
                didSleep = true;
            }
        }
        return null;
    }

    @Override
    public void stopProducer() {
        this.receiverInterface.stopThread();
        this.senderInterface.stopThread();
        this.isProducing = false;
    }

    @Override
    public void registerReader() {
        this.receiverInterface.registerReader();
        this.senderInterface.registerReader();
    }
    
    
}
