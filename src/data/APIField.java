package data;

import java.io.Serializable;

public class APIField implements Serializable {
	private static final long serialVersionUID = -722190730022687163L;
	
	String name;
	APIType type, returnType;
	
	public APIField(String name, APIType type, APIType returnType) {
		this.name = name;
		this.type = type;
		this.returnType = returnType;
	}
	
	public String getFQN() {
		return this.type.getFQN() + "." + name;
	}
	
	@Override
	public String toString() {
		return getFQN();
	}
}
