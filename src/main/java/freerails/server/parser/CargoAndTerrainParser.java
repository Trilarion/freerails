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

package freerails.server.parser;

import org.apache.log4j.Logger;
import org.xml.sax.*;

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
// TODO where is the DTD, how to find it?
public class CargoAndTerrainParser implements ContentHandler {

    private static final Logger logger = Logger.getLogger(CargoAndTerrainParser.class.getName());

    private final java.lang.StringBuffer buffer;
    private final CargoAndTerrainHandler handler;
    private final java.util.Stack<Object[]> context;
    private final EntityResolver resolver;

    /**
     * Creates a parser instance.
     *
     * @param handler  handler interface implementation (never {@code null}
     * @param resolver SAX entity resolver implementation or {@code null}. It
     *                 is recommended that it could be able to resolve at least the
     *                 DTD.
     */
    public CargoAndTerrainParser(final CargoAndTerrainHandler handler, final EntityResolver resolver) {
        this.handler = handler;
        this.resolver = resolver;
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
    public static void parse(final InputSource input, final CargoAndTerrainHandler handler) throws SAXException, javax.xml.parsers.ParserConfigurationException, java.io.IOException {
        parse(input, new CargoAndTerrainParser(handler, null));
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
    public static void parse(final java.net.URL url, final CargoAndTerrainHandler handler) throws SAXException, javax.xml.parsers.ParserConfigurationException, java.io.IOException {
        parse(new InputSource(url.toExternalForm()), handler);
    }

    private static void parse(final InputSource input, final CargoAndTerrainParser recognizer) throws SAXException, javax.xml.parsers.ParserConfigurationException, java.io.IOException {
        javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
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
    public final void setDocumentLocator(Locator locator) {
    }

    /**
     * This SAX interface method is implemented by the parser.
     */
    public final void startDocument() {
    }

    /**
     * This SAX interface method is implemented by the parser.
     */
    public final void endDocument() {
    }

    /**
     * This SAX interface method is implemented by the parser.
     */
    public final void startElement(java.lang.String uri, java.lang.String localName, java.lang.String qName, Attributes atts) throws SAXException {
        dispatch(true);
        context.push(new Object[]{qName, new org.xml.sax.helpers.AttributesImpl(atts)});

        switch (localName) {
            case "Converts":
                handler.handle_Converts(atts);
                break;
            case "Tile":
                handler.start_Tile(atts);
                break;
            case "Cargo":
                handler.handle_Cargo(atts);
                break;
            case "Consumes":
                handler.handle_Consumes(atts);
                break;
            case "Produces":
                handler.handle_Produces(atts);
                break;
        }
    }

    /**
     * This SAX interface method is implemented by the parser.
     */
    public final void endElement(java.lang.String uri, java.lang.String localName, java.lang.String qName) throws SAXException {
        dispatch(false);
        context.pop();

        switch (localName) {
            case "Tile":
                handler.end_Tile();
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
    public final void ignorableWhitespace(char[] ch, int start, int length) {
    }

    /**
     * This SAX interface method is implemented by the parser.
     */
    public final void processingInstruction(java.lang.String target, java.lang.String data) {
    }

    /**
     * This SAX interface method is implemented by the parser.
     */
    public final void startPrefixMapping(final java.lang.String prefix, final java.lang.String uri) {
    }

    /**
     * This SAX interface method is implemented by the parser.
     */
    public final void endPrefixMapping(final java.lang.String prefix) {
    }

    /**
     * This SAX interface method is implemented by the parser.
     */
    public final void skippedEntity(java.lang.String name) {
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
    protected ErrorHandler getDefaultErrorHandler() {
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