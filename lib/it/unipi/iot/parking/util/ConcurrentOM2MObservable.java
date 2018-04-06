/*
 * Copyright 2018 Gabriele Ara (gabriele.ara@live.it)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package it.unipi.iot.parking.util;

import java.util.HashSet;
import java.util.Set;

import it.unipi.iot.parking.om2m.data.OM2MResource;

/**
 * This class can be extended to implement easily an Observable/Observer pattern
 * in Java over a set of events defined as an Enumerated type.
 *
 * @author Gabriele Ara {@literal <gabriele.ara@live.it>}
 * @param <Event>
 *            the Enumarated type representing different events related to the
 *            Observable object status
 */
// TODO: change completely documentation
public class ConcurrentOM2MObservable implements OM2MObservable {
    
    // The object used to synchronize the behavior of this Observable
    private final Object MONITOR = new Object();
    // The map between each event and the list of Observers registered on it
    private final Set<Observer> _observers = new HashSet<>();
    
    /**
     * This method adds a new Observer - it will be notified when Observable changes
     *
     * @param event
     *            the event that will be tracked
     * @param observer
     *            the object that shall be notified when an event equal to the given
     *            one occurs
     */
    @Override
    public void registerObserver(Observer observer) {
        if (observer == null)
            throw new IllegalArgumentException("The observer parameter cannot be null");
        
        synchronized (MONITOR) {
            _observers.add(observer);
        }
    }
    
    /**
     * This method removes an Observable - it will no longer be notified when the
     * current object status changes.
     *
     * @param event
     *            the event associated with the observer to be removed
     * @param observer
     *            the observer to be removed from the set
     * @return true if the set of the registered observable has been modified by
     *         this operation, false otherwise
     */
    @Override
    public boolean unregisterObserver(Observer observer) {
        synchronized (MONITOR) {
            return _observers.remove(observer);
        }
    }
    
    /**
     * This method notifies currently registered observers about Observable's
     * changes through the usage of events.
     *
     * @param event
     *            the event occurred, only observables registered on this event will
     *            be notified, one by one
     */
    public void notifyObservers(OM2MResource newResource) {
        Set<Observer> observersCopy;
        
        synchronized (MONITOR) {
            observersCopy = new HashSet<>(_observers);
        }
        
        for (Observer observer : observersCopy) {
            observer.onObservableChanged(this, newResource);
        }
    }
}
