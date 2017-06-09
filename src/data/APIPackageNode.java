package data;

import java.io.Serializable;
import java.util.HashMap;

public class APIPackageNode implements Serializable {
	private static final long serialVersionUID = -654414935534276897L;
	
	String name;
	APIPackageNode parent;
	HashMap<String, APIPackageNode> subPackages = new HashMap<>();
	HashMap<String, APIType> types = new HashMap<>();
	
	public APIPackageNode(String name, APIPackageNode parent) {
		this.name = name;
		this.parent = parent;
	}
	
	public APIPackageNode addSubPackage(String name) {
		APIPackageNode sub = subPackages.get(name);
		if (sub == null) {
			sub = new APIPackageNode(name, this);
			subPackages.put(name, sub);
		}
		return sub;
	}
	
	public APIType addType(String name) {
		APIType type = types.get(name);
		if (type == null) {
			type = new APIType(name, this);
			types.put(name, type);
		}
		return type;
	}

	public String getFQN() {
		return this.parent == null || isTop() ? name : this.parent.getFQN() + "." + name;
	}
	
	private boolean isTop() {
		return this.parent != null && this.parent.name.isEmpty();
	}

	@Override
	public String toString() {
		return getFQN();
	}
}
