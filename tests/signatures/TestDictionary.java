package signatures;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;

import dictionary.APIDictionary;
import dictionary.APIElement;
import dictionary.APIField;
import dictionary.APIMethod;
import dictionary.APIType;

public class TestDictionary {
    
    @Test
    public void test() throws Exception {
		File in = new File("T:/type-resolution");
		long beforeUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		APIDictionary dict = new APIDictionary();
		dict.build(in, "G:/github/repos-10stars-100commits.csv", 0);
		long afterUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		System.out.println("Memory usage: " + (afterUsedMem - beforeUsedMem) / 1000 / 1000);
		System.out.println("Types: " + dict.getNumOfTypes());
		System.out.println("Methods: " + dict.getNumOfMethods());
		System.out.println("Fields: " + dict.getNumOfFields());
		
		ArrayList<APIElement> l = new ArrayList<>();
		System.out.println(l = new ArrayList<APIElement>(dict.getTypesByName("String")));
		Assert.assertThat(l, new APIElementListMatcher<ArrayList<APIElement>>("java.lang.String"));
		System.out.println(l = new ArrayList<APIElement>(dict.getMethodsByName("substring(1)")));
		Assert.assertThat(l, new APIElementListMatcher<ArrayList<APIElement>>("java.lang.String.substring(int,)"));
		System.out.println(l = new ArrayList<APIElement>(dict.getFieldsByName("MAX_VALUE")));
		Assert.assertThat(l, new APIElementListMatcher<ArrayList<APIElement>>("java.lang.Integer.MAX_VALUE"));
	
		Scanner scan = new Scanner(System.in);
		while (true) {
			String text = scan.nextLine();
			String[] parts = text.split("\\s");
			if (parts.length == 2) {
				if (parts[0].equals("t"))
					System.out.println(new ArrayList<APIType>(dict.getTypesByName(parts[1])));
				else if (parts[0].equals("m"))
					System.out.println(new ArrayList<APIMethod>(dict.getMethodsByName(parts[1])));
				else if (parts[0].equals("f"))
					System.out.println(new ArrayList<APIField>(dict.getFieldsByName(parts[1])));
			}
			if (text.isEmpty())
				break;
		}
		scan.close();
    }
    
    @Test
    public void test1() throws Exception {
		File in = new File("T:/type-resolution");
		long beforeUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		APIDictionary dict = new APIDictionary();
		dict.build(in, "G:/github/repos-10stars-100commits.csv", 5000);
		long afterUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		System.out.println("Memory usage: " + (afterUsedMem - beforeUsedMem) / 1000 / 1000);
		System.out.println("Types: " + dict.getNumOfTypes());
		System.out.println("Methods: " + dict.getNumOfMethods());
		System.out.println("Fields: " + dict.getNumOfFields());
		
		ArrayList<APIElement> l = new ArrayList<>();
		System.out.println(l = new ArrayList<APIElement>(dict.getTypesByName("String")));
		Assert.assertThat(l, new APIElementListMatcher<ArrayList<APIElement>>("java.lang.String"));
		System.out.println(l = new ArrayList<APIElement>(dict.getMethodsByName("substring(1)")));
		Assert.assertThat(l, new APIElementListMatcher<ArrayList<APIElement>>("java.lang.String.substring(int,)"));
		System.out.println(l = new ArrayList<APIElement>(dict.getFieldsByName("MAX_VALUE")));
		Assert.assertThat(l, new APIElementListMatcher<ArrayList<APIElement>>("java.lang.Integer.MAX_VALUE"));
    }

    private static class APIElementListMatcher<T> extends BaseMatcher<T> {
    	private String name;
    	
    	public APIElementListMatcher(String name) {
    		this.name = name;
		}

		@Override
		public void describeTo(Description description) {
            description.appendText("contains " + name);
		}

		@Override
		public boolean matches(Object item) {
			if (item instanceof List) {
				List<?> l = (List<?>) item;
				for (Object e : l) {
					if (e instanceof APIElement) {
						APIElement api = (APIElement) e;
						if (api.getFQN().equals(name))
							return true;
					}
				}
			}
			return false;
		}
    }
}
