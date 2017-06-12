package dictionary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class APIType extends APIElement implements Serializable {
	private static final long serialVersionUID = -5790551948759393843L;
	
	private APIPackageNode packageNode;
	private HashMap<Integer, APIField> fields = new HashMap<>();
	private HashMap<Integer, ArrayList<APIMethod>> methods = new HashMap<>();
	
	public APIType(Integer id, APIPackageNode packageNode) {
		super(id);
		this.packageNode = packageNode;
	}

	public HashMap<Integer, APIField> getFields() {
		return fields;
	}

	public HashMap<Integer, ArrayList<APIMethod>> getMethods() {
		return methods;
	}

	public APIField addField(String name, APIType fieldType) {
		Integer id = APIDictionary.getId(name);
		APIField field = getFields().get(id);
		if (field == null) {
			field = new APIField(id, this, fieldType);
			getFields().put(id, field);
		}
		return field;
	}

	public APIMethod addMethod(String name, APIType[] parameterTypes, APIType returnType) {
		String nameWithNumber = name + "(" + parameterTypes.length + ")";
		Integer id = APIDictionary.getId(nameWithNumber);
		ArrayList<APIMethod> ms = getMethods().get(id);
		if (ms == null) {
			ms = new ArrayList<>();
			getMethods().put(id, ms);
		}
		for (APIMethod method : ms)
			if (method.hasParameterTypes(parameterTypes))
				return method;
		APIMethod method = new APIMethod(id, this, parameterTypes, returnType);
		ms.add(method);
		return method;
	}
	
	@Override
	public String getFQN() {
		return this.packageNode.parent == null ? getName() : this.packageNode.getFQN() + "." + getName();
	}
	
	@Override
	public String toString() {
		return getFQN();
	}
}
