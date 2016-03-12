//package
package org.liquidsql.util;

//Imports
import java.util.ArrayList;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Vector;

/**
 * <p>
 * <b>BucketMap:</b>
 * A bucket map is an shallow extension of the <b>java.util.HashMap</b>
 * with an extra ability to map a single key to multiple objects.
 * The key object must implement the methods <b>equals()</b>
 * and <b>hashCode()</b> and the value object can be any list object
 * that inherits from <b>java.util.List</b>
 * All methods found in <b>java.util.HashMap</b> can
 * be found in this class.
 * </p>
 * <br>
 * <p>Created: 18 April 2013</p>
 *
 * @author <b>Tsepo Maleka</b>
 * @param <K> - The bucket map key
 * @param <V> - The bucket map value mapped to the key
 *
 */
public class BucketMap<K,V> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    //Private variables
    /**
     * Stores the hash map
     */
    private TreeMap<K, List<V>> hashMap;
    
    /**
     * Determines if the user preferred another type of List
     * instead of ArrayList
     */
    private ListType listType;
    
    //Constructors
    /**
     * <p>
     * <b>Default Constructor</b>
     * Creates an instance of the BucketMap object.<p>
     */
    public BucketMap(){
        //set the hash map object to a new instance
        this.hashMap = new TreeMap<K, List<V>>();
        //set the default list type for get(...) to ArrayList
        this.listType = ListType.ArrayList;
    }
    
    /**
     * <p>
     * <b>Overloaded Constructor: </b>
     * Creates an instance of the BucketMap object.
     *
     * @param listType - enum type: The list type to use when the objects are returned in a list
     * when calling get(...) from this object instance. Available list types: </br>
     * 	<ul>
     * 			<li>ArrayList (default) <b>[java.util.ArrayList]</b></li>
     * 			<li>LinkedList <b>[java.util.LinkedList]</b> </li>
     * 			<li>Stack <b>[java.util.Stack]</b> </li>
     * 			<li>Vector <b>[java.util.Vector]</b> </li>
     * </ul>
     */
    public BucketMap(ListType listType){
        this();
        this.listType = listType;
    }
    
    //Common hash map methods
    
    /**
     * <p>Removes all of the mappings from this map.</p>
     */
    public void clear(){
        if (hashMap != null){
            if (hashMap.size() > 0)
                hashMap.clear();
        }
    }
    
    /**
     * <p>Returns a shallow copy of this HashMap instance:
     * the keys and values themselves are not cloned.</p>
     * @return
     */
    @Override
    public Object clone(){
        return hashMap.clone();
    }
    
    /**
     * <p>
     * Returns <b>true</b> if this map contains a mapping for the specified value.
     * If more than one instance of this value exists, this method will only
     * check the first occurrence in the buckets.
     * </p>
     * @param value - the value object to find
     * @return
     */
    public boolean containsValue(V value) {
        if (isEmpty())
            return false;
        
        List<V> list = null;
        boolean isFound = false;
        
        for (K key : hashMap.keySet()){
            list = hashMap.get(key);
            if (list.contains(value)){
                isFound = true;
                break;
            }
        }
        
        return isFound;
    }
    
    /**
     * <p>
     * Returns <b>true</b> if this map contains a mapping for the specified key.
     * NB: keys are unique.
     * </p>
     * @param key
     * @return
     */
    public boolean containsKey(K key){
        if (isEmpty())
            return false;
        
        return hashMap.containsKey(key);
    }
    
    /**
     * <p>
     * Returns a <b>java.util.Set</b> view of the mappings contained in this map.
     * </p>
     * @return
     */
    public Set<Map.Entry<K, List<V>>> entrySet() {
        if (isEmpty())
            return null;
        return hashMap.entrySet();
    }
    
    /**
     * <p>
     * Returns the list of all value to which the specified key is mapped,
     * or null if this map contains no mapping for the key.</p>
     * @param key
     * @return
     */
    public List<V> get(K key) {
        if (isEmpty())
            return null;
        return hashMap.get(key);
    }
    
    /**
     * <p>Returns <b>true</b> if this map contains no key-value mappings.</p>
     * @return
     */
    public boolean isEmpty() {
        return (hashMap.isEmpty());
    }
    
    /**
     * <p>
     * Returns a <b>java.util.Set</b>
     * view of the keys contained in this map.
     * </p>
     * @return
     */
    public Set<K> keySet() {
        if (isEmpty())
            return null;
        return hashMap.keySet();
    }
    
    /**
     * <p>
     * Associates the specified value with the specified key in this map.
     * </br>
     * <b>NB:</b> This method will not overwrite the existing value if the key
     * is already in the map - instead, it will map several values to
     * a single key - that is, a single key mapping to a bucket of objects
     * in a java.util.List
     * </p>
     * @param key
     * @param value
     * @return
     */
    public V put(K key, V value) {
        
        if (hashMap.containsKey(key)){
            List<V> arbritaryList = (List<V>)hashMap.get(key);
            
            if (!arbritaryList.contains(value)){
                arbritaryList.add(value);
                return value;
            }
        }
        
        else {
            List<V> arbritaryList = getPreferredListInstance();
            arbritaryList.add(value);
            hashMap.put(key, arbritaryList);
            return value;
        }
        
        return null;
    }
    
    /**
     * <p>Copies all of the mappings from the specified map to this map.</p>
     * @param map
     */
    public void putAll(Map<K,List<V>> map) {
        if (map != null){
            if (map instanceof HashMap)
                hashMap.putAll(map);
        }
    }
    
    /**
     * Removes the value from this map if present.
     * @param value
     * @return
     */
    public V removeValue(V value) {
        if (isEmpty())
            return null;
        
        V removedValue = null;
        
        for (K key : hashMap.keySet()){
            List<V> arbritaryList = hashMap.get(key);
            if (arbritaryList.contains(value)){
                removedValue = value;
                arbritaryList.remove(value);
                break;
            }
        }
        
        return removedValue;
    }
    
    /**
     * Removes the mapping for the specified key from this map if present.
     * @param key
     * @return
     */
    public List<V> removeKey(K key){
        if (isEmpty())
            return null;
        List<V> removedKey = null;
        
        if (hashMap.containsKey(key))
            removedKey = hashMap.remove(key);
        
        return removedKey;
    }
    
    /**
     * Returns the number of key-values mappings in this map.
     * @return
     */
    public int size() {
        return hashMap.size();
    }
    
    /**
     * Creates an instance of the preferred list
     * @return
     */
    protected List<V> getPreferredListInstance(){
        List<V> preferredList = null;
        
        if (listType == ListType.LinkedList)
            preferredList = new LinkedList<V>();
        else if (listType == ListType.Stack)
            preferredList = new Stack<V>();
        else if (listType == ListType.Vector)
            preferredList = new Vector<V>();
        else
            preferredList = new ArrayList<V>();
        
        return preferredList;
    }
    
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        for (K key : hashMap.keySet()){
            builder.append("[key: " + key.toString() + "] \n");
            builder.append("[values] [\n");
            for (V value: hashMap.get(key)){
                builder.append(value.toString() + "\n");
            }
            
            builder.append("]\n\n");
        }
        return builder.toString();
    }
    
    /**
     * Enumeration for list types
     */
    public enum ListType {
        ArrayList, LinkedList, Stack, Vector
    }
}