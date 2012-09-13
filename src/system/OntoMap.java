package system;

import java.util.concurrent.ConcurrentHashMap;

/**
 * For all Long keys, get( Long ) returns a non-null value.
 *
 * @author Pete Cappello
 */
final public class OntoMap<V> extends ConcurrentHashMap<Long, V>
{
    private Factory<V> factory;

    public OntoMap(Factory factory)
    {
        this(100,factory);
    }

    public OntoMap(int size, Factory factory) {
        super(size, 0.9f, 1);
        this.factory = factory;
    }
    
    public V get(Long key)
    {
        putIfAbsent(key, factory.make());
        return super.get(key);
    }

    public V remove(Long key)
    {
        return super.remove(key);
    }
}