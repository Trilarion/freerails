/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

/*
 * HtmlPanel.java
 *
 */

package freerails.client.view;

import freerails.client.renderer.RendererRoot;
import freerails.client.ModelRoot;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Displays a HTML document read from a URL.
 */
public class HtmlPanel extends JPanel implements View {

    @Serial
    private static final long serialVersionUID = 4120848850266371126L;
    private static final TaggedLogger logger = Logger.tag("HtmlPanel");
    private JButton done;
    private JLabel htmlJLabel;

    /**
     * Creates a html panel instance.
     */
    HtmlPanel() {
        GridBagConstraints gridBagConstraints;

        JScrollPane jScrollPane1 = new JScrollPane();
        htmlJLabel = new JLabel();
        done = new JButton();

        setLayout(new GridBagLayout());

        setMinimumSize(new Dimension(400, 300));
        jScrollPane1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        htmlJLabel.setFont(new Font("Dialog", 0, 12));
        htmlJLabel.setVerticalTextPosition(SwingConstants.TOP);
        jScrollPane1.setViewportView(htmlJLabel);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(7, 7, 7, 7);
        add(jScrollPane1, gridBagConstraints);

        done.setText("Close");

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(7, 7, 7, 7);
        add(done, gridBagConstraints);
    }

    /**
     * Creates a html panel instance.
     * @param url the url
     */
    public HtmlPanel(URL url) {
        this();
        setHtml(loadText(url));
    }

    /**
     * Creates a html panel instance.
     * @param url the url
     * @param context the context
     */
    private HtmlPanel(URL url, Map<String, ?> context) {
        this();
        String template = loadText(url);
        String populatedTemplate = populateTokens(template, context);
        setHtml(populatedTemplate);
    }

    /**
     * Creates a html panel instance.
     * @param html the html
     */
    public HtmlPanel(String html) {
        this();
        setHtml(html);
    }

    /**
     * Replaces template tokens with values from the supplied context.
     * @param template the template
     * @param context the context
     * @return the populated HTML
     */
    static String populateTokens(String template, Object context) {
        StringTokenizer tokenizer = new StringTokenizer(template, "$");
        StringBuilder output = new StringBuilder();

        while (tokenizer.hasMoreTokens()) {

            output.append(tokenizer.nextToken());

            if (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                String value;
                if (context instanceof Map<?, ?> map) {
                    value = String.valueOf(map.get(token));
                } else {
                    try {
                        StringTokenizer t2 = new StringTokenizer(token, ".");
                        Object o = context;
                        while (t2.hasMoreTokens()) {
                            String subToken = t2.nextToken();
                            Field field = o.getClass().getField(subToken);
                            o = field.get(o);
                        }
                        value = o.toString();
                    } catch (Exception e) {
                        throw new NoSuchElementException(token);
                    }
                }
                output.append(value);
            }
        }

        return output.toString();
    }

    /**
     * Load the help text from file.
     */
    static String loadText(final URL htmlUrl) {
        try {
            InputStream in = htmlUrl.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(in)));
            String line;
            StringBuilder text = new StringBuilder();
            while ((line = br.readLine()) != null) {
                text.append(line);
            }
            return text.toString();
        } catch (Exception e) {
            logger.warn(htmlUrl.toString());
            return "Couldn't read: " + htmlUrl;
        }
    }

    /**
     * Sets the up.
     * @param modelRoot the model root
     * @param rendererRoot the renderer root
     * @param closeAction the close action
     */
    @Override
    public void setup(ModelRoot modelRoot, RendererRoot rendererRoot, Action closeAction) {
        done.setAction(closeAction);
    }

    /**
     * Sets the html.
     * @param s the s
     */
    void setHtml(String s) {
        htmlJLabel.setText(s);
    }
}
