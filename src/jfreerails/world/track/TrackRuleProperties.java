package jfreerails.world.track;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.Money;

/**
 * Stores some of the properties of a track type.
 * 
 * @author Luke
 */
final public class TrackRuleProperties implements FreerailsSerializable {
	private static final long serialVersionUID = 3618704101752387641L;

	private final boolean enableDoubleTrack;

	private final Money maintenanceCost;

	private final Money price;

	private final Money fixedCost;

	private final TrackRule.TrackCategories category;

	private final int rGBvalue;

	private final int stationRadius;

	private final String typeName;

	public TrackRuleProperties(int rgb, boolean doubleTrack, String name,
			TrackRule.TrackCategories c, int radius, int price,
			int maintenance, int fixedCost) {
		stationRadius = radius;
		rGBvalue = rgb;
		enableDoubleTrack = doubleTrack;
		typeName = name;
		category = c;
		this.price = new Money(price);
		this.maintenanceCost = new Money(maintenance);
		this.fixedCost = new Money(fixedCost);
	}

	public boolean equals(Object o) {
		if (o instanceof TrackRuleProperties) {
			TrackRuleProperties test = (TrackRuleProperties) o;

			if (rGBvalue == test.getRGBvalue()
					&& enableDoubleTrack == test.isEnableDoubleTrack()
					&& typeName.equals(test.getTypeName())
					&& category == test.category
					&& stationRadius == test.stationRadius) {
				return true;
			}
			return false;
		}
		return false;
	}

	public Money getMaintenanceCost() {
		return maintenanceCost;
	}

	public Money getPrice() {
		return price;
	}

	private int getRGBvalue() {
		return rGBvalue;
	}

	public int getStationRadius() {
		return stationRadius;
	}

	public String getTypeName() {
		return typeName;
	}

	public int hashCode() {
		int result;
		result = rGBvalue;
		result = 29 * result + (enableDoubleTrack ? 1 : 0);
		result = 29 * result + typeName.hashCode();
		result = 29 * result + category.hashCode();
		result = 29 * result + stationRadius;
		result = 29 * result + price.hashCode();
		result = 29 * result + fixedCost.hashCode();
		result = 29 * result + maintenanceCost.hashCode();

		return result;
	}

	public boolean isEnableDoubleTrack() {
		return enableDoubleTrack;
	}

	public boolean isStation() {
		return category.equals(TrackRule.TrackCategories.station);
	}

	public TrackRule.TrackCategories getCategory() {
		return category;
	}

	public Money getFixedCost() {
		return fixedCost;
	}
}