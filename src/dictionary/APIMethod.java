package dictionary;

import java.io.Serializable;

public class APIMethod extends APIElement implements Serializable {
	private static final long serialVersionUID = -2427857003057367443L;
	
	APIType type, returnType;
	APIType[] parameterTypes;
	
	public APIMethod(String name, APIType type, APIType[] parameterTypes, APIType returnType) {
		this.name = name;
		this.type = type;
		this.parameterTypes = parameterTypes;
		this.returnType = returnType;
	}
	
	@Override
	public String getFQN() {
		return this.type.getFQN() + "." + name + "(" + getParameterTypesString() + ")";
	}
	
	private String getParameterTypesString() {
		StringBuilder sb = new StringBuilder();
		for (APIType t : parameterTypes)
			sb.append(t.getFQN() + ",");
		return sb.toString();
	}

	@Override
	public String toString() {
		return getFQN();
	}
}
