package jfreerails.client.top;

import java.awt.Component;
import java.awt.Point;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import jfreerails.client.renderer.StationRadiusRenderer;
import jfreerails.controller.StationBuilder;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.track.TrackRule;

/**
 * This JPopupMenu displays the list of station types that
 * are available and builds the type that is selected. 
 * @author Luke Lindsay 08-Nov-2002
 *
 */
public class StationTypesPopup extends JPopupMenu {

	Point tileToBuildStationOn;

	StationBuilder stationBuilder;

	StationRadiusRenderer stationRadiusRenderer;

	PopupMenuListener popupMenuListener;

	public StationTypesPopup() {

	}
	
	public boolean canBuiltStationHere(Point p){
		return stationBuilder.canBuiltStationHere(p);
	}

	public void setup(StationBuilder sb, StationRadiusRenderer srr) {
		stationBuilder = sb;
		stationRadiusRenderer = srr;
		World w = sb.getWorld();
		this.removeAll();
		this.removePopupMenuListener(popupMenuListener);
		popupMenuListener = new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				stationRadiusRenderer.hide();
			}

			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				stationRadiusRenderer.show();
				stationRadiusRenderer.setPosition(
					tileToBuildStationOn.x,
					tileToBuildStationOn.y);

			}
		};
		this.addPopupMenuListener(popupMenuListener);

		for (int i = 0; i < w.size(KEY.TRACK_RULES); i++) {
			final int trackRuleNumber = i;
			final TrackRule trackRule = (TrackRule) w.get(KEY.TRACK_RULES, i);
			if (trackRule.isStation()) {
				String trackType = trackRule.getTypeName();
				final JMenuItem rbMenuItem =
					new JMenuItem("Build " + trackType);
				rbMenuItem
					.addActionListener(new java.awt.event.ActionListener() {

					public void actionPerformed(
						java.awt.event.ActionEvent actionEvent) {
						stationBuilder.setStationType(trackRuleNumber);
						stationBuilder.buildStation(tileToBuildStationOn);
					}
				});

				//Show the relevant station radius when the station type's menu item gets focus.
				rbMenuItem.addChangeListener(new ChangeListener() {

					public void stateChanged(ChangeEvent e) {
						if (rbMenuItem.isArmed()) {
							stationRadiusRenderer.setRadius(
								trackRule.getStationRadius());
						}
					}
				});
				this.add(rbMenuItem);
			}
		}

	}

	public void show(Component invoker, int x, int y, Point tile) {
		tileToBuildStationOn = tile;
		super.show(invoker, x, y);
	}
}
