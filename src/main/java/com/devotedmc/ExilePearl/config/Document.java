package com.devotedmc.ExilePearl.config;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Document implements Map<String, Object> {
    private final LinkedHashMap<String, Object> documentAsMap;

    /**
     * Creates an empty Document instance.
     */
    public Document() {
        documentAsMap = new LinkedHashMap<String, Object>();
    }

    /**
     * Create a Document instance initialized with the given key/value pair.
     *
     * @param key   key
     * @param value value
     */
    public Document(final String key, final Object value) {
        documentAsMap = new LinkedHashMap<String, Object>();
        documentAsMap.put(key, value);
    }

    /**
     * Creates a Document instance initialized with the given map.
     *
     * @param map initial map
     */
    public Document(final Map<String, Object> map) {
        documentAsMap = new LinkedHashMap<String, Object>(map);
    }

    /**
     * Put the given key/value pair into this Document and return this.  Useful for chaining puts in a single expression, e.g.
     * <pre>
     * doc.append("a", 1).append("b", 2)}
     * </pre>
     * @param key   key
     * @param value value
     * @return this
     */
    public Document append(final String key, final Object value) {
    	String[] keys = key.split("\\.");
    	
    	if (keys.length == 1) {
    		documentAsMap.put(key, value);
    		return this;
    	}
    	
    	Document doc = this;
    	for(int i = 0; i < keys.length - 1; i++) {
    		doc = doc.getDocument(keys[i]);
    	}
    	
    	doc = doc.append(keys[keys.length - 1], value);
    	documentAsMap.put(keys[0], doc);
    	return this;
    }
    
    public Document append(final DataPair pair) {
    	return append(pair.getKey(), pair.getValue());
    }

    /**
     * Gets the value of the given key, casting it to the given {@code Class<T>}.  This is useful to avoid having casts in client code,
     * though the effect is the same.  So to get the value of a key that is of type String, you would write {@code String name =
     * doc.get("name", String.class)} instead of {@code String name = (String) doc.get("x") }.
     *
     * @param key   the key
     * @param clazz the non-null class to cast the value to
     * @param <T>   the type of the class
     * @return the value of the given key, or null if the instance does not contain this key.
     * @throws ClassCastException if the value of the given key is not of type T
     */
    public <T> T get(final Object key, final Class<T> clazz) {
        return clazz.cast(get(key));
    }

    /**
     * Gets the value of the given key as an Integer.
     *
     * @param key the key
     * @return the value as an integer, which may be null
     * @throws java.lang.ClassCastException if the value is not an integer
     */
    public Integer getInteger(final Object key) {
        return (Integer) get(key);
    }

    /**
     * Gets the value of the given key as a primitive int.
     *
     * @param key          the key
     * @param defaultValue what to return if the value is null
     * @return the value as an integer, which may be null
     * @throws java.lang.ClassCastException if the value is not an integer
     */
    public int getInteger(final Object key, final int defaultValue) {
        Object value = get(key);
        return value == null ? defaultValue : (Integer) value;
    }

    /**
     * Gets the value of the given key as a Long.
     *
     * @param key the key
     * @return the value as a long, which may be null
     * @throws java.lang.ClassCastException if the value is not an long
     */
    public Long getLong(final Object key) {
        return (Long) get(key);
    }

    /**
     * Gets the value of the given key as a primitive long.
     *
     * @param key          the key
     * @param defaultValue what to return if the value is null
     * @return the value as a long, which may be null
     * @throws java.lang.ClassCastException if the value is not a long
     */
    public Long getLong(final Object key, final long defaultValue) {
        Object value = get(key);
        return value == null ? defaultValue : (Long) value;
    }

    /**
     * Gets the value of the given key as a Double.
     *
     * @param key the key
     * @return the value as a double, which may be null
     * @throws java.lang.ClassCastException if the value is not an double
     */
    public Double getDouble(final Object key) {
        return (Double) get(key);
    }

    /**
     * Gets the value of the given key as a String.
     *
     * @param key the key
     * @return the value as a String, which may be null
     * @throws java.lang.ClassCastException if the value is not a String
     */
    public String getString(final Object key) {
        return (String) get(key);
    }
    
    public String getString(final DataPair pair) {
        return (String) get(pair.getKey());
    }

    /**
     * Gets the value of the given key as a String.
     *
     * @param key          the key
     * @param defaultValue what to return if the value is null
     * @return the value as a String, which may be null
     * @throws java.lang.ClassCastException if the value is not a String
     */
    public String getString(final Object key, final String defaultValue) {
        Object value = get(key);
        return value == null ? defaultValue : (String) value;
    }

    /**
     * Gets the value of the given key as a Boolean.
     *
     * @param key the key
     * @return the value as a double, which may be null
     * @throws java.lang.ClassCastException if the value is not an double
     */
    public Boolean getBoolean(final Object key) {
        return (Boolean) get(key);
    }

    /**
     * Gets the value of the given key as a primitive boolean.
     *
     * @param key          the key
     * @param defaultValue what to return if the value is null
     * @return the value as a double, which may be null
     * @throws java.lang.ClassCastException if the value is not an double
     */
    public boolean getBoolean(final Object key, final boolean defaultValue) {
        Object value = get(key);
        return value == null ? defaultValue : (Boolean) value;
    }

    /**
     * Gets the value of the given key as a Date.
     *
     * @param key the key
     * @return the value as a Date, which may be null
     * @throws java.lang.ClassCastException if the value is not a Date
     */
    public Date getDate(final Object key) {
        return (Date) get(key);
    }
    
    public Date getDate(final Object key, final Date defaultValue) {
        Object value = get(key);
        return value == null ? defaultValue : (Date) value;
    }
    
    @SuppressWarnings("unchecked")
	public List<String> getStringList(final Object key) {
        return (List<String>) get(key);
    }
    
    
    public boolean containsField(final Object key) {
    	Object value = get(key);
    	return value != null;
    }
    
    public boolean containsDocument(final Object key) {
    	Object value = get(key);
    	return value != null && value instanceof Document;
    }
    
    @SuppressWarnings("unchecked")
	public Document getDocument(final Object key) {
    	Object value = get(key);
    	return new Document((Map<String, Object>) value);
    }
    
    @SuppressWarnings("unchecked")
	public <T> T getRaw(final Object key) {
        return (T)documentAsMap.get(key);
    }
    
	public <T> Document setRaw(final String key, final T value) {
        documentAsMap.put(key, value);
        return this;
    }

    // Vanilla Map methods delegate to map field

    @Override
    public int size() {
        return documentAsMap.size();
    }

    @Override
    public boolean isEmpty() {
        return documentAsMap.isEmpty();
    }

    @Override
    public boolean containsValue(final Object value) {
        return documentAsMap.containsValue(value);
    }

    @Override
    public boolean containsKey(final Object key) {
    	return get(key) != null;
    }

    @Override
    public Object get(final Object key) {
    	if (!(key instanceof String)) {
    		return documentAsMap.get(key);
    	}
    	
    	String sKey = (String)key;
    	String[] keys = sKey.split("\\.");
    	
    	if (keys.length == 1) {
    		return documentAsMap.get(key);
    	}
    	
    	Document doc = this;
    	for(int i = 0; i < keys.length - 1; i++) {
    		doc = doc.getDocument(keys[i]);
    	}
    	
        return doc.get(keys[keys.length - 1]);
    }

    @Override
    public Object put(final String key, final Object value) {
    	append(key, value);
    	return null;
    }

    @Override
    public Object remove(final Object key) {
        return documentAsMap.remove(key);
    }

    @Override
    public void putAll(final Map<? extends String, ?> map) {
    	for(java.util.Map.Entry<? extends String, ?> e : map.entrySet()) {
    		append(e.getKey(), e.getValue());
    	}
    }

    @Override
    public void clear() {
        documentAsMap.clear();
    }

    @Override
    public Set<String> keySet() {
        return documentAsMap.keySet();
    }

    @Override
    public Collection<Object> values() {
        return documentAsMap.values();
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        return documentAsMap.entrySet();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Document document = (Document) o;

        if (!documentAsMap.equals(document.documentAsMap)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return documentAsMap.hashCode();
    }

    @Override
    public String toString() {
        return "Document{"
               + documentAsMap
               + '}';
    }
}