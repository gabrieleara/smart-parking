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
public interface OM2MObservable {
    /**
     * The interface that any Observer of this class shall implement.
     */
    public interface Observer {
        /**
         * This method will be called whenever an event is fired on an Observable object
         * on which the current object holds a subscription for that event. There is no
         * way to distinguish between events fired on different Observable objects.
         *
         * NOTICE: this method should be as quick as possible, because its execution
         * occurs on the same thread that fired the event on the Observable object.
         * Thus, never execute blocking calls from within this method, prefer rather to
         * wake up or start a new Thread and execute the majority of the code on it.
         * 
         * @param observable
         *            the Observable that fired the given event
         * @param event
         *            the event that has been fired on the registered Observable
         */
        void onObservableChanged(OM2MObservable observable, OM2MResource newResource);
    }
    
    /**
     * This method adds a new Observer - it will be notified when Observable changes
     *
     * @param event
     *            the event that will be tracked
     * @param observer
     *            the object that shall be notified when an event equal to the given
     *            one occurs
     */
    public void registerObserver(Observer observer);
    
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
    public boolean unregisterObserver(Observer observer);
}
