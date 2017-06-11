package dictionary;

import java.io.Serializable;

public class APIField extends APIElement implements Serializable {
	private static final long serialVersionUID = -722190730022687163L;
	
	APIType type, returnType;
	
	public APIField(String name, APIType type, APIType returnType) {
		this.name = name;
		this.type = type;
		this.returnType = returnType;
	}
	
	@Override
	public String getFQN() {
		return this.type.getFQN() + "." + name;
	}
	
	@Override
	public String toString() {
		return getFQN();
	}
}
