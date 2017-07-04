package dictionary;

import java.io.Serializable;

public class APIField extends APIElement implements Serializable {
	private static final long serialVersionUID = -722190730022687163L;
	
	private APIType type, returnType;
	
	public APIField(int id, APIType type, APIType returnType) {
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

	@Override
	public int hashCode() {
		return getFQN().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof APIField)
			return getFQN().equals(((APIField) obj).getFQN());
		return false;
	}
}
