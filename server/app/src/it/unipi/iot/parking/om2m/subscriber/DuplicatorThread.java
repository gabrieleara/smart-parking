package it.unipi.iot.parking.om2m.subscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;

import it.unipi.iot.parking.ParksDataHandler;
import it.unipi.iot.parking.om2m.ErrorCode;
import it.unipi.iot.parking.om2m.OM2MException;
import it.unipi.iot.parking.om2m.data.OM2MResource;

//TODO: some copies may be scheduled to be written, but never are if the thread
//is interrupted
public class DuplicatorThread extends Thread {
    
    private final SortedSet<CopyRequest> queue = new TreeSet<>();
    
    private DuplicatorThread() {
    }
    
    private static DuplicatorThread instance = null;
    
    public static DuplicatorThread init() {
        // Overwrites old instance: INSECURE but it will be used in a secure way
        instance = new DuplicatorThread();
        
        return instance;
    }
    
    public static DuplicatorThread getInstance() {
        return instance;
    }
    
    public void requestCopy(final OM2MResource resource, final CopyResourceSubscriber demanding) {
        final CopyRequest request = new CopyRequest(resource, demanding);
        
        synchronized (this) {
            queue.add(request);
            notify();
        }
    }
    
    public void requestAll(final List<OM2MResource> resources,
            final CopyResourceSubscriber demanding) {
        final List<CopyRequest> requestsList;
        
        if (resources.size() < 1)
            return;
        
        // Shortcut
        if (resources.size() == 1) {
            requestCopy(resources.get(0), demanding);
            return;
        }
        
        requestsList = new ArrayList<>(resources.size());
        
        for (int i = 0; i < resources.size(); ++i) {
            requestsList.add(new CopyRequest(resources.get(i), demanding));
            // requestsList.set(i, new CopyRequest(resources.get(i), demanding));
        }
        
        synchronized (this) {
            queue.addAll(requestsList);
            notify();
        }
    }
    
    private CopyRequest extract() throws InterruptedException {
        final CopyRequest request;
        
        synchronized (this) {
            while (queue.isEmpty()) {
                this.wait();
            }
            
            request = queue.first();
            queue.remove(request);
        }
        
        return request;
    }
    
    @Override
    public void run() {
        CopyRequest request;
        // OM2MResource copy;
        OM2MResource original;
        CopyResourceSubscriber demanding;
        
        try {
            while (!this.isInterrupted()) {
                request = extract();
                original = request.original;
                demanding = request.demanding;
                
                try {
                    // copy =
                    ParksDataHandler.createCopy(original, demanding.getBaseCopyURI());
                } catch (OM2MException e) {
                    if (e.getCode() == ErrorCode.NAME_ALREADY_PRESENT) {
                        // Keep going, it's all fine and it is an intended behavior
                        System.out.println("Don't worry, OM2MException caught properly!");
                        // If needed, implement this
                        // copy = ParksDataHandler.get();
                    } else {
                        // Something bad happened!
                        throw e;
                    }
                } catch (TimeoutException e) {
                    // NOTICE: these timeouts mean that the IN was not available! We are kept
                    // out of the world!
                    throw new RuntimeException(
                            "IN unavailable! Contact system admin as soon as possible!");
                }
                
                // If the thread should terminate, the whole system is meant to terminate, so
                // let us avoid some effort from now on
                if (this.isInterrupted())
                    break;
                
                demanding.postProcess(original);
            }
        } catch (InterruptedException ex) {
            // Thread terminates, as requested, even if there is still stuff in the queue!
        }
    }
    
    private static class CopyRequest implements Comparable<CopyRequest> {
        private final OM2MResource           original;
        private final CopyResourceSubscriber demanding;
        
        public CopyRequest(OM2MResource original, CopyResourceSubscriber demanding) {
            this.original = original;
            this.demanding = demanding;
        }
        
        @Override
        public int compareTo(CopyRequest o) {
            return this.original.compareTo(o.original);
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((demanding == null) ? 0 : demanding.hashCode());
            result = prime * result + ((original == null) ? 0 : original.hashCode());
            return result;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CopyRequest other = (CopyRequest) obj;
            if (demanding == null) {
                if (other.demanding != null)
                    return false;
            } else if (!demanding.equals(other.demanding))
                return false;
            if (original == null) {
                if (other.original != null)
                    return false;
            } else if (!original.equals(other.original))
                return false;
            return true;
        }
    }
}
