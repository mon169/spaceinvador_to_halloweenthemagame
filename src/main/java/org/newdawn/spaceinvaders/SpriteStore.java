package org.newdawn.spaceinvaders;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.imageio.ImageIO;

/**
 * A resource manager for sprites in the game. Its often quite important
 * how and where you get your game resources from. In most cases
 * it makes sense to have a central resource loader that goes away, gets
 * your resources and caches them for future use.
 * <p>
 * [singleton]
 * <p>
 * @author Kevin Glass
 */
public class SpriteStore {
	/** The single instance of this class */
	private static SpriteStore single = new SpriteStore();
	
	/**
	 * Get the single instance of this class 
	 * 
	 * @return The single instance of this class
	 */
	public static SpriteStore get() {
		return single;
	}
	
    /** The cached sprite map, from reference to sprite instance */
    private HashMap<String, Sprite> sprites = new HashMap<String, Sprite>();
	
	/**
	 * Retrieve a sprite from the store
	 * 
	 * @param ref The reference to the image to use for the sprite
	 * @return A sprite instance containing an accelerate image of the request reference
	 */
    public Sprite getSprite(String ref) {
		// if we've already got the sprite in the cache
		// then just return the existing version
        if (sprites.get(ref) != null) {
            return sprites.get(ref);
		}
		
		// otherwise, go away and grab the sprite from the resource
		// loader
		BufferedImage sourceImage = null;
		
        try {
			// The ClassLoader.getResource() ensures we get the sprite
			// from the appropriate place, this helps with deploying the game
			// with things like webstart. You could equally do a file look
			// up here.
			URL url = this.getClass().getClassLoader().getResource(ref);
			
            if (url == null) {
                System.err.println("Can't find ref: " + ref + " — using placeholder sprite");
            } else {
                // use ImageIO to read the image in
                sourceImage = ImageIO.read(url);
            }
		} catch (IOException e) {
            System.err.println("Failed to load: " + ref + " — using placeholder sprite");
		}
		
        // create an accelerated image (original size if available, otherwise 1x1 placeholder)
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        int width = (sourceImage != null) ? sourceImage.getWidth() : 1;
        int height = (sourceImage != null) ? sourceImage.getHeight() : 1;
        Image image = gc.createCompatibleImage(width, height, Transparency.BITMASK);
		
		// draw our source image into the accelerated image
        if (sourceImage != null) {
            image.getGraphics().drawImage(sourceImage,0,0,null);
        }
		
		// create a sprite, add it the cache then return it
		Sprite sprite = new Sprite(image);
        sprites.put(ref, sprite);
		
		return sprite;
	}
	
	/**
	 * Utility method to handle resource loading failure
	 * 
	 * @param message The message to display on failure
	 */
    // removed: fail(String) — use stderr logs above instead
}