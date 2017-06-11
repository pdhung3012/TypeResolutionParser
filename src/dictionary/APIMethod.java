package dictionary;

import java.io.Serializable;
import java.util.Arrays;

public class APIMethod extends APIElement implements Serializable {
	private static final long serialVersionUID = -2427857003057367443L;
	
	private APIType type, returnType;
	private APIType[] parameterTypes;
	
	public APIMethod(Integer id, APIType type, APIType[] parameterTypes, APIType returnType) {
		super(id);
		this.type = type;
		this.parameterTypes = parameterTypes;
		this.returnType = returnType;
	}
	
	@Override
	public String getFQN() {
		return this.type.getFQN() + "." + getName() + "(" + getParameterTypesString() + ")";
	}
	
	private String getParameterTypesString() {
		StringBuilder sb = new StringBuilder();
		for (APIType t : parameterTypes)
			sb.append(t.getFQN() + ",");
		return sb.toString();
	}

	public boolean hasParameterTypes(APIType[] parameterTypes) {
		return Arrays.equals(this.parameterTypes, parameterTypes);
	}

	@Override
	public String toString() {
		return getFQN();
	}
}
