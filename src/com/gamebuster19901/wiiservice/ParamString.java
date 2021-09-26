package com.gamebuster19901.wiiservice;

import java.util.ArrayList;

/**
 * <p>PARAM-STRING is a simple transfer protocol to transfer named parameters and their values.
 * <p>The data is sent as an ASCII string using the following format:
 * 
 * <p>{@code \NAME1\VALUE1\NAME2\VALUE2\NAME3\VALUE3\...\FINAL\}
 * 
 * <p>It is <b>highly recommended</b> to read the Full documentation of this protocol, which can be found
 * <a href="http://wiki.tockdom.com/wiki/MKWii_Network_Protocol/PARAM-STRING">here.</a>
 */
public class ParamString {

	private final StringBuilder string = new StringBuilder();
	
	public ParamString() {};
	
	/**
	 * Adds a parameter with the specified name, and no value.
	 * 
	 * @param name the name of the parameter to add
	 * @return this paramString, for method chaining.
	 */
	public ParamString add(String name) {
		name = replaceSlashes(name);
		slice();
		string.append(name);
		slice();
		return this;
	}
	
	/**
	 * Adds a parameter with the specified name and value.
	 * 
	 * @param name the name of the parameter to add
	 * @param value the value of the specifiied parameter
	 * @return this paramString, for method chaining.
	 */
	public ParamString add(String name, Object value) {
		name = replaceSlashes(name);
		value = replaceSlashes(value.toString());
		slice();
		string.append(name);
		slice();
		string.append(value);
		return this;
	}
	
	/**
	 * Adds a final parameter. This is a convenience method, and functions exactly as if you
	 * called {@code add("final")}
	 * 
	 * @return this paramString, for method chaining.
	 */
	public ParamString finall() { //final is a java reserved word :/
		return add("final");
	}
	
	/**
	 * @return a list of {@link Pair Pairs} that represent this ParamString.
	 */
	public ArrayList<Pair<String, String>> toPairs() {
		ArrayList<Pair<String, String>> ret = new ArrayList<Pair<String, String>>();
		String[] data = toString().split("\\\\");
		for(int i = 0; i < data.length; i = i + 2) {
			String key = data[i];
			String value = null;
			if(i + 1 < data.length) {
				value = data[i + 1];
			}
			ret.add(new Pair<String, String>(key, value));
		}
		return ret;
	}
	
	/**
	 * Converts a String into a ParamString.
	 * If the String does not represent an valid ParamString, the behavior of this class is undefined.
	 * 
	 * @param paramString the string to convert
	 */
	public static ParamString fromString(String paramString) {
		ParamString ret = new ParamString();
		ret.string.append(paramString);
		return ret;
	}
	
	/**
	 * Returns the raw representation of this ParamString
	 */
	@Override
	public String toString() {
		return string.toString();
	}
	
	@Override
	public int hashCode() {
		return string.toString().hashCode();
	}
	
	private void slice() {
		string.append('\\');
	}
	
	private String replaceSlashes(String string) {
		if(string.contains("/")) {
			string = string.replace("/", "/1");
		}
		if(string.contains("\\")) {
			string = string.replace("\\", "/2");
		}
		
		return string;
	}
	
	/**
	 * A simple key-value pair.
	 *
	 * @param <K> Key type
	 * @param <V> Value type
	 */
	public static final class Pair<K, V> {
		final K key;
		final V value;
		
		private Pair(K key, V value) {
			this.key = key;
			this.value = value;
		}
		
		public K getKey() {
			return key;
		}
		
		public V getValue() {
			return value;
		}
	}
	
}
