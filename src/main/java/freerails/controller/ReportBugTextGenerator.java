package freerails.controller;

import java.awt.*;
import java.io.InputStream;
import java.util.Properties;

/*
 * Created on 09-Sep-2005
 *
 */

/**
 *
 */


public class ReportBugTextGenerator {

    private static final String TRACKER_URL = "http://sourceforge.net/tracker/?func=add&group_id=209321&atid=1009246";

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        Exception e = genException();

        System.out.println(genText());

        System.out.println(genText(e));

    }

    private static Exception genException() {
        return new Exception();
    }

    /**
     *
     * @return
     */
    public static String genText() {
        StringBuffer sb = new StringBuffer();
        sb.append("How to report a bug\n");
        sb.append("\n");
        sb
                .append("Use the sourceforge.net bug tracker at the following url:\n");
        sb.append(TRACKER_URL);
        sb.append("\n");
        sb.append("\n");
        sb.append("Please include:\n");
        sb
                .append("  1. Steps to reproduce the bug (attach a  save game if  appropriate).\n");
        sb.append("  2. What you expected to see.\n");
        sb
                .append("  3. What you saw instead (attach a screenshot if appropriate).\n");
        sb
                .append("  4. The details below (copy and past them into the bug report).\n");
        appendBuildProps(sb);
        sb.append("\n");
        sb.append("\n");
        return sb.toString();
    }

    /**
     *
     * @param e
     * @return
     */
    public static String genText(Exception e) {
        StackTraceElement[] s = e.getStackTrace();

        StringBuffer sb = new StringBuffer();
        sb.append("Unexpected Exception\n");
        sb.append("\n");
        sb.append("Consider submitting a bug report using the sourceforge.net"
                + " bug tracker at the following url:\n");
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
        sb
                .append("  2. Include steps to reproduce the bug (attach a  save game if  appropriate).\n");
        sb
                .append("  3. Copy and paste the details below into the bug report:\n");
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

        String builtBy = null;

        String versionnumber = null;
        try {
            Properties props = new Properties();
            InputStream in = ReportBugTextGenerator.class
                    .getResourceAsStream("/build.properties");
            props.load(in);

            props.load(in);
            in.close();
            version = props.getProperty("freerails.build");
            builtBy = props.getProperty("freerails.built.by");
            versionnumber = props.getProperty("freerails.version");
        } catch (Exception e) {
            // ignore, there's nothing useful we can do.
        }
        version = null == version ? "not set" : version;
        builtBy = null == builtBy ? "not set" : builtBy;
        versionnumber = null == versionnumber ? "not set" : versionnumber;

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
        sb.append("\n\t");
        sb.append("compiled by ");
        sb.append(builtBy);
        sb.append("\n\t");
        sb.append("Version: ");
        sb.append(versionnumber);
    }

    /**
     *
     * @param e
     */
    @SuppressWarnings("deprecation")
    public static void unexpectedException(Exception e) {
        ScreenHandler.exitFullScreenMode();

        String str = genText(e);
        System.err.print(str);
        UnexpectedExceptionForm unexpectedExceptionForm = new UnexpectedExceptionForm();
        unexpectedExceptionForm.setText(str);
        unexpectedExceptionForm.setVisible(true);
        if (!EventQueue.isDispatchThread()) {
            Thread.currentThread().stop();
        }
    }

}
