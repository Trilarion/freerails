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
 * Provides the broker frame functionality.
 */
class BrokerFrame extends JInternalFrame {

    @Serial
    private static final long serialVersionUID = 4121409622587815475L;
    private static final TaggedLogger logger = Logger.tag("BrokerFrame");
    private JButton done;
    private JLabel htmlJLabel;
    JMenuItem issueBond;
    JMenuItem repayBond;
    JMenu stocks;

    /**
     * Creates new form BrokerFrame
     */
    BrokerFrame() {
        GridBagConstraints gridBagConstraints;

        JScrollPane jScrollPane1 = new JScrollPane();
        JPanel jPanel1 = new JPanel();
        htmlJLabel = new JLabel();
        done = new JButton();
        JMenuBar brokerMenu = new JMenuBar();
        JMenu bonds = new JMenu();
        issueBond = new JMenuItem();
        repayBond = new JMenuItem();
        stocks = new JMenu();

        getContentPane().setLayout(new GridBagLayout());

        jScrollPane1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jPanel1.setLayout(new BorderLayout());

        htmlJLabel.setFont(new Font("Dialog", 0, 12));
        htmlJLabel.setText("sdfa");
        htmlJLabel.setVerticalAlignment(SwingConstants.TOP);
        htmlJLabel.setVerticalTextPosition(SwingConstants.TOP);
        jPanel1.add(htmlJLabel, BorderLayout.CENTER);

        jScrollPane1.setViewportView(jPanel1);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jScrollPane1, gridBagConstraints);

        done.setText("Close");
        done.setVerifyInputWhenFocusTarget(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(7, 7, 7, 7);
        getContentPane().add(done, gridBagConstraints);

        bonds.setText("Bonds");
        issueBond.setText("Issue Bond");
        bonds.add(issueBond);

        repayBond.setText("Repay Bond");
        bonds.add(repayBond);

        brokerMenu.add(bonds);

        stocks.setText("Stocks");
        brokerMenu.add(stocks);

        setJMenuBar(brokerMenu);
        pack();
    }

    /**
     * Creates a broker frame instance.
     *
     * @param url the url parameter
     */
    private BrokerFrame(URL url) {
        this();
        setHtml(loadText(url));
    }

    /**
     * Creates a broker frame instance.
     *
     * @param url the url parameter
     * @param context the context parameter
     */
    private BrokerFrame(URL url, Map<String, ?> context) {
        this();
        String template = loadText(url);
        String populatedTemplate = populateTokens(template, context);
        setHtml(populatedTemplate);
    }

    /**
     * Creates a broker frame instance.
     *
     * @param html the html parameter
     */
    private BrokerFrame(String html) {
        this();
        setHtml(html);
    }

    /**
     * Load the help text from file.
     */
    public static String loadText(final URL htmlUrl) {
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
     * Performs the populate tokens operation.
     *
     * @param template the template parameter
     * @param context the context parameter
     * @return the populate tokens produced by this method
     */
    public static String populateTokens(String template, Object context) {
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
                        Field field = context.getClass().getField(token);
                        value = field.get(context).toString();
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
     * Sets the up.
     *
     * @param m the model parameter
     * @param vl the vl parameter
     * @param closeAction the action invoked when the panel is closed
     */
    void setup(ModelRoot m, RendererRoot vl, Action closeAction) {
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
