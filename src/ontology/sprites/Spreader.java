package ontology.sprites;

import core.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import ontology.Types;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 21/10/13
 * Time: 18:31
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Spreader extends Flicker
{
    public double spreadprob;

    public Spreader(){}

    public Spreader(Vector2d position, Dimension size, SpriteContent cnt)
    {
        //Init the sprite
        this.init(position, size);

        //Specific class default parameter values.
        loadDefaults();

        //Parse the arguments.
        this.parseParameters(cnt);
    }

    protected void loadDefaults()
    {
        super.loadDefaults();
        spreadprob = 1.0;
    }

    public void update(Game game)
    {
        super.update(game);
        if(age == 2)
        {
            for(Vector2d u : Types.BASEDIRS)
            {
                if(game.getRandomGenerator().nextDouble() < spreadprob)
                {
                    game.addSprite(this.getType(), new Vector2d(this.lastrect.x + u.x*this.lastrect.width,
                                                    this.lastrect.y + u.y*this.lastrect.height));
                }
            }
        }

    }


    public VGDLSprite copy()
    {
        Spreader newSprite = new Spreader();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        Spreader targetSprite = (Spreader) target;
        targetSprite.spreadprob = this.spreadprob;
        super.copyTo(targetSprite);
    }
}