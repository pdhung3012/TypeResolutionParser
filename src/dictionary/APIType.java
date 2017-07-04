package dictionary;

import java.io.Serializable;
import java.util.ArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;

public class APIType extends APIElement implements Serializable {
	private static final long serialVersionUID = -5790551948759393843L;
	
	private APIPackageNode packageNode;
	private TIntObjectHashMap<APIField> fields = new TIntObjectHashMap<>();
	private TIntObjectHashMap<ArrayList<APIMethod>> methods = new TIntObjectHashMap<>();
	
	public APIType(int id, APIPackageNode packageNode) {
		super(id);
		this.packageNode = packageNode;
	}

	public TIntObjectHashMap<APIField> getFields() {
		return fields;
	}

	public TIntObjectHashMap<ArrayList<APIMethod>> getMethods() {
		return methods;
	}
	
	public APIField getField(String name) {
		return fields.get(APIDictionary.getId(name));
	}
	
	public ArrayList<APIMethod> getMethods(String name) {
		return methods.get(APIDictionary.getId(name));
	}

	public APIField addField(String name, APIType fieldType) {
		int id = APIDictionary.getId(name);
		APIField field = fields.get(id);
		if (field == null) {
			field = new APIField(id, this, fieldType);
			fields.put(id, field);
		}
		return field;
	}

	public APIMethod addMethod(String name, APIType[] parameterTypes, APIType returnType) {
		String nameWithNumber = name + "(" + parameterTypes.length + ")";
		int id = APIDictionary.getId(nameWithNumber);
		ArrayList<APIMethod> ms = methods.get(id);
		if (ms == null) {
			ms = new ArrayList<>();
			methods.put(id, ms);
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

	@Override
	public int hashCode() {
		return getFQN().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof APIType)
			return getFQN().equals(((APIType) obj).getFQN());
		return false;
	}
}
