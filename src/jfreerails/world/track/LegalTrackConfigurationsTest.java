/*
 * LegalTrackConfigurationsTest.java
 * JUnit based test
 *
 *This test checks that the String representation of track configurations that
 *is used in the ruleset are processed correctly by the
 *constructor of LegalTrackConfigurations.
 *
 * Created on 21 January 2002, 23:15
 */
package jfreerails.world.track;

import java.util.ArrayList;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 *
 * @author lindsal
 */
public class LegalTrackConfigurationsTest extends TestCase {
    public LegalTrackConfigurationsTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(LegalTrackConfigurationsTest.class);
    }

    public void testTrackPieceIsLegal() {
        ArrayList templates = new ArrayList();

        templates.add("000111000");

        LegalTrackConfigurations ltc = new LegalTrackConfigurations(-1,
                templates);

        TrackConfiguration template = TrackConfiguration.getFlatInstance(
                "010010010");
        assertEquals(true, ltc.trackConfigurationIsLegal(template));
        template = TrackConfiguration.getFlatInstance("010111000");
        assertEquals(false, ltc.trackConfigurationIsLegal(template));
    }
}