package parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.Stack;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import utils.FileUtil;

public class ClassPathUtil {

	public static void getGradleDependencies(File file, String outPath) {
		String content = FileUtil.getFileContent(file.getAbsolutePath());
		Scanner sc = new Scanner(content);
		boolean inDependencies = false;
		HashMap<String, String> variableValue = new HashMap<>();
		while (sc.hasNextLine()) {
			String l = sc.nextLine().trim();
			String line = l;
			if (line.matches("dependencies[\\s]*\\{"))
				inDependencies = true;
			int index = line.indexOf('=');
			if (index > -1) {
				String left = line.substring(0, index).trim();
				String right = line.substring(index + 1).trim();
				if (!left.isEmpty() && !right.isEmpty()) {
					char ch = right.charAt(0);
					index = right.indexOf(ch, 1);
					if (index == right.length() - 1) {
						if (ch == '\'' || ch == '\"')
							right = right.substring(1, right.length()-1);
						if (right.toUpperCase().endsWith("-" + "SNAPSHOT"))
							right = right.substring(0, right.length() - ("-" + "SNAPSHOT").length()) + ".0";
						variableValue.put(left, right);
					}
				}
			} else {
				if (inDependencies) {
					if (line.startsWith("compile ") || line.startsWith("testCompile ") || line.startsWith("classpath ") || line.startsWith("[group")) {
						if (line.startsWith("[group")) {
							index = line.indexOf(']');
							if (index == -1)
								continue;
							line = line.substring(1, index);
						} else {
							index = line.indexOf(' ');
							line = line.substring(index).trim();
						}
						int vi = 0;
						while (true) {
							vi = line.indexOf('+', vi);
							if (vi == -1)
								break;
							line = line.substring(0, vi).trim() + "+" + line.substring(vi+1).trim();
							int j = vi + 1;
							while (j < line.length()) {
								char ch = line.charAt(j);
								if (!Character.isJavaIdentifierPart(ch))
									break;
								j++;
							}
							String var = line.substring(vi+1, j);
							if (variableValue.containsKey(var)) {
								String value = variableValue.get(var);
								char ch = line.charAt(vi-1);
								if (ch == '\'' || ch == '\"')
									line = line.substring(0, vi-1) + value + ch + line.substring(j);
							}
							vi++;
						}
						String[] values = getGradleDependencyInfo(line);
						if (values == null)
							continue;
						for (int i = 0; i < values.length; i++) {
							String v = values[i];
							vi = 0;
							while (true) {
								vi = v.indexOf('$', vi);
								if (vi == -1)
									break;
								int j = vi + 1;
								while (j < v.length()) {
									char ch = v.charAt(j);
									if (ch == '$' || !Character.isJavaIdentifierPart(ch))
										break;
									j++;
								}
								String var = v.substring(vi+1, j);
								if (variableValue.containsKey(var))
									v = v.replace("$" + var, variableValue.get(var));
								vi++;
							}
							values[i] = v;
						}
						if (values.length == 2)
							values = new String[]{values[0], values[1], "null"};
						if (values[0] == null || values[0].contains("$") || values[0].contains("@")
								|| values[1] == null || values[1].contains("$") || values[1].contains("@") 
								|| values[2] == null || values[2].contains("$") || values[2].contains("@")) {
//							System.err.println("Cannot download gradle dependency " + values[0] + ":" + values[1] + ":" + values[2]);
							continue;
						}
						values = strip(values);
						for (int i = 0; i < values.length; i++) {
							String v = values[i];
							if (variableValue.containsKey(v))
								values[i] = variableValue.get(v);
						}
						if (values != null && values.length == 3) {
							values[2] = values[2].replace('+', '0');
							String name = values[1] + "-" + values[2] + ".jar";
							String link = "http://central.maven.org/maven2/";
							link += values[0].replace('.', '/');
							link += "/" + values[1];
							link += "/" + values[2];
							link += "/" + name;
							try {
								getFile(outPath, name, link);
							} catch (IOException ex) {
								String prefix = "http://central.maven.org/maven2/";
								prefix += values[0].replace('.', '/');
								prefix += "/" + values[1] + "/";
								try {
									getFile(outPath, prefix, values);
								} catch (IOException e1) {
//									System.err.println("Cannot download gradle dependency " + link);
								}
							}
						}
					}
				}
			}
		}
		sc.close();
	}

	private static String[] strip(String[] values) {
		String[] vs = new String[values.length];
		for (int i = 0; i < vs.length; i++) {
			vs[i] = strip(values[i]);
		}
		return vs;
	}

	private static String strip(String s) {
		if (s == null)
			return "null";
		char ch = s.charAt(0);
		if (Character.isLetter(ch) || Character.isDigit(ch)) {
			if (s.toUpperCase().endsWith("-" + "SNAPSHOT"))
				s = s.substring(0, s.length() - ("-" + "SNAPSHOT").length()) + ".0";
			return s;
		}
		if (ch == '(' || ch == '\'' || ch =='\"')
			return strip(s.substring(1, s.length() - 1));
		return s;
	}

	private static String[] getGradleDependencyInfo(String line) {
		if (line.isEmpty())
			return null;
		if (line.endsWith("{"))
			return getGradleDependencyInfo(line.substring(0, line.length()-1).trim());
		String[] values = null;
		char ch = line.charAt(0);
		if (ch == '\'' || ch == '\"') {
			line = line.substring(1, line.length() - 1);
			values = line.split(":");
		} else if (line.startsWith("group")) {
			values = line.split(",");
			for (int i = 0; i < values.length; i++) {
				String v = values[i];
				v = v.substring(v.indexOf(':') + 1).trim();
				values[i] = v;
			}
		} else if (line.length() < 2)
			return null;
		else if (!Character.isLetter(ch))
			return getGradleDependencyInfo(line.substring(1, line.length()-1).trim());
		return values;
	}
	
	public static class PomFile {
		private String id;
		private PomFile parent;
		HashMap<String, String> properties = new HashMap<>();
		HashMap<String, String> managedDependencies = new HashMap<>();
		
		public PomFile(String id, String parent, 
				Properties properties, List<Dependency> managedDependencies, List<Repository> repos,
				HashSet<String> globalRepoLinks, HashMap<String, String> globalProperties, HashMap<String, String> globalManagedDependencies,
				Stack<PomFile> parentPomFiles) {
			this.id = id;
			if (!parentPomFiles.isEmpty())
				this.parent = parentPomFiles.peek();
			if (properties != null) {
				for (Entry<Object, Object> e : properties.entrySet()) {
					this.properties.put(e.getKey().toString(), e.getValue().toString());
					globalProperties.put(e.getKey().toString(), e.getValue().toString());
				}
			}
			if (managedDependencies != null)
				for (Dependency d : managedDependencies) {
					String v = d.getVersion();
					if (v == null || v.isEmpty())
						continue;
					if (v.startsWith("$")) {
						v = v.substring(1);
						if (v.startsWith("{") && v.endsWith("}"))
							v = v.substring(1, v.length()-1).trim();
						v = getPropertyValue(v);
						if (v != null) {
							if (v.startsWith("[")) {
								v = v.substring(1, v.length() - 1);
								int index = v.indexOf(",");
								if (index > -1)
									v = v.substring(0, index);
							}
						}
					}
					this.managedDependencies.put(d.getGroupId() + ":" + d.getArtifactId(), v);
					globalManagedDependencies.put(d.getGroupId() + ":" + d.getArtifactId(), v);
				}
			if (repos != null) {
				for (int i = 0; i < repos.size(); i++) {
					String url = repos.get(i).getUrl();
					if (url != null && !url.isEmpty())
						globalRepoLinks.add(url);
				}
			}
		}

		public void getDependencies(List<Dependency> dependencies, 
				HashSet<String> globalRepoLinks, HashMap<String, String> globalProperties, HashMap<String, String> globalManagedDependencies,
				String outPath) {
			for (Dependency dep : dependencies) {
				String[] values = new String[]{dep.getGroupId(), dep.getArtifactId(), dep.getVersion()};
				for (int i = 0; i < values.length; i++) {
					String v = values[i];
					if (v != null && !v.isEmpty()) {
						v = v.trim();
						char ch = v.charAt(0);
						if (ch =='$' || ch == '@') {
							v = v.substring(1);
							if (v.startsWith("{") && v.endsWith("}"))
								v = v.substring(1, v.length()-1).trim();
							String val = getPropertyValue(v);
							if (val == null)
								v = globalProperties.get(v);
							if (v != null) {
								if (v.startsWith("[")) {
									v = v.substring(1, v.length() - 1);
									int index = v.indexOf(",");
									if (index > -1)
										v = v.substring(0, index);
								}
								values[i] = v;
							}
						}
					} else if (i == 2) {
						v = getManagedDepedency(values[0] + ":" + values[1]);
						if (v == null)
							v = globalManagedDependencies.get(values[0] + ":" + values[1]);
						if (v != null)
							values[i] = v;
					}
				}
				if (values[0] == null || values[0].contains("$") || values[0].contains("@")
						|| values[1] == null || values[1].contains("$") || values[1].contains("@") 
						|| values[2] == null || values[2].contains("$") || values[2].contains("@")) {
//					System.err.println("Cannot download pom dependency " + values[0] + ":" + values[1] + ":" + values[2]);
					continue;
				}
				values = strip(values);
				values[2] = values[2].replace('+', '0');
				String name = values[1] + "-" + values[2] + ".jar";
				for (String link : globalRepoLinks) {
					link += values[0].replace('.', '/');
					link += "/" + values[1];
					link += "/" + values[2];
					link += "/" + name;
					try {
						getFile(outPath, name, link);
						break;
					} catch (IOException ex) {
					}
				}
				if (!(new File(outPath + "/" + name).exists())) {
					String prefix = "http://central.maven.org/maven2/";
					prefix += values[0].replace('.', '/');
					prefix += "/" + values[1] + "/";
					try {
						getFile(outPath, prefix, values);
						break;
					} catch (IOException e1) {
//						System.err.println("Cannot download pom dependency " + values[0] + ":" + values[1] + ":" + values[2]);
					}
				}
			}
		}

		private String getManagedDepedency(String name) {
			String v = this.managedDependencies.get(name);
			if (v != null)
				return v;
			if (this.parent != null) {
				return this.parent.getManagedDepedency(name);
			}
			return null;
		}

		private String getPropertyValue(String name) {
			String v = this.properties.get(name);
			if (v != null)
				return v;
			if (this.parent != null) {
				return this.parent.getPropertyValue(name);
			}
			return null;
		}
		
		@Override
		public String toString() {
			return this.id;
		}
	}

	public static void getPomDependencies(File file, String outPath,
			HashSet<String> globalRepoLinks, HashMap<String, String> globalProperties, HashMap<String, String> globalManagedDependencies,
			Stack<PomFile> parentPomFiles) {
		Reader reader = null;
		MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
		Model model = null;
		try {
			reader = new FileReader(file);
			try {
				model = xpp3Reader.read(reader);
			} catch (IOException e1) {
//				e1.printStackTrace();
				return;
			} catch (XmlPullParserException e1) {
//				e1.printStackTrace();
				return;
			}
		} catch (FileNotFoundException e) {
			return;
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e1) {
					return;
				}
		}
		PomFile pf = new PomFile(model.getId(), model.getParent() != null ? model.getParent().getId() : null,
						model.getProperties(),
						model.getDependencyManagement() != null ? model.getDependencyManagement().getDependencies() : null,
						model.getRepositories(),
						globalRepoLinks, globalProperties, globalManagedDependencies,
						parentPomFiles);
		parentPomFiles.push(pf);
		pf.getDependencies(model.getDependencies(), globalRepoLinks, globalProperties, globalManagedDependencies, outPath);
	}

	private static void getFile(String outPath, String prefix, String[] values) throws IOException {
		String lib = values[1];
		File dir = new File(outPath);
		if (dir.exists()) {
			for (File file : dir.listFiles()) {
				String name = file.getName();
				if (name.startsWith(lib) && name.endsWith(".jar"))
					return;
			}
		}
		
		InputStream is = null;
	    try {
	    	URL url = new URL(prefix);
	        is = url.openStream();
	        BufferedReader br = new BufferedReader(new InputStreamReader(is));
		    String line, last = null;
	        while ((line = br.readLine()) != null) {
	        	line = line.trim();
	        	if (line.startsWith("<a ")) {
	        		int index = "<a href=".length();
	        		char ch = line.charAt(index);
	        		if (ch == '\'' || ch == '\"')
	        			index++;
	        		String v = line;
        			if (ch == '\'' || ch == '\"')
        				v = line.substring(index, line.indexOf(ch, index));
        			else {
        				int e = line.indexOf(' ', index);
        				if (e == -1)
        					e = line.indexOf('>', index);
        				v = line.substring(index, e);
        			}
        			String[] parts = v.split("\\.");
	        		try {
	        			Integer.parseInt(parts[0]);
	        			last = v;
	        		} catch (Exception e) {
	        			if (last != null)
	        				break;
	        		}
	        	}
	        }
	        if (last != null) {
	        	if (last.endsWith("/"))
	        		last = last.substring(0, last.length() - 1);
				String name = values[1] + "-" + last + ".jar";
	        	String link = prefix + last;
				link += "/" + name;
				getFile(outPath, name, link);
	        }
	    } finally {
	        try {
	            if (is != null) is.close();
	        } catch (IOException ioe) {}
	    }
	}

	public static void getFile(String outPath, String name, String link) throws IOException {
		if (new File(outPath + "/" + name).exists())
			return;
		URL url = new URL(link);
		ReadableByteChannel rbc = Channels.newChannel(url.openStream());
		File dir = new File(outPath);
		if (!dir.exists())
			dir.mkdirs();
		FileOutputStream fos = new FileOutputStream(dir.getAbsolutePath() + "/" + name);
		fos.getChannel().transferFrom(rbc, 0, Integer.MAX_VALUE);
		fos.flush();
		fos.close();
	}

}
