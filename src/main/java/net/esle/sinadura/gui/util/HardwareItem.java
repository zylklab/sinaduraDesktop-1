package net.esle.sinadura.gui.util;

public class HardwareItem {

	private String key;
	private String name;
	private String path;
	private String so;
	
	public HardwareItem(String key, String name, String path, String so) {
		super();
		this.key = key;
		this.name = name;
		this.path = path;
		this.so = so;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getSo() {
		return so;
	}
	public void setSo(String so) {
		this.so = so;
	}
}