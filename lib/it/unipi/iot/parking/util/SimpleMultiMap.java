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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class implements a simple MultiMap using {@link HashMap} and
 * {@link HashSet} classes. In particular, for each key it is associated a
 * {@link Set} of objects that can be accessed using that key.
 *
 * NOTICE: this is a simple implementation that assumes that the allocations of
 * a few {@link Set} instances is not a big deal for the system. To be more
 * precise, this implementation does not delete empty Set objects on removal of
 * the only value associated with a key. If this is an important issue for you,
 * please feel free to add this functionality in your own class based on this
 * one (even if not extending this class). As a last note, please, give me some
 * attribute if this class inspired the one you wrote by yourself :) .
 *
 * @author Gabriele Ara {@literal <gabriele.ara@live.it>}
 * @param <K>
 *            the class used as key for the SimpleMultiMap
 * @param <V>
 *            the class used as value for the SimpleMultiMap
 */
public class SimpleMultiMap<K, V> {
    // The map used to store the associations
    private final Map<K, Set<V>> _map = new HashMap<>();
    
    /**
     * Checks whether the given arguments are correct or not. If not, it throws an
     * IllegalArgumentException.
     *
     * @param o
     *            the argument to be checked
     * @param arg
     *            the name of the argument to be checked, to print it in the
     *            exception String.
     */
    private void checkArg(Object o, String arg) {
        if (o == null) {
            throw new IllegalArgumentException("Null argument '" + arg + "'.");
        }
    }
    
    /**
     * Adds a new value in the set associated to the given key.
     *
     * @param key
     *            the key used to access the map
     * @param value
     *            the value to be added
     */
    public void put(K key, V value) {
        checkArg(key, "key");
        checkArg(value, "value");
        
        Set<V> valSet = _map.get(key);
        
        if (valSet == null) {
            valSet = new HashSet<>(0);
            valSet.add(value);
            _map.put(key, valSet);
        } else {
            valSet.add(value);
        }
    }
    
    /**
     * Removes an association key-value from within the map.
     *
     * @param key
     *            the key used to access the map
     * @param value
     *            the value to be removed
     * @return true if the map was modified by this operation
     */
    public boolean remove(K key, V value) {
        Set<V> valSet = _map.get(key);
        
        if (valSet == null)
            return false;
        
        return valSet.remove(value);
    }
    
    /**
     * Removes all mappings associated with the given value within the map.
     *
     * @param value
     *            the value to be removed
     * @return true if the map was modified by this operation
     */
    public boolean removeAll(V value) {
        boolean contained = false;
        
        Set<K> keySet = _map.keySet();
        
        for (K key : keySet) {
            Set<V> valSet = _map.get(key);
            
            contained = valSet.remove(value) || contained;
        }
        
        return contained;
    }
    
    /**
     * Checks if there is at least one reference to the given value.
     *
     * @param value
     *            the value to be searched
     * @return true if the map contained at least one reference to the given value
     */
    public boolean contains(V value) {
        Set<K> keySet = _map.keySet();
        
        for (K key : keySet) {
            Set<V> valSet = _map.get(key);
            
            if(valSet.contains(value))
                return true;
        }
        
        return false;
    }
    
    /**
     * Gets the set of values associated with the given key.
     *
     * @param key
     *            the key used to access the map
     * @return the {@link Set} associated with the given key
     */
    public Set<V> get(K key) {
        return _map.get(key);
    }
    
}