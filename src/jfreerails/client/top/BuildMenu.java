
/*
* BuildMenu.java
*
* Created on 30 July 2001, 06:49
*/
package jfreerails.client.top;
import jfreerails.controller.TrackMoveProducer;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.track.TrackRule;


/**
*
* @author  Luke Lindsay
*/

final public class BuildMenu extends javax.swing.JMenu {

	private TrackMoveProducer trackBuilder;

	/** Creates new BuildMenu */

	public BuildMenu() {
		super();
	}

	public void setup(
		World w,
		TrackMoveProducer tb) {

		this.removeAll();
		this.trackBuilder = tb;
		this.setText("Build");
		javax.swing.ButtonGroup trackTypesGroup = new javax.swing.ButtonGroup();
		javax.swing.ButtonGroup buildRemoveOrUpgrade =
			new javax.swing.ButtonGroup();
		javax.swing.JRadioButtonMenuItem buildTrackMenuItem =
			new javax.swing.JRadioButtonMenuItem("Build Track");
		buildTrackMenuItem
			.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(
				java.awt.event.ActionEvent actionEvent) {
				System.out.println("build track");
				trackBuilder.setTrackBuilderMode(TrackMoveProducer.BUILD_TRACK);
			}
		});

		/*Set build track as the default*/
		buildTrackMenuItem.setSelected(true);
		trackBuilder.setTrackBuilderMode(TrackMoveProducer.BUILD_TRACK);
		buildRemoveOrUpgrade.add(buildTrackMenuItem);
		this.add(buildTrackMenuItem);
		javax.swing.JRadioButtonMenuItem RemoveTrackMenuItem =
			new javax.swing.JRadioButtonMenuItem("Remove Track");
		RemoveTrackMenuItem
			.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(
				java.awt.event.ActionEvent actionEvent) {
				System.out.println("remove track");
				trackBuilder.setTrackBuilderMode(TrackMoveProducer.REMOVE_TRACK);
			}
		});
		buildRemoveOrUpgrade.add(RemoveTrackMenuItem);
		this.add(RemoveTrackMenuItem);
		javax.swing.JRadioButtonMenuItem upgradeTrackMenuItem =
			new javax.swing.JRadioButtonMenuItem("Upgrade Track");
		upgradeTrackMenuItem
			.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(
				java.awt.event.ActionEvent actionEvent) {
				System.out.println("upgrade track");
				trackBuilder.setTrackBuilderMode(TrackMoveProducer.UPGRADE_TRACK);
			}
		});
		buildRemoveOrUpgrade.add(upgradeTrackMenuItem);
		this.add(upgradeTrackMenuItem);
		this.addSeparator();
		for (int i = 0; i < w.size(KEY.TRACK_RULES); i++) {
			final int trackRuleNumber = i;
			TrackRule trackRule = (TrackRule)w.get(KEY.TRACK_RULES, i);
			if (!trackRule.isStation()) { //Stations get built by pressing F8
				String trackType = trackRule.getTypeName();
				javax.swing.JRadioButtonMenuItem rbMenuItem =
					new javax.swing.JRadioButtonMenuItem("Build " + trackType);
				rbMenuItem
					.addActionListener(new java.awt.event.ActionListener() {

					public void actionPerformed(
						java.awt.event.ActionEvent actionEvent) {
						trackBuilder.setTrackRule(trackRuleNumber);
					}
				});
				if (0 == i) {
					rbMenuItem.setSelected(true);
				}
				trackTypesGroup.add(rbMenuItem);
				this.add(rbMenuItem);
			}
		}
	}
}
