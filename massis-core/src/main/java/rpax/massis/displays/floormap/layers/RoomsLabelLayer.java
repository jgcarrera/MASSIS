package rpax.massis.displays.floormap.layers;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import rpax.massis.model.building.Floor;
import rpax.massis.model.building.SimRoom;

public class RoomsLabelLayer extends FloorMapLayer {

    private static final Font NAME_FONT = new Font("Georgia", Font.BOLD, 30);

    public RoomsLabelLayer(boolean enabled)
    {
        super(enabled);
    }

    @Override
    protected void draw(Floor f, Graphics2D g)
    {
        g.setColor(Color.orange);
        Font originalF = g.getFont();
        g.setFont(NAME_FONT);
        for (SimRoom r : f.getRooms())
        {
            char[] chars = r.toString().toCharArray();
            g.setColor(Color.black);
            g.drawChars(chars, 0, chars.length, (int) r.getXY().x,
                    (int) r.getXY().y);
        }
        g.setFont(originalF);
    }

    @Override
    public String getName()
    {
        return "Room Labels";
    }
}
