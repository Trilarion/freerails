
/*
 *  TrackRule.java
 *
 *  Created on 15 July 2001, 19:53
 */
package jfreerails.world.track;

import java.util.Iterator;

import jfreerails.world.common.OneTileMoveVector;

/**
 *  This class encapsulates the rules that apply to a type of track node. They
 *  concern: the legal routes trains can travel across the node, whether the
 *  node's track can be doubled, on which terrain types it can be built, and the
 *  maximum number of consecutive nodes of this type (used for bridges and
 *  tunnels).
 *
 *@author     Luke Lindsay
 *    09 October 2001
 *@version    0.1
 */

final public class TrackRuleImpl implements TrackRule {

	private final TrackRuleProperties properties;

	private final LegalTrackConfigurations legalConfigurations;

	private final LegalTrackPlacement legalTrackPlacement;

	/*
	 *  Track templates are 9 bit values, so there are 512 possible templates.
	 *  If legalTrackTemplate[x]==true, then x is a legal track-template.
	 *  Example:
	 *  000
	 *  111
	 *  000
	 *  This represents a horizontal straight.
	 */

	public TrackRuleImpl(
		TrackRuleProperties p,
		LegalTrackConfigurations lc,
		LegalTrackPlacement ltp) {
		if (null == p || null == lc || null == ltp) {
			throw new java.lang.IllegalArgumentException();
		}
		properties = p;
		legalConfigurations = lc;
		legalTrackPlacement = ltp;
	}

	public boolean testTrackPieceLegality(int trackTemplateToTest) {
		TrackConfiguration trackConfiguration =
			TrackConfiguration.getFlatInstance(trackTemplateToTest);

		return legalConfigurations.trackConfigurationIsLegal(trackConfiguration);
	}

	public OneTileMoveVector[] getLegalRoutes(OneTileMoveVector directionComingFrom) {

		//TODO add code..
		return null;
	}

	public boolean canBuildOnThisTerrainType(String TerrainType) {

		return legalTrackPlacement.canBuildOnThisTerrain(TerrainType);
	}

	public boolean isDoubleTrackEnabled() {
		return properties.isDoubleTrackEnabled();
	}
	public String getTypeName() {
		return properties.getTypeName();
	}
	public int getMaximumConsecutivePieces() {
		return legalConfigurations.getMaximumConsecutivePieces();
	}
	public int getRuleNumber() {
		return properties.getRuleNumber();
	}
	public Iterator getLegalConfigurationsIterator() {
		return legalConfigurations.getLegalConfigurationsIterator();
	}

	public TrackPiece getTrackPiece(TrackConfiguration config) {
		return new TrackPieceImpl(config, this);
	}

	public boolean trackPieceIsLegal(TrackConfiguration config) {
		return legalConfigurations.trackConfigurationIsLegal(config);
	}

	public boolean isStation() {
		return properties.isStation();
	}

	public boolean equals(Object o) {
		if (o instanceof TrackRuleImpl) {
			TrackRuleImpl trackRuleImpl = (TrackRuleImpl) o;
			boolean propertiesFieldsEqual = this.properties.equals(trackRuleImpl.getProperties());
			boolean legalConfigurationsEqual =
				this.legalConfigurations.equals(trackRuleImpl.getLegalConfigurations());
			boolean legalTrackPlacementEqual =
				this.legalTrackPlacement.equals(trackRuleImpl.getLegalTrackPlacement());
			if (propertiesFieldsEqual && legalConfigurationsEqual && legalTrackPlacementEqual) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public LegalTrackConfigurations getLegalConfigurations() {
		return legalConfigurations;
	}

	public LegalTrackPlacement getLegalTrackPlacement() {
		return legalTrackPlacement;
	}

	public TrackRuleProperties getProperties() {
		return properties;
	}

}