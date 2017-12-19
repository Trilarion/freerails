/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package experimental;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;

/**
 * This class generates an ant script that checks the dependencies between
 * packages and also generates an html page that illustrates the allowed
 * dependencies. The checkdep target on the generated ant script tests the
 * dependencies of packages. It does this by copying the contents of the package
 * in question together with the contents of the packages it is allowed to
 * depend on to a temporary directory, then compiling the contents of the
 * package. If the packaged depends on classes other than those contained in the
 * packages it is allowed to depend on, the compile will fail.
 */
public class GenerateDependenciesXmlAndHtml {
    private static final Logger logger = Logger
            .getLogger(GenerateDependenciesXmlAndHtml.class.getName());
    private final ArrayList<String> packages = new ArrayList<>();
    private final String sig;
    private PrintWriter xmlWriter;
    private PrintWriter htmlWriter;
    private boolean started = false;
    private boolean startedBlock = false;

    private GenerateDependenciesXmlAndHtml(String xmlFilename,
                                           String htmlFilename) throws FileNotFoundException {

        Date d = new Date();
        sig = this.getClass().getName() + " on " + d;

        // Setup writers
        File xmlFile = new File(xmlFilename);
        xmlWriter = new PrintWriter(new FileOutputStream(xmlFile));
        File htmlFile = new File(htmlFilename);
        htmlWriter = new PrintWriter(new FileOutputStream(htmlFilename));

        String[] basePackages = {"freerails/util/*"};
        start();

        startBlock("All");

        add(basePackages);
        add("freerails/world/**/*");
        add("freerails/move/**/*");
        add("freerails/controller/*");
        add("freerails/network/*");
        add(new String[]{"freerails/server/**/*", "freerails/client/**/*"});
        add("freerails/launcher/**/*");
        add("freerails/experimental/**/*");

        endBlock();

        startBlock("World");
        add(basePackages);
        add("freerails/world/common/*");
        add(new String[]{"freerails/world/terrain/*",
                "freerails/world/cargo/*", "freerails/world/train/*",
                "freerails/world/station/*"});
        add("freerails/world/track/*");
        add("freerails/world/finances/*");
        add("freerails/world/player/*");
        add("freerails/world/top/*");
        endBlock();

        startBlock("Server");
        add(basePackages);
        add("freerails/world/**/*");
        add("freerails/move/**/*");
        add("freerails/controller/*");
        add("freerails/network/*");
        add("freerails/server/common/*");
        add("freerails/server/parser/*");
        add("freerails/server/*");
        endBlock();

        startBlock("Client");
        add(basePackages);
        add("freerails/world/**/*");
        add("freerails/move/**/*");
        add("freerails/controller/*");
        add("freerails/network/*");
        add("freerails/client/common/*");
        add("freerails/client/renderer/*");
        add("freerails/client/view/*");
        add("freerails/client/top/*");
        endBlock();

        finish();
        xmlWriter.flush();
        htmlWriter.flush();

        logger.info(sig);
        logger.info("Wrote " + xmlFile);
        logger.info("Wrote " + htmlFile);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            new GenerateDependenciesXmlAndHtml("checkdep.xml", "src"
                    + File.separator + "docs" + File.separator
                    + "dependencies.html");
        } catch (FileNotFoundException e) {
        }
    }

    static boolean isPackageNameOk(String s) {
        return s.matches("(([a-zA-Z]*)/)*\\*")
                || s.matches("(([a-zA-Z]*)/)*\\*\\*/\\*");
    }

    private void start() {
        assert !started;

        startXml();

        htmlWriter.write("<html>\n");
        htmlWriter.write("<title>Dependencies between packages</title>\n");

        htmlWriter.write("<p><code>This file was generate by " + sig
                + "</code></p>\n");
        htmlWriter.write("<h1>Dependencies between packages</h1>\n");
        htmlWriter
                .write("<p>The figures below show the dependencies: packages may only depend, i.e. import classes and interfaces, from packages below.</p>\n");
        started = true;
    }

    private void startBlock(String blockName) {
        assert started;
        assert !startedBlock;
        startedBlock = true;

        htmlWriter.write("<h2>" + blockName + "</h2>");
        xmlWriter
                .write("\n\t\t<!-- Setup the directory where the legal dependencies are stored  -->\n");
        xmlWriter.write("\t\t<delete dir=\"dependencies\" />\n");
        xmlWriter.write("\t\t<mkdir dir=\"dependencies\" />\n");
    }

    private void endBlock() {
        assert started;
        assert startedBlock;

        htmlWriter
                .write("<table width=\"100%\" border=\"1\" cellpadding=\"10\" cellspacing=\"10\" bordercolor=\"#333333\" bgcolor=\"#FFFFFF\">\n");
        for (int i = packages.size() - 1; i >= 0; i--) {
            String packageName = packages.get(i);
            htmlWriter.write("<tr bgcolor=\"#FFCCCC\"> \n");
            htmlWriter.write("<td height=\"50\"  bgcolor=\"#FFCC66\">"
                    + packageName + "</td>\n");
            htmlWriter.write("</tr>\n");
        }
        htmlWriter.write("</table>\n");
        packages.clear();

        xmlWriter.write("\n\t\t<!-- End Block -->\n");
        xmlWriter.write("\t\t<echo message=\"End Block\"/>\n");

        startedBlock = false;
    }

    private void startXml() {
        // Start the file.
        xmlWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xmlWriter
                .write("<project basedir=\".\" default=\"checkdep\" name=\"checkdep\">\n");
        xmlWriter.write("\t<description>This ant script was generated by "
                + sig
                + " to check the dependencies for freerails.</description>\n");

        // Set the properties.

        // Add the compile target.
        xmlWriter
                .write("\n\t<target description=\"Build everything except JUnit test-classes\" name=\"compile\">\n");
        xmlWriter.write("\t\t<mkdir dir=\"build\" />\n");
        xmlWriter
                .write("\t\t<javac destdir=\"build\" fork=\"true\" srcdir=\"src\" source=\"1.5\">\n");
        xmlWriter.write("\t\t\t<exclude name=\"**/*Test.java\" />\n");
        xmlWriter.write("\t\t </javac>\n");
        xmlWriter.write("\t</target>\n");

        // Start the check depend target.
        xmlWriter
                .write("\n\n\t<target depends=\"compile\" description=\"Tests whether dependencies between packages conform to the rules defined in this target\" name=\"checkdep\">\n");

    }

    private void add(String packageName) {
        add(new String[]{packageName});
    }

    private void add(String[] packageNames) {
        assert started;
        assert startedBlock;

        String packagesString = "";
        for (int i = packageNames.length - 1; i > 0; i--) {
            packagesString += convertToPackageName(packageNames[i]) + ", ";
        }
        packagesString += " " + convertToPackageName(packageNames[0]);

        // The html writer will use this later.
        packages.add(packagesString);

        xmlWriter.write("\n\t\t<!-- New row: " + packagesString + "  -->\n");
        xmlWriter.write("\t\t<echo message=\"New row: " + packagesString
                + "\"/>\n");

        // Include the source files we are going to compile.
        for (String packageName : packageNames) {
            xmlWriter.write("\t\t<echo message=\"Check dependencies for "
                    + packageName + "\"/>\n");

            xmlWriter.write("\t\t<delete dir=\"temp\" />\n");
            xmlWriter.write("\t\t<mkdir dir=\"temp\" />\n");

            // First copy the files we are testing.
            xmlWriter.write("\t\t<copy todir=\"temp\">\n");
            xmlWriter.write("\t\t<fileset dir=\"src\">\n");

            xmlWriter.write("\t\t\t<include name=\"" + packageName
                    + ".java\" />\n");

            // Exclude unit tests.
            xmlWriter.write("\t\t\t<exclude name=\"**/*Test.java\" />\n");

            xmlWriter.write("\t\t</fileset>\n");
            xmlWriter.write("\t\t</copy>\n");

            xmlWriter
                    .write("\t\t<javac fork=\"true\" srcdir=\"temp\" source=\"1.5\" classpath=\"dependencies\">\n");
            // Include the files we are going to compile.
            xmlWriter.write("\t\t\t<include name=\"" + packageName
                    + ".java\" />\n");

            xmlWriter.write("\t\t</javac>\n");
            xmlWriter.write("\t\t<delete dir=\"temp\" />\n");
        }

        // Copy the files we have just tested to the dependencies directory.
        xmlWriter.write("\t\t<copy todir=\"dependencies\">\n");
        xmlWriter.write("\t\t<fileset dir=\"build\">\n");
        for (String packageName : packageNames) {
            xmlWriter.write("\t\t\t<include name=\"" + packageName
                    + ".class\" />\n");
        }
        xmlWriter.write("\t\t\t<exclude name=\"**/*Test.class\" />\n");
        xmlWriter.write("\t\t</fileset>\n");
        xmlWriter.write("\t\t</copy>\n");

    }

    private String convertToPackageName(String packagesString) {
        if (!isPackageNameOk(packagesString)) {
            throw new IllegalArgumentException(packagesString);
        }
        packagesString = packagesString.replace('/', '.');

        /*
         * Remove the last two characters, so that freerails.world.**.* - >
         * freerails.world.** and freerails.util.* -> freerails.util
         */
        packagesString = packagesString.substring(0,
                packagesString.length() - 2);
        return packagesString;
    }

    private void finish() {
        assert started;
        assert !startedBlock;
        // finish the file.
        xmlWriter.write("\t\t<delete dir=\"temp\" />\n");
        xmlWriter.write("\t\t<delete dir=\"dependencies\" />\n");
        xmlWriter.write("\t</target>\n");
        xmlWriter.write("</project>\n");

        htmlWriter.write("</html>\n");
        started = false;
    }

}
