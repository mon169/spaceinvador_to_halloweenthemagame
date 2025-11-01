package org.newdawn.spaceinvaders.manager;

import java.awt.Graphics2D;
import org.newdawn.spaceinvaders.Sprite;

public class BackgroundManager {
    public void draw(Graphics2D g, Sprite s, int offY) {
        if (s == null) return;
        int iw = s.getWidth(), ih = s.getHeight();
        double scale = Math.max(800.0 / iw, 600.0 / ih);
        int dw = (int) Math.round(iw * scale);
        int dh = (int) Math.round(ih * scale);
        int dx = (800 - dw) / 2;
        int sy = -(offY % dh);
        s.drawScaled(g, dx, sy,    dw, dh);
        s.drawScaled(g, dx, sy+dh, dw, dh);
    }
}
