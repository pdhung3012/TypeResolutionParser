package parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Scanner;

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
							right = right.substring(0, right.length() - ("-" + "SNAPSHOT").length());
						if (right.toUpperCase().endsWith("-" + "SN"))
							right = right.substring(0, right.length() - ("-" + "SN").length());
						variableValue.put(left, right);
					}
				}
			} else {
				if (inDependencies) {
					if (line.startsWith("compile ") || line.startsWith("testCompile ") || line.startsWith("classpath ")) {
						index = line.indexOf(' ');
						line = line.substring(index).trim();
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
									System.err.println("Cannot download dependency " + link);
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
		char ch = s.charAt(0);
		if (Character.isLetter(ch) || Character.isDigit(ch)) {
			if (s.toUpperCase().endsWith("-" + "SNAPSHOT"))
				s = s.substring(0, s.length() - ("-" + "SNAPSHOT").length());
			if (s.toUpperCase().endsWith("-" + "SN"))
				s = s.substring(0, s.length() - ("-" + "SN").length());
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
		} else if (!Character.isLetter(ch))
			return getGradleDependencyInfo(line.substring(1, line.length()-1).trim());
		return values;
	}

	public static void getPomDependencies(File file, String outPath) {
		String content = FileUtil.getFileContent(file.getAbsolutePath());
		HashMap<String, String> variableValue = readVariableValue(content);
		int s = content.indexOf("<dependencies>");
		if (s == -1)
			return;
		s += "<dependencies>".length();
		int e = content.indexOf("</dependencies>");
		if (e == -1)
			return;
		content = content.substring(s, e);
		e = 0;
		while (true) {
			int ds = content.indexOf("<dependency>", e);
			if (ds == -1)
				break;
			ds += "<dependency>".length();
			int de = content.indexOf("</dependency>", ds);
			String[] values = read(content.substring(ds, de), new String[]{"groupId", "artifactId", "version"});
			if (values == null) {
				e = de + "</dependency>".length();
				continue;
			}
			for (int i = 0; i < values.length; i++) {
				String v = values[i].trim();
				if (v.startsWith("$")) {
					v = v.substring(1);
					if (v.startsWith("{") && v.endsWith("}"))
						v = v.substring(1, v.length()-1).trim();
					if (variableValue.containsKey(v)) {
						v = variableValue.get(v);
						if (v.startsWith("[")) {
							v = v.substring(1, v.length() - 1);
							int index = v.indexOf(",");
							if (index > -1)
								v = v.substring(0, index);
						}
						values[i] = v;
					}
				}
			}
			values = strip(values);
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
					System.err.println("Cannot download dependency " + link);
				}
			}
			e = de + "</dependency>".length();
		}
	}

	private static HashMap<String, String> readVariableValue(String content) {
		HashMap<String, String> varValue = new HashMap<>();
		int s = 0;
		while (true) {
			s = content.indexOf('<', s);
			if (s == -1)
				break;
			int e = content.indexOf('>', s);
			String tag = content.substring(s+1, e);
			if (tag.startsWith("/")) {
				s = e + 1;
				continue;
			}
			if (tag.startsWith("!--")) {
				e = content.indexOf("-->", e);
				if (e == -1)
					return varValue;
				s = e + 3;
			} else if (tag.endsWith("/")) {
				s = e + 1;
			} else if (tag.contains(" ")) {
				s = e;
			} else {
				s = e + 1;
				e = content.indexOf("</" + tag + ">", s);
				if (e == -1)
					return varValue;
				String value = content.substring(s, e);
				if (!value.contains("<") && !value.contains(">")) {
					varValue.put(tag, value);
					s = e + tag.length() + 3;
				}
			}
		}
		return varValue;
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

	private static String[] read(String content, String[] keys) {
		String[] values = new String[keys.length];
		for (int i = 0; i < keys.length; i++) {
			String ss = "<" + keys[i] + ">", es = "</" + keys[i] + ">";
			int si = content.indexOf(ss) + ss.length();
			int ei = content.indexOf(es);
			if (ei == -1) {
				if (i < 2)
					return null;
				values[i] = "null";
			} else
				values[i] = content.substring(si, ei).trim();
		}
		return values;
	}

}
