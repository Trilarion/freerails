package jfreerails.controller;


/**
 * This Event signifies the change of speedof simulator. It is used to display
 * the text paused, when the game is paused.
 */
public class SpeedChangedCommand extends ServerCommand {
    private int ticksPerSecond;

    /**
 * Creates a command informing about actual game speed.
 * @param speed actual game speed
 */
    public SpeedChangedCommand(int ticksPerSecond) {
        this.ticksPerSecond = ticksPerSecond;
    }

    public int getTicksPerSecond() {
        return ticksPerSecond;
    }
}