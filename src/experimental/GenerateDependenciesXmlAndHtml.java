
package experimental;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

/**
 *  This class generates an ant script that checks the dependencies between packages 
 * and also generates an html page that illustrates the allowed dependencies.
 * The checkdep target on the generated ant script tests the dependencies of packages.  It does this by copying the contents of the package 
 *  in question together with the contents of the packages it is allowed to depend on to a temporary directory, then compiling
 *    the contents of the package.  If the packaged depends on classes other than those contained in the packages it is allowed
 *    to depend on, the compile will fail.
 *  @author Luke  
 *
 */
public class GenerateDependenciesXmlAndHtml {
	private static final Logger logger = Logger
			.getLogger(GenerateDependenciesXmlAndHtml.class.getName()); 

	private PrintWriter xmlWriter;
	private PrintWriter htmlWriter;
	private ArrayList<String> packages = new ArrayList<String>();
	private boolean started = false;
	private boolean startedBlock = false;
	private String sig;

	public static void main(String[] args) {
		try {			
			new GenerateDependenciesXmlAndHtml("checkdep.xml", "src"+File.separator+"docs"+File.separator+"dependencies.html");
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		}
	}
	
	private GenerateDependenciesXmlAndHtml(String xmlFilename, String htmlFilename) throws FileNotFoundException{
		
		Date d = new Date();
		sig = this.getClass().getName()+" on "+d;
		
		//Setup writers
		File xmlFile = new File(xmlFilename);
		xmlWriter = new PrintWriter(new FileOutputStream(xmlFile));
		File htmlFile = new File(htmlFilename);
		htmlWriter = new PrintWriter(new FileOutputStream(htmlFilename));
		
		String[] basePackages = {"jfreerails/util/*", "it/unimi/dsi/fastUtil/*"};
		start();
		
		startBlock("All");		
		
		add(basePackages);
		add("jfreerails/world/**/*");
		add("jfreerails/move/**/*");	
		add("jfreerails/controller/*");
		add("jfreerails/network/*");
		add(new String[] {"jfreerails/server/**/*", "jfreerails/client/**/*"});		
		add("jfreerails/launcher/**/*");
		add("jfreerails/experimental/**/*");
		
		endBlock();
		
		startBlock("World");
		add(basePackages);
		add("jfreerails/world/common/*");	
		add(new String[] {"jfreerails/world/terrain/*", "jfreerails/world/cargo/*",  "jfreerails/world/train/*", "jfreerails/world/station/*"});
		add("jfreerails/world/track/*");
		add("jfreerails/world/accounts/*");
		add("jfreerails/world/player/*");
		add("jfreerails/world/top/*");
		endBlock();	
		
		
		startBlock("Server");
		add(basePackages);
		add("jfreerails/world/**/*");
		add("jfreerails/move/**/*");
		add("jfreerails/controller/*");
		add("jfreerails/network/*");
		add("jfreerails/server/common/*");
		add("jfreerails/server/parser/*");
		add("jfreerails/server/*");			
		endBlock();		
		
		startBlock("Client");
		add(basePackages);
		add("jfreerails/world/**/*");		
		add("jfreerails/move/**/*");	
		add("jfreerails/controller/*");
		add("jfreerails/network/*");
		add("jfreerails/client/common/*");
		add("jfreerails/client/renderer/*");
		add("jfreerails/client/view/*");			
		add("jfreerails/client/top/*");
		endBlock();	
		
		
		
		finish();
		xmlWriter.flush();
		htmlWriter.flush();
		
		logger.info(sig);
		logger.info("Wrote "+xmlFile);
		logger.info("Wrote "+htmlFile);
	}
	
	private void start(){
		assert !started;
		
		startXml();
		
		htmlWriter.write("<html>\n");
		htmlWriter.write("<title>Dependencies between packages</title>\n");
		
		htmlWriter.write("<p><code>This file was generate by "+sig+"</code></p>\n");
		htmlWriter.write("<h1>Dependencies between packages</h1>\n");
		htmlWriter.write("<p>The figures below show the dependencies: packages may only depend, i.e. import classes and interfaces, from packages below.</p>\n");
		started = true;
	}
	
	private void startBlock(String blockName){
		assert started;
		assert !startedBlock;
		startedBlock = true;
		
		htmlWriter.write("<h2>"+blockName+"</h2>");
		xmlWriter.write("\n\t\t<!-- Setup the directory where the legal dependencies are stored  -->\n"); 
		xmlWriter.write("\t\t<delete dir=\"dependencies\" />\n");
		xmlWriter.write("\t\t<mkdir dir=\"dependencies\" />\n");		
	}
	
	private void endBlock(){
		assert started;
		assert startedBlock;
		
		htmlWriter.write("<table width=\"100%\" border=\"1\" cellpadding=\"10\" cellspacing=\"10\" bordercolor=\"#333333\" bgcolor=\"#FFFFFF\">\n");
		for(int i = packages.size() - 1; i >= 0 ; i--){
			String packageName = packages.get(i);			
			htmlWriter.write("<tr bgcolor=\"#FFCCCC\"> \n");
			htmlWriter.write("<td height=\"50\"  bgcolor=\"#FFCC66\">"+packageName+"</td>\n");
			htmlWriter.write("</tr>\n");
		}
		htmlWriter.write("</table>\n");
		packages.clear();
		
		xmlWriter.write("\n\t\t<!-- End Block -->\n");
		xmlWriter.write("\t\t<echo message=\"End Block\"/>\n");
		
		startedBlock = false;		
	}
	
	private void startXml() {
		//Start the file.
		xmlWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		xmlWriter.write("<project basedir=\".\" default=\"checkdep\" name=\"checkdep\">\n");
		xmlWriter.write("\t<description>This ant script was generated by "+sig+" to check the dependencies for jfreerails.</description>\n");
		
		//Set the properties.
		
		//Add the compile target.
		xmlWriter.write("\n\t<target description=\"Build everything except JUnit test-classes\" name=\"compile\">\n");
		xmlWriter.write("\t\t<mkdir dir=\"build\" />\n");
		xmlWriter.write("\t\t<javac destdir=\"build\" fork=\"true\" srcdir=\"src\" source=\"1.5\">\n");
		xmlWriter.write("\t\t\t<exclude name=\"**/*Test.java\" />\n");
		xmlWriter.write("\t\t </javac>\n");
		xmlWriter.write("\t</target>\n");
		
		//Start the check depend target.
		xmlWriter.write("\n\n\t<target depends=\"compile\" description=\"Tests whether dependencies between packages conform to the rules defined in this target\" name=\"checkdep\">\n");
		
		
	}

	private void add(String packageName){
		add(new String[]{packageName});
	}		
	
	private void add(String[] packageNames){
		assert started;
		assert startedBlock;
		
		String packagesString = "";
		for(int i = packageNames.length-1 ; i > 0  ; i--){		
			packagesString+= convertToPackageName(packageNames[i])+", ";	
		}	
		packagesString+= " "+convertToPackageName(packageNames[0]);				
		
		//The html writer will use this later.
		packages.add(packagesString);
		
		xmlWriter.write("\n\t\t<!-- New row: "+packagesString+"  -->\n");
		xmlWriter.write("\t\t<echo message=\"New row: "+packagesString+"\"/>\n");
		
		
		//Include the source files we are going to compile.
		for(int i = 0 ; i < packageNames.length ; i ++){	
			xmlWriter.write("\t\t<echo message=\"Check dependencies for "+packageNames[i]+"\"/>\n");
			
			xmlWriter.write("\t\t<delete dir=\"temp\" />\n");
			xmlWriter.write("\t\t<mkdir dir=\"temp\" />\n");
								
			//First copy the files we are testing.
			xmlWriter.write("\t\t<copy todir=\"temp\">\n");
			xmlWriter.write("\t\t<fileset dir=\"src\">\n");		
							
			xmlWriter.write("\t\t\t<include name=\""+packageNames[i]+".java\" />\n");		
																	
			//Exclude unit tests.
			xmlWriter.write("\t\t\t<exclude name=\"**/*Test.java\" />\n");
			
			xmlWriter.write("\t\t</fileset>\n");
			xmlWriter.write("\t\t</copy>\n");					
					
			xmlWriter.write("\t\t<javac fork=\"true\" srcdir=\"temp\" source=\"1.5\" classpath=\"dependencies\">\n");					
			//Include the files we are going to compile.					
			xmlWriter.write("\t\t\t<include name=\""+packageNames[i]+".java\" />\n");		
																						
			xmlWriter.write("\t\t</javac>\n");
			xmlWriter.write("\t\t<delete dir=\"temp\" />\n");
		}
		
		//Copy the files we have just tested to the dependencies directory.
		xmlWriter.write("\t\t<copy todir=\"dependencies\">\n");
		xmlWriter.write("\t\t<fileset dir=\"build\">\n");		
		for(int i = 0 ; i < packageNames.length ; i ++){		
			xmlWriter.write("\t\t\t<include name=\""+packageNames[i]+".class\" />\n");					
		}
		xmlWriter.write("\t\t\t<exclude name=\"**/*Test.class\" />\n");	
		xmlWriter.write("\t\t</fileset>\n");
		xmlWriter.write("\t\t</copy>\n");
		
	}
	
	private String convertToPackageName(String packagesString) {
		if(!isPackageNameOk(packagesString)){
			throw new IllegalArgumentException(packagesString);
		}
		packagesString = packagesString.replace('/', '.');
		
		/*Remove the last two characters, so that  
		 * jfreerails.world.**.* - > jfreerails.world.**
		 * and jfreerails.util.* -> jfreerails.util
		 */ 
		packagesString = packagesString.substring(0, packagesString.length()-2);
		return packagesString;
	}
	
	static boolean isPackageNameOk(String s){
		return s.matches("(([a-zA-Z]*)/)*\\*") || s.matches("(([a-zA-Z]*)/)*\\*\\*/\\*");		
	}

	private void finish(){
		assert started;
		assert !startedBlock;
		//finish the file.
		xmlWriter.write("\t\t<delete dir=\"temp\" />\n");
		xmlWriter.write("\t\t<delete dir=\"dependencies\" />\n");
		xmlWriter.write("\t</target>\n");
		xmlWriter.write("</project>\n");
				
		
		htmlWriter.write("</html>\n");
		started = false;
	}
	
}
