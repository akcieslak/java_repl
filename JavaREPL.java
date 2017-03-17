
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import com.sun.source.util.JavacTask;
import javax.tools.*;


public class JavaREPL {
	public static void exec(Reader r) throws IOException{
		BufferedReader stdin = new BufferedReader(r);
		NestedReader reader = new NestedReader(stdin);
		int classNumber = 0;
		String extendSuper;

		//Creates a temporary path and specifies for it to be deleted on exit
		Path path = Files.createTempDirectory(null);
		path.toFile().deleteOnExit();


		URL tmp = new File(path.toString()).toURI().toURL();
		URLClassLoader loader = new URLClassLoader(new URL[]{tmp});

		
		while (true) {

			System.out.print("> ");

			try {
				stdin.ready();
			} catch (IOException e){
				break;
			}

			String java = reader.getNestedString();
			
			if (java.startsWith("print")){
				java = replacePrint(java);
			}
			
			if (classNumber == 0){
				extendSuper = "";
			} else {
				extendSuper = "extends Interp_" + (classNumber - 1);
 			}

			//opens up file and writes info as a declaration
			String code = getCode("Interp_" + classNumber, extendSuper, "public static " + java, "");
			File file = writeFile(path, code, classNumber);
			
			//if the info isn't a declaration, opens up the file and tries to write it as a statement
			if (!isDeclaration(file)){
				code = getCode("Interp_" + classNumber, extendSuper, "", java);
				file = writeFile(path, code, classNumber);
			};

			//tries to compile code
			if (compile(file, path)){
				try {
					execute(loader, classNumber);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				classNumber += 1;
			}
		}

	}
	
	
	public static String replacePrint(String java){
		StringBuffer editJava = new StringBuffer();
		StringBuffer builder = new StringBuffer();
		editJava.append(java);
		if (editJava.length() > 5){
			editJava.delete(0, 5);
		}
		//deletes the semi-colon
		editJava.delete(editJava.length()-1, editJava.length());
		//sticks the info to be printed between Sysout statement
		String value2 = builder.append("System.out.println(" + editJava.toString()
				+ ");").toString();
		return value2;
		
	}
	
	
	public static File writeFile(Path path, String code, int classNumber) throws IOException{
		File file = new File(path + "/Interp_" + classNumber + ".java");
		file.deleteOnExit();
		FileWriter fw = new FileWriter(file.getAbsolutePath());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(code);
		bw.close();
		fw.close();
		return file;
	}


	public static void execute(URLClassLoader loader, int classNumber) throws ClassNotFoundException{
		Class<?> c2 = loader.loadClass("Interp_" + (classNumber));
		java.lang.reflect.Method m2;
		try {
			m2 = c2.getDeclaredMethod("exec");
			m2.invoke(null, null);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	
	public static boolean compile(File name, Path path) throws IOException{
		JavaCompiler compiler= ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
    	Iterable<? extends JavaFileObject> compilationUnits =
 	           fileManager.getJavaFileObjectsFromStrings(Arrays.asList(name.toString()));
    	Iterable<String>  compileOptions = Arrays.asList("-classpath", path.toString());
		JavacTask task = (JavacTask) compiler.getTask(null, fileManager, diagnostics,
			    compileOptions, null, compilationUnits);


		boolean ok = task.call();
		//http://stackoverflow.com/questions/32295873/how-to-get-compile-error-messages-with-javacompiler
		if (diagnostics.getDiagnostics().size() > 0){
			java.util.List<Diagnostic<? extends JavaFileObject>> diagnosticsInfo = diagnostics.getDiagnostics();
	        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnosticsInfo) {
	        	System.err.println("line " + diagnostic.getLineNumber() + ": " + diagnostic.getMessage(new Locale("English")));
	        	System.err.flush();
	        }
		}
		fileManager.close();
		return ok;

	}


	public static boolean isDeclaration(File name) throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
    	Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjects(name);

		JavacTask task = (JavacTask) compiler.getTask(null, fileManager, diagnostics,
			    null, null, fileObjects);


		 task.parse();
		 fileManager.close();
		 return diagnostics.getDiagnostics().size() == 0;

	}


	public static String getCode(String className, String extendSuper,
								 String def, String stat)
	{
		String code =
			"\n" +
			"import java.io.*;\n" +
			"import java.util.*;\n"+
			"public class "+className+" "+extendSuper+"{\n" +
			"	"+def+"\n"+
			"	public static void exec() {\n" +
			"		"+stat+"\n"+
			"	}\n"+
			"}\n";
		return code;
	}


	public static void main(String[] args) throws IOException {
		exec(new InputStreamReader(System.in));
	}



}

