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
import org.xml.sax.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.util.Stack;

/**
 * The class reads XML documents according to specified DTD and translates all
 * related events into CargoAndTerrainHandler events.
 *
 * Usage sample:
 *
 * <pre>
 *      RulesParser parser = new RulesParser(...);
 *      parser.parse(new InputSource(&quot;...&quot;));
 * </pre>
 *
 * <b>Warning:</b> the class is machine generated. DO NOT MODIFY!
 */
// TODO where is the DTD, how to find it? (see FullSaveGameManager)
public class CargoAndTerrainXmlParser implements ContentHandler {

    private static final Logger logger = Logger.getLogger(CargoAndTerrainXmlParser.class.getName());

    private final StringBuffer buffer;
    private final CargoAndTerrainXmlHandler handler;
    private final Stack<Object[]> context;
    private final EntityResolver resolver;

    /**
     * Creates a parser instance.
     *
     * @param handler  handler interface implementation (never {@code null}
     * @param resolver SAX entity resolver implementation or {@code null}. It
     *                 is recommended that it could be able to resolve at least the
     *                 DTD.
     */
    private CargoAndTerrainXmlParser(final CargoAndTerrainXmlHandler handler, final EntityResolver resolver) {
        this.handler = handler;
        this.resolver = resolver;
        buffer = new StringBuffer(111);
        context = new Stack<>();
    }

    /**
     * The recognizer entry method taking an Inputsource.
     *
     * @param input InputSource to be parsed.
     * @throws java.io.IOException                            on I/O error.
     * @throws SAXException                                   propagated exception thrown by a DocumentHandler.
     * @throws ParserConfigurationException a parser satisfying requested configuration can not be
     *                                                        created.
     */
    private static void parse(final InputSource input, final CargoAndTerrainXmlHandler handler) throws SAXException, ParserConfigurationException, java.io.IOException {
        parse(input, new CargoAndTerrainXmlParser(handler, null));
    }

    /**
     * The recognizer entry method taking a URL.
     *
     * @param url URL source to be parsed.
     * @throws java.io.IOException                            on I/O error.
     * @throws SAXException                                   propagated exception thrown by a DocumentHandler.
     * @throws ParserConfigurationException a parser satisfying requested configuration can not be
     *                                                        created.
     */
    public static void parse(final java.net.URL url, final CargoAndTerrainXmlHandler handler) throws SAXException, ParserConfigurationException, java.io.IOException {
        parse(new InputSource(url.toExternalForm()), handler);
    }

    private static void parse(final InputSource input, final CargoAndTerrainXmlParser recognizer) throws SAXException, ParserConfigurationException, java.io.IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true); // the code was generated according DTD
        factory.setNamespaceAware(true); // the code was generated according
        // DTD

        XMLReader parser = factory.newSAXParser().getXMLReader();
        parser.setContentHandler(recognizer);
        parser.setErrorHandler(recognizer.getDefaultErrorHandler());

        if (recognizer.resolver != null) {
            parser.setEntityResolver(recognizer.resolver);
        }

        parser.parse(input);
    }

    /**
     * This SAX interface method is implemented by the parser.
     */
    public final void setDocumentLocator(Locator locator) {}

    /**
     * This SAX interface method is implemented by the parser.
     */
    public final void startDocument() {}

    /**
     * This SAX interface method is implemented by the parser.
     */
    public final void endDocument() {}

    /**
     * This SAX interface method is implemented by the parser.
     */
    public final void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        dispatch(true);
        context.push(new Object[]{qName, new org.xml.sax.helpers.AttributesImpl(atts)});

        switch (localName) {
            case "Converts":
                handler.handleConversions(atts);
                break;
            case "Tile":
                handler.startTile(atts);
                break;
            case "Cargo":
                handler.handleCargo(atts);
                break;
            case "Consumes":
                handler.handleConsumptions(atts);
                break;
            case "Produces":
                handler.handleProductions(atts);
                break;
        }
    }

    /**
     * This SAX interface method is implemented by the parser.
     */
    public final void endElement(String uri, String localName, String qName) {
        dispatch(false);
        context.pop();

        switch (localName) {
            case "Tile":
                handler.endTile();
                break;
        }
    }

    /**
     * This SAX interface method is implemented by the parser.
     */
    public final void characters(char[] ch, int start, int length) {
        buffer.append(ch, start, length);
    }

    /**
     * This SAX interface method is implemented by the parser.
     */
    public final void ignorableWhitespace(char[] ch, int start, int length) {}

    /**
     * This SAX interface method is implemented by the parser.
     */
    public final void processingInstruction(String target, String data) {}

    /**
     * This SAX interface method is implemented by the parser.
     */
    public final void startPrefixMapping(final String prefix, final String uri) {
    }

    /**
     * This SAX interface method is implemented by the parser.
     */
    public final void endPrefixMapping(final String prefix) {
    }

    /**
     * This SAX interface method is implemented by the parser.
     */
    public final void skippedEntity(String name) {
    }

    private void dispatch(final boolean fireOnlyIfMixed) {
        if (fireOnlyIfMixed && buffer.length() == 0) {
            return; // skip it
        }

        buffer.delete(0, buffer.length());
    }

    /**
     * Creates default error handler used by this parser.
     *
     * @return org.xml.sax.ErrorHandler implementation
     */
    private ErrorHandler getDefaultErrorHandler() {
        return new ErrorHandler() {
            public void error(SAXParseException exception) throws SAXException {
                if (context.isEmpty()) {
                    logger.error("Missing DOCTYPE.");
                }

                throw exception;
            }

            public void fatalError(SAXParseException exception) throws SAXException {
                throw exception;
            }

            public void warning(SAXParseException exception) {
                // ignore
            }
        };
    }
}