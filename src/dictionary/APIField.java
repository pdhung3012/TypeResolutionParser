package dictionary;

import java.io.Serializable;

public class APIField extends APIElement implements Serializable {
	private static final long serialVersionUID = -722190730022687163L;
	
	private APIType type, returnType;
	
	public APIField(Integer id, APIType type, APIType returnType) {
		super(id);
		this.type = type;
		this.returnType = returnType;
	}
	
	public APIType getType() {
		return type;
	}

	public APIType getReturnType() {
		return returnType;
	}

	@Override
	public String getFQN() {
		return this.type.getFQN() + "." + getName();
	}
	
	@Override
	public String toString() {
		return getFQN();
	}
}
