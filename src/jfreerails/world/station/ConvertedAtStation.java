package jfreerails.world.station;

import jfreerails.world.common.FreerailsSerializable;


public class ConvertedAtStation implements FreerailsSerializable {
    public static final int NOT_CONVERTED = Integer.MIN_VALUE;
    private final int[] convertedTo;

    public ConvertedAtStation(int[] convertedTo) {
        this.convertedTo = (int[])convertedTo.clone(); //defensive copy.
    }

    public static ConvertedAtStation emptyInstance(int numberOfCargoTypes) {
        int[] convertedTo = emptyConversionArray(numberOfCargoTypes);

        return new ConvertedAtStation(convertedTo);
    }

    public static int[] emptyConversionArray(int numberOfCargoTypes) {
        int[] convertedTo = new int[numberOfCargoTypes];

        for (int i = 0; i < numberOfCargoTypes; i++) {
            convertedTo[i] = NOT_CONVERTED;
        }

        return convertedTo;
    }

    public boolean isCargoConverted(int cargoNumber) {
        if (NOT_CONVERTED == convertedTo[cargoNumber]) {
            return false;
        } else {
            return true;
        }
    }

    public int getConversion(int cargoNumber) {
        return convertedTo[cargoNumber];
    }

    public boolean equals(Object o) {
        if (o instanceof ConvertedAtStation) {
            ConvertedAtStation test = (ConvertedAtStation)o;

            if (this.convertedTo.length != test.convertedTo.length) {
                return false;
            }

            for (int i = 0; i < convertedTo.length; i++) {
                if (convertedTo[i] != test.convertedTo[i]) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }
}