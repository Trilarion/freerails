package experimental;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import javax.imageio.ImageIO;
import jfreerails.client.common.ImageManager;
import jfreerails.client.renderer.TrainImages;
import jfreerails.world.cargo.CargoType;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.train.WagonType;


public class WagonRenderer {
    private HashMap typeColors = new HashMap();
    private static GraphicsConfiguration defaultConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                                                                   .getDefaultScreenDevice()
                                                                                   .getDefaultConfiguration();
    private experimental.ViewPerspective viewPerspective = experimental.ViewPerspective.OVERHEAD;
    private int trainType = WagonType.PASSENGER;
    private SideOnViewImageSize sideOnViewSize = new SideOnViewImageSize(10);
    private OneTileMoveVector direction = OneTileMoveVector.NORTH;
    private BufferedImage[][] trains;

    private void rendererTrainWithoutBuffer(Graphics g, Point p) {
        experimental.TrainTypeRenderer ttv = (experimental.TrainTypeRenderer)typeColors.get(new Integer(
                    trainType));
        Color c = ttv.getColor();

        g.setColor(c);

        if (experimental.ViewPerspective.OVERHEAD == viewPerspective) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.rotate(direction.getDirection(), p.x, p.y);
            g2.fillRect(p.x - 5, p.y - 10, 10, 20);
        } else {
            int width = sideOnViewSize.getWidth();
            int height = sideOnViewSize.getHeight();
            g.fillRect(p.x + 25 - width / 2, p.y + 25 - width / 2, width, height);
        }
    }

    public void setDirection(OneTileMoveVector direction) {
        this.direction = direction;
    }

    public void setTrainTypes(int type) {
        this.trainType = type;
    }

    private void setup() {
        trains = new BufferedImage[WagonType.NUMBER_OF_CATEGORIES][8];

        int row = 0;
        int column = 0;
        Iterator trainTypes = typeColors.keySet().iterator();

        while (trainTypes.hasNext()) {
            row = 0;

            Integer typeNumber = (Integer)trainTypes.next();
            column = typeNumber.intValue();
            this.setTrainTypes(typeNumber.intValue());

            OneTileMoveVector[] vectors = OneTileMoveVector.getList();

            for (int i = 0; i < vectors.length; i++) {
                trains[column][row] = defaultConfiguration.createCompatibleImage(30,
                        30, Transparency.TRANSLUCENT);

                Graphics g = trains[column][row].getGraphics();
                row++;
                this.setDirection(vectors[i]);

                Point p = new Point(15, 15);
                this.rendererTrainWithoutBuffer(g, p);
            }
        }
    }

    public WagonRenderer() {
        typeColors.put(new Integer(WagonType.MAIL),
            new experimental.TrainTypeRenderer(Color.WHITE));

        typeColors.put(new Integer(WagonType.PASSENGER),
            new experimental.TrainTypeRenderer(Color.BLUE));

        typeColors.put(new Integer(WagonType.FAST_FREIGHT),
            new experimental.TrainTypeRenderer(Color.YELLOW));

        typeColors.put(new Integer(WagonType.SLOW_FREIGHT),
            new experimental.TrainTypeRenderer(new Color(128, 0, 0)));

        typeColors.put(new Integer(WagonType.BULK_FREIGHT),
            new experimental.TrainTypeRenderer(Color.BLACK));

        typeColors.put(new Integer(WagonType.ENGINE),
            new experimental.TrainTypeRenderer(Color.LIGHT_GRAY));
        setup();
    }

    public static void main(String[] args) {
        try {
            WagonRenderer wagonRenderer = new WagonRenderer();

            writeImages(wagonRenderer, "mail", WagonType.MAIL);
            writeImages(wagonRenderer, "passenger", WagonType.PASSENGER);
            writeImages(wagonRenderer, "goods", WagonType.FAST_FREIGHT);
            writeImages(wagonRenderer, "steel", WagonType.SLOW_FREIGHT);
            writeImages(wagonRenderer, "coal", WagonType.BULK_FREIGHT);
            writeImages(wagonRenderer, "norris", WagonType.ENGINE);
            writeImages(wagonRenderer, "grasshopper", WagonType.ENGINE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeImages(WagonRenderer wagonRenderer,
        String typeName, int type) throws IOException {
        OneTileMoveVector[] vectors = OneTileMoveVector.getList();

        for (int i = 0; i < vectors.length; i++) {
            String fileName = typeName + "_" + vectors[i].toAbrvString() +
                ".png";
            File file = new File("src/jfreerails/data/trains/" + fileName);

            RenderedImage image = wagonRenderer.trains[type][vectors[i].getNumber()];
            ImageIO.write(image, "PNG", file);
        }
    }

    public Image generateSideOnImage(int type) {
        Image image = defaultConfiguration.createCompatibleImage(200, 100,
                Transparency.BITMASK);
        Graphics g = image.getGraphics();
        experimental.TrainTypeRenderer ttv = (experimental.TrainTypeRenderer)typeColors.get(new Integer(
                    type));
        Color c = ttv.getColor();
        g.setColor(c);
        g.fillRect(10, 25, 180, 55);
        g.setColor(Color.BLACK);
        g.fillArc(40, 80, 20, 20, 0, 360);
        g.fillArc(140, 80, 20, 20, 0, 360);

        return image;
    }

    public static void writeImages(ImageManager imageManager, ReadOnlyWorld w) {
        WagonRenderer wagonRenderer = new WagonRenderer();

        for (int j = 0; j < w.size(SKEY.CARGO_TYPES); j++) {
            CargoType cargoType = (CargoType)w.get(SKEY.CARGO_TYPES, j);
            String name = cargoType.getName();
            String category = cargoType.getCategory();

            int type = 0;

            if (category.equals("Mail")) {
                type = WagonType.MAIL;
            } else if (category.equals("Passengers")) {
                type = WagonType.PASSENGER;
            } else if (category.equals("Fast_Freight")) {
                type = WagonType.FAST_FREIGHT;
            } else if (category.equals("Slow_Freight")) {
                type = WagonType.SLOW_FREIGHT;
            } else if (category.equals("Bulk_Freight")) {
                type = WagonType.BULK_FREIGHT;
            } else {
                throw new IllegalArgumentException(category);
            }

            OneTileMoveVector[] vectors = OneTileMoveVector.getList();

            for (int i = 0; i < vectors.length; i++) {
                String fileName = TrainImages.generateOverheadFilename(name, i);

                Image image = wagonRenderer.trains[type][vectors[i].getNumber()];
                imageManager.setImage(fileName, image);
            }

            String fileName = TrainImages.generateSideOnFilename(name);

            imageManager.setImage(fileName,
                wagonRenderer.generateSideOnImage(type));
        }
    }
}