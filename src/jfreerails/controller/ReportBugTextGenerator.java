package jfreerails.controller;
import java.awt.EventQueue;
import java.io.InputStream;
import java.util.Properties;

import jfreerails.controller.UnexpectedExceptionForm;

/*
 * Created on 09-Sep-2005
 *
 */

public class ReportBugTextGenerator {

	private static final String TRACKER_URL = "http://sourceforge.net/tracker/?group_id=9495&atid=109495";

	public static void main(String[] args) {
		Exception e = genException();

		System.out.println(genText());
		
		System.out.println(genText(e));
	
	}

	private static Exception genException() {
		Exception e = new Exception();
		return e;
	}

	public static String genText() {
		StringBuffer sb = new StringBuffer();
		sb.append("How to report a bug\n");
		sb.append("\n");
		sb.append("Use the sourceforge.net bug tracker at the following url:\n");
		sb.append(TRACKER_URL);
		sb.append("\n");
		sb.append("\n");
		sb.append("Please include:\n");
		sb.append("  1. Steps to reproduce the bug (attach a  save game if  appropriate).\n");
		sb.append("  2. What you expected to see.\n");
		sb.append("  3. What you saw instead (attach a screenshot if appropriate).\n");
		sb.append("  4. The details below (copy and past them into the bug report).\n");		
		appendBuildProps(sb);
		sb.append("\n");
		sb.append("\n");
		return sb.toString();
	}

	public  static String genText(Exception e) {
		StackTraceElement[] s = e.getStackTrace();

		StringBuffer sb = new StringBuffer();
		sb.append("Unexpected Exception\n");
		sb.append("\n");
		sb.append("Consider submitting a bug report using the sourceforge.net" +
				" bug tracker at the following url:\n");
		sb.append(TRACKER_URL);
		sb.append("\n");
		sb.append("\n");
		sb.append("Please:\n");
		sb.append("  1. Use the following as the title of the bug report:\n\t");		
		sb.append(" Unexpected Exception: ");
		sb.append(s[0].getFileName());
		sb.append(" line ");
		sb.append(s[0].getLineNumber());
		sb.append("\n");
		sb.append("  2. Include steps to reproduce the bug (attach a  save game if  appropriate).\n");
		sb.append("  3. Copy and paste the details below into the bug report:\n");	
		appendBuildProps(sb);
		sb.append("\n");
		sb.append("\n\t");

		sb.append(e.toString());
		for (StackTraceElement ste : s) {
			sb.append("\n\t\t at ");
			sb.append(ste);
		}
		return sb.toString();
	}

	private static void appendBuildProps(StringBuffer sb) {

		String version = null;

		String builtBy = null;;

		try {
			Properties props = new Properties();			
			InputStream in = ReportBugTextGenerator.class
			.getResourceAsStream("/build.properties");
			props.load(in);		
			in.close();
			version = props.getProperty("freerails.build");
			builtBy = props.getProperty("freerails.built.by");
		} catch (Exception e) {
			// ignore, there's nothing useful we can do.			
		}
		version = null == version ? "not set" : version;
		builtBy = null == builtBy ? "not set" : builtBy;		
		sb.append("\t");
		sb.append(System.getProperty("os.name"));
		sb.append(" ");
		sb.append(System.getProperty("os.version"));
		sb.append("\n\t");
		sb.append(System.getProperty("java.vm.name"));
		sb.append(" ");
		sb.append(System.getProperty("java.version"));
		sb.append("\n\t");
		sb.append("Freerails build ");
		sb.append(version);
		sb.append("  compiled by ");
		sb.append(builtBy);
	}

	@SuppressWarnings("deprecation")
	public static void unexpectedException(Exception e) {
		ScreenHandler.exitFullScreenMode();
	
		String str = genText(e);
		System.err.print(str);
		UnexpectedExceptionForm unexpectedExceptionForm = new UnexpectedExceptionForm();
		unexpectedExceptionForm.setText(str);
		unexpectedExceptionForm.setVisible(true);
		if(!EventQueue.isDispatchThread()){
			Thread.currentThread().stop();
		}
	}

}
