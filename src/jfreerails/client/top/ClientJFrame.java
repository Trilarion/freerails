package jfreerails.client.top;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import jfreerails.controller.TextMessageHandler;
import jfreerails.controller.TextMessenger;

/** 
 *
 * @author Luke Lindsay
 */
final public class ClientJFrame extends javax.swing.JFrame implements TextMessenger {

	public ClientJFrame(
		GUIComponentFactory gUIComponentFactory) {
		this.mainMapViewContainer = gUIComponentFactory.createMainMap();
		this.overviewMapViewContainer = gUIComponentFactory.createOverviewMap();

		this.buildMenu = gUIComponentFactory.createBuildMenu();
		this.gameMenu = gUIComponentFactory.createGameMenu();
		this.displayMenu = gUIComponentFactory.createDisplayMenu();
		TextMessageHandler.setMessengerBoy(this);
		initComponents();
	}

	private void initComponents() {
		textMessage = new javax.swing.JLabel("Message");

		
		jMenuBar1.add(gameMenu);
		jMenuBar1.add(buildMenu);
		jMenuBar1.add(displayMenu);
		setJMenuBar(jMenuBar1);
		
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				exitForm(evt);
			}
		});

		getContentPane().setLayout(new java.awt.GridBagLayout());
		java.awt.GridBagConstraints gridBagConstraints3,
			gridBagConstraints2,
			gridBagConstraints1;

		initLayout();
		
	}

	private void initLayout() {
		java.awt.GridBagConstraints gridBagConstraints3;
		java.awt.GridBagConstraints gridBagConstraints2;
		java.awt.GridBagConstraints gridBagConstraints1;
		gridBagConstraints1 = new java.awt.GridBagConstraints();
		gridBagConstraints1.gridheight = 1;
		gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints1.weightx = 1.0;
		gridBagConstraints1.weighty = 1.0;
		getContentPane().add(mainMapViewContainer, gridBagConstraints1);

		gridBagConstraints3 = new java.awt.GridBagConstraints();
		gridBagConstraints3.gridheight = 1;
		gridBagConstraints3.gridy = 1;
		gridBagConstraints3.gridx = 0;
		gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints3.weightx = 1.0;
		gridBagConstraints3.weighty = 0;
		getContentPane().add(textMessage, gridBagConstraints3);

		overviewMapViewContainer.setMinimumSize(
			overviewMapViewContainer.getPreferredSize());
		gridBagConstraints2 = new java.awt.GridBagConstraints();
		gridBagConstraints2.gridx = 1;
		gridBagConstraints2.gridy = 0;
		gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTH;
		getContentPane().add(overviewMapViewContainer, gridBagConstraints2);
	}

	

	/** Exit the Application */
	private void exitForm(java.awt.event.WindowEvent evt) {
		System.exit(0);
	}

	public void displayMessage(java.lang.String message) {
		//System.out.println(message);
		textMessage.setText(message);
	}
	

	private JComponent mainMapViewContainer;

	private JComponent overviewMapViewContainer;
	private JLabel textMessage;
	protected JMenuBar jMenuBar1 = new javax.swing.JMenuBar();

	protected JMenu buildMenu;
	protected JMenu gameMenu;
	protected JMenu displayMenu;

}
