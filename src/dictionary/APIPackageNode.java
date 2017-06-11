package dictionary;

import java.io.Serializable;
import java.util.HashMap;

public class APIPackageNode extends APIElement implements Serializable {
	private static final long serialVersionUID = -654414935534276897L;
	
	APIPackageNode parent;
	HashMap<Integer, APIPackageNode> subPackages = new HashMap<>();
	HashMap<Integer, APIType> types = new HashMap<>();
	
	public APIPackageNode(Integer id, APIPackageNode parent) {
		super(id);
		this.parent = parent;
	}
	
	public APIPackageNode addSubPackage(String name) {
		Integer id = APIDictionary.getId(name);
		APIPackageNode sub = subPackages.get(id);
		if (sub == null) {
			sub = new APIPackageNode(id, this);
			subPackages.put(id, sub);
		}
		return sub;
	}
	
	public APIType addType(String name) {
		Integer id = APIDictionary.getId(name);
		APIType type = types.get(id);
		if (type == null) {
			type = new APIType(id, this);
			types.put(id, type);
		}
		return type;
	}

	@Override
	public String getFQN() {
		return this.parent == null || isTop() ? getName() : this.parent.getFQN() + "." + getName();
	}
	
	private boolean isTop() {
		return this.parent != null && this.parent.getName().isEmpty();
	}

	@Override
	public String toString() {
		return getFQN();
	}
}
