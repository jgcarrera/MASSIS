package rpax.massis.displays.floormap.layers;

import java.awt.Color;
import java.awt.Graphics2D;

import rpax.massis.model.agents.DefaultAgent;
import rpax.massis.model.building.Floor;

/**
 * Draws the vision area of each agent
 *
 * @author rpax
 *
 */
public class VisionRadioLayer extends FloorMapLayer {

    public VisionRadioLayer(boolean enabled)
    {
        super(enabled);

    }

    @Override
    protected void draw(Floor f, Graphics2D g)
    {
        for (DefaultAgent p : f.getPeople())
        {
            if (!p.isDynamic())
            {
                continue;
            }

            g.setColor(Color.red);
            g.draw(p.getVisionRadioShape());

        }
    }

    @Override
    public String getName()
    {
        return "Vision radio view";
    }
}