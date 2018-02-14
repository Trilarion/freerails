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

package freerails.savegames;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Stack;

/**
 * The class reads XML documents according to specified DTD and translates all
 * related events into TrackTilesHandler events.
 *
 * Usage sample:
 *
 * <pre>
 *      Track_TilesParser parser = new Track_TilesParser(...);
 *      parser.parse(new InputSource(&quot;...&quot;));
 * </pre>
 */
public class TrackTilesXmlParser implements org.xml.sax.ContentHandler {

    private static final Logger logger = Logger.getLogger(TrackTilesXmlParser.class.getName());
    private final StringBuffer buffer;
    private final TrackTilesXmlHandler handler;
    private final Stack<Object[]> context;

    /**
     * @param handler
     */
    private TrackTilesXmlParser(final TrackTilesXmlHandler handler) {
        this.handler = handler;
        buffer = new StringBuffer(111);
        context = new java.util.Stack<>();
    }

    /**
     * The recognizer entry method taking an Inputsource.
     *
     * @param input InputSource to be parsed.
     * @throws java.io.IOException                            on I/O error.
     * @throws SAXException                                   propagated exception thrown by a DocumentHandler.
     * @throws javax.xml.parsers.ParserConfigurationException a parser satisfying requested configuration can not be
     *                                                        created.
     */
    private static void parse(final InputSource input, final TrackTilesXmlHandler handler) throws SAXException, ParserConfigurationException, IOException {
        parse(input, new TrackTilesXmlParser(handler));
    }

    /**
     * The recognizer entry method taking a URL.
     *
     * @param url URL source to be parsed.
     * @throws java.io.IOException                            on I/O error.
     * @throws SAXException                                   propagated exception thrown by a DocumentHandler.
     * @throws javax.xml.parsers.ParserConfigurationException a parser satisfying requested configuration can not be
     *                                                        created.
     */
    public static void parse(final java.net.URL url, final TrackTilesXmlHandler handler) throws SAXException, ParserConfigurationException, IOException {
        parse(new InputSource(url.toExternalForm()), handler);
    }

    private static void parse(final InputSource input, final TrackTilesXmlParser recognizer) throws SAXException, ParserConfigurationException, IOException {
        javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setValidating(true); // the code was generated according DTD
        factory.setNamespaceAware(false); // the code was generated according
        // DTD

        org.xml.sax.XMLReader parser = factory.newSAXParser().getXMLReader();
        parser.setContentHandler(recognizer);
        parser.setErrorHandler(recognizer.getDefaultErrorHandler());
        parser.parse(input);
    }

    public void setDocumentLocator(org.xml.sax.Locator locator) {
    }

    public void startDocument() {
    }

    public void endDocument() {
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        dispatch(true);
        context.push(new Object[]{qName, new org.xml.sax.helpers.AttributesImpl(attributes)});

        switch (qName) {
            case "CanOnlyBuildOnTheseTerrainTypes":
                handler.startCanOnlyBuildOnTheseTerrainTypes(attributes);
                break;
            case "ListOfTrackPieceTemplates":
                handler.startListOfTrackPieceTemplates(attributes);
                break;
            case "CannotBuildOnTheseTerrainTypes":
                handler.startCannotBuildOnTheseTerrainTypes(attributes);
                break;
            case "TrackType":
                handler.startTrackType(attributes);
                break;
            case "TerrainType":
                handler.handleTerrainType(attributes);
                break;
            case "TrackPieceTemplate":
                handler.startTrackPieceTemplate(attributes);
                break;
            case "TrackSet":
                handler.startTrackSet(attributes);
                break;
        }
    }

    public void endElement(String uri, String localName, String qName) {
        dispatch(false);
        context.pop();

        switch (qName) {
            case "CanOnlyBuildOnTheseTerrainTypes":
                handler.endCanOnlyBuildOnTheseTerrainTypes();
                break;
            case "ListOfTrackPieceTemplates":
                handler.endListOfTrackPieceTemplates();
                break;
            case "CannotBuildOnTheseTerrainTypes":
                handler.endCannotBuildOnTheseTerrainTypes();
                break;
            case "TrackType":
                handler.endTrackType();
                break;
            case "Tiles":
                handler.endTiles();
                break;
        }
    }

    public void characters(char[] ch, int start, int length) {
        buffer.append(ch, start, length);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) {
    }

    public void processingInstruction(String target, String data) {
    }

    public void startPrefixMapping(final String prefix, final String uri) {
    }

    public void endPrefixMapping(final String prefix) {
    }

    public void skippedEntity(String name) {
    }

    private void dispatch(final boolean fireOnlyIfMixed) {
        if (fireOnlyIfMixed && buffer.length() == 0) {
            return; // skip it
        }

        buffer.delete(0, buffer.length());
    }

    private org.xml.sax.ErrorHandler getDefaultErrorHandler() {
        return new org.xml.sax.ErrorHandler() {
            public void error(org.xml.sax.SAXParseException exception) throws SAXException {
                if (context.isEmpty()) {
                    logger.error("Missing DOCTYPE.");
                }

                throw exception;
            }

            public void fatalError(org.xml.sax.SAXParseException exception) throws SAXException {
                throw exception;
            }

            public void warning(org.xml.sax.SAXParseException exception) {
                // ignore
            }
        };
    }
}