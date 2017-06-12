package dictionary;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import utils.FileUtil;
import utils.NotifyingBlockingThreadPoolExecutor;

public class ExtractEntitySignature {
	private static final int THREAD_POOL_SIZE = 8;

	private static final Callable<Boolean> blockingTimeoutCallback = new Callable<Boolean>() {
		@Override
		public Boolean call() throws Exception {
			return true; // keep waiting
		}
	};
	private static NotifyingBlockingThreadPoolExecutor pool = new NotifyingBlockingThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 15, TimeUnit.SECONDS, 200, TimeUnit.MILLISECONDS, blockingTimeoutCallback);
	
	private static final AtomicInteger numOfJars = new AtomicInteger(0), numOfProjects = new AtomicInteger(0), numOfTypes = new AtomicInteger(0), numOfMethods = new AtomicInteger(0), numOfFields = new AtomicInteger(0);

	protected static final String OUTPUT_DIR = "T:/type-resolution";
	
	private final HashSet<String> types = new HashSet<>(), methods = new HashSet<>(), fields = new HashSet<>();

	public static void main(String[] args) {
		extractFromJars();
		extractFromSource();
		
		System.out.println("Jars: " + numOfJars);
		System.out.println("Projects: " + numOfProjects);
		System.out.println("Types: " + numOfTypes);
		System.out.println("Methods: " + numOfMethods);
		System.out.println("Fields: " + numOfFields);
	}

	public static void extractFromJars() {
		String jrePath = System.getProperty("java.home") + "/lib";
		File dir = new File(jrePath);
		extractFromJar(dir);
		
//		dir = new File("D:/eclipse/plugins");
//		extract(dir);
	}

	private static void extractFromJar(final File file) {
		if (file.isDirectory()) {
			for (File sub : file.listFiles())
				extractFromJar(sub);
		} else if (file.getName().endsWith(".jar")) {
			numOfJars.incrementAndGet();
			final File out = new File(OUTPUT_DIR);
			if (new File(out, file.getName() + "-types").exists())
				return;
			final String jarFilePath = file.getAbsolutePath();
			System.out.println("Parsing jar file " + jarFilePath);
			pool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						ExtractEntitySignature ees = new ExtractEntitySignature();
						ees.extractFromJarFile(jarFilePath);
						
						System.out.println("Jars: " + numOfJars);
						System.out.println("Projects: " + numOfProjects);
						System.out.println("Types: " + numOfTypes);
						System.out.println("Methods: " + numOfMethods);
						System.out.println("Fields: " + numOfFields);
						
						FileUtil.writeToFile(new File(out, file.getName() + "-types").getAbsolutePath(), ees.types);
						FileUtil.writeToFile(new File(out, file.getName() + "-methods").getAbsolutePath(), ees.methods);
						FileUtil.writeToFile(new File(out, file.getName() + "-fields").getAbsolutePath(), ees.fields);
					} catch (Throwable t) {
						System.err.println("Error in parsing jar file " + jarFilePath);
						t.printStackTrace();
						System.err.println(t.getMessage());
					}
				}
			});
		}
	}

	public void extractFromJarFile(final String jarFilePath) throws IOException {
		JarFile jarFile = new JarFile(jarFilePath);
		Enumeration<JarEntry> entries = jarFile.entries();
		while(entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			if(entry.getName().endsWith(".class")) {
				try {
					ClassParser parser = new ClassParser(jarFilePath, entry.getName());
					JavaClass jc = parser.parse();
					String pn = jc.getPackageName();
					if (!jc.isAnonymous() && pn != null && !pn.isEmpty()) {
						String className = jc.getClassName();
//								className = className.replace('$', '.');
						types.add(className);
						numOfTypes.incrementAndGet();
						for (Field field : jc.getFields())
							extract(field, className);
						for (Method method : jc.getMethods())
							extract(method, className);
					}
				} catch (IOException | ClassFormatException e) {
//							System.err.println("Error in parsing class file: " + entry.getName());
//							System.err.println(e.getMessage());
				}
			}
		}
		jarFile.close();
	}

	private static void extractFromSource() {
		File dir = new File("G:/github/repos-5stars-50commits");
		extractFromSource(dir);
	}

	private static void extractFromSource(final File dir) {
		if (dir.isDirectory()) {
			if (new File(dir, ".git").exists()) {
				numOfProjects.incrementAndGet();
				System.out.println("Projects: " + numOfProjects);
				final File out = new File(OUTPUT_DIR);
				if (new File(out, dir.getParentFile().getName() + "___" + dir.getName() + "-types").exists())
					return;
				System.out.println("Parsing project " + dir.getAbsolutePath());
				pool.execute(new Runnable() {
					@Override
					public void run() {
						try {
							ExtractEntitySignature ees = new ExtractEntitySignature();
							ees.extractFromSourceProject(dir);
							
							System.out.println("Jars: " + numOfJars);
							System.out.println("Projects: " + numOfProjects);
							System.out.println("Types: " + numOfTypes);
							System.out.println("Methods: " + numOfMethods);
							System.out.println("Fields: " + numOfFields);
							
							FileUtil.writeToFile(new File(out, dir.getParentFile().getName() + "___" + dir.getName() + "-types").getAbsolutePath(), ees.types);
							FileUtil.writeToFile(new File(out, dir.getParentFile().getName() + "___" + dir.getName() + "-methods").getAbsolutePath(), ees.methods);
							FileUtil.writeToFile(new File(out, dir.getParentFile().getName() + "___" + dir.getName() + "-fields").getAbsolutePath(), ees.fields);
						} catch (Throwable t) {
							System.err.println("Error in parsing project " + dir.getAbsolutePath());
							t.printStackTrace();
							System.err.println(t.getMessage());
						}
					}
				});
			} else
				for (File sub : dir.listFiles())
					extractFromSource(sub);
		}
	}

	private void extractFromSourceProject(File dir) {
		ArrayList<File> files = FileUtil.getPaths(dir);
		String[] paths = new String[files.size()];
		for (int i = 0; i < files.size(); i++) {
			paths[i] = files.get(i).getAbsolutePath();
		}
		final HashSet<CompilationUnit> cus = new HashSet<>();
		FileASTRequestor r = new FileASTRequestor() {
			@Override
			public void acceptAST(String sourceFilePath, CompilationUnit cu) {
				cus.add(cu);
			}
		};
		@SuppressWarnings("rawtypes")
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setCompilerOptions(options);
		parser.setEnvironment(new String[0], new String[]{}, new String[]{}, true);
		parser.setIgnoreMethodBodies(true);
		parser.setResolveBindings(true);
		parser.createASTs(paths, null, new String[0], r, null);
		for (CompilationUnit cu : cus)
			for (int i = 0 ; i < cu.types().size(); i++)
				extract((AbstractTypeDeclaration) cu.types().get(i), cu.getPackage() == null ? "" : cu.getPackage().getName().getFullyQualifiedName() + ".");
	}

	private void extract(AbstractTypeDeclaration type, String prefix) {
		ITypeBinding tb = type.resolveBinding();
		if (tb == null)
			return;
		tb = tb.getTypeDeclaration();
		if (tb == null)
			return;
		if (tb.isAnonymous() || tb.isLocal())
			return;
		String fqn = getQualifiedName(tb);
		if (tb.isNested())
			fqn = getQualifiedName(tb.getDeclaringClass().getTypeDeclaration()) + "$" + tb.getName();
		types.add(fqn);
		numOfTypes.incrementAndGet();
		for (int i = 0; i < type.bodyDeclarations().size(); i++) {
			BodyDeclaration bd = (BodyDeclaration) type.bodyDeclarations().get(i);
			if (bd instanceof FieldDeclaration)
				extract((FieldDeclaration) bd);
			else if (bd instanceof MethodDeclaration)
				extract((MethodDeclaration) bd);
			else if (bd instanceof AbstractTypeDeclaration)
				extract((AbstractTypeDeclaration) bd, prefix);
		}
	}

	private void extract(FieldDeclaration f) {
		for (int j = 0; j < f.fragments().size(); j++) {
			VariableDeclarationFragment vdf = (VariableDeclarationFragment) f.fragments().get(j);
			IVariableBinding vb = vdf.resolveBinding();
			if (vb == null) 
				continue;
			vb = vb.getVariableDeclaration();
			fields.add(getSignature(vb));
			numOfFields.incrementAndGet();
		}
	}

	private static String getSignature(IVariableBinding vb) {
		return getQualifiedName(vb.getDeclaringClass().getTypeDeclaration()) + "." + vb.getName() + " " + getQualifiedName(vb.getType().getTypeDeclaration());
	}

	private void extract(MethodDeclaration method) {
		IMethodBinding mb = method.resolveBinding();
		if (mb == null)
			return;
		mb = mb.getMethodDeclaration();
		methods.add(getSignature(mb));
		numOfMethods.incrementAndGet();
	}

	private static String getSignature(IMethodBinding mb) {
		StringBuilder sb = new StringBuilder();
		sb.append(getQualifiedName(mb.getDeclaringClass().getTypeDeclaration()) + "." + mb.getName() + " (");
		for (ITypeBinding tb : mb.getParameterTypes())
			sb.append(getQualifiedName(tb.getTypeDeclaration()) + ",");
		sb.append(") " + getQualifiedName(mb.getReturnType().getTypeDeclaration()));
		return sb.toString();
	}

	private static String getQualifiedName(ITypeBinding tb) {
		if (tb.isArray()) {
			return tb.getElementType().getTypeDeclaration().getQualifiedName() + "[" + tb.getDimensions() + "]";
		} else 
			return tb.getQualifiedName();
	}

	private void extract(Field field, String fqn) {
		fields.add(getSignature(field, fqn));
		numOfFields.incrementAndGet();
	}

	private static String getSignature(Field field, String fqn) {
		return fqn + "." + field.getName() + " " + field.getType().toString();
	}

	private void extract(Method method, String fqn) {
		methods.add(getSignature(method, fqn));
		numOfMethods.incrementAndGet();
	}

	private static String getSignature(Method method, String fqn) {
		StringBuilder sb = new StringBuilder();
		sb.append(fqn + "." + method.getName() + " (");
		for (Type type : method.getArgumentTypes())
			sb.append(type.toString() + ",");
		sb.append(") " + method.getReturnType().toString());
		return sb.toString();
	}

}
