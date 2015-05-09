package priv.bajdcc.util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 哈希表
 * <p>
 * T->id,V=list,id= list indexof T
 * </p>
 * 
 * @author bajdcc
 */
public class HashListMapEx<K, V> {

	public HashMap<K, V> map = new HashMap<K, V>();

	public ArrayList<V> list = new ArrayList<V>();

	public boolean contains(K k) {
		return map.containsKey(k);
	}

	public void add(K k, V v) {
		if (map.put(k, v) == null) {
			list.add(v);
		}
	}

	public V get(K k) {
		return map.get(k);
	}

	public int indexOf(V v) {
		return list.indexOf(v);
	}
}