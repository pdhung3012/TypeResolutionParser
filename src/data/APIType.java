package data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class APIType implements Serializable {
	private static final long serialVersionUID = -5790551948759393843L;
	
	String name;
	APIPackageNode packageNode;
	HashMap<String, APIField> fields = new HashMap<>();
	HashMap<String, ArrayList<APIMethod>> methods = new HashMap<>();
	
	public APIType(String name, APIPackageNode packageNode) {
		this.name = name;
		this.packageNode = packageNode;
	}

	public APIField addField(String name, APIType fieldType) {
		APIField field = fields.get(name);
		if (field == null) {
			field = new APIField(name, this, fieldType);
			fields.put(name, field);
		}
		return field;
	}

	public APIMethod addMethod(String name, APIType[] parameterTypes, APIType returnType) {
		String nameWithNumber = name + "(" + parameterTypes.length + ")";
		ArrayList<APIMethod> ms = methods.get(nameWithNumber);
		if (ms == null) {
			ms = new ArrayList<>();
			methods.put(nameWithNumber, ms);
		}
		for (APIMethod method : ms)
			if (Arrays.equals(method.parameterTypes, parameterTypes))
				return method;
		APIMethod method = new APIMethod(name, this, parameterTypes, returnType);
		ms.add(method);
		return method;
	}
	
	public String getFQN() {
		return this.packageNode.parent == null ? name : this.packageNode.getFQN() + "." + name;
	}
	
	@Override
	public String toString() {
		return getFQN();
	}
}
