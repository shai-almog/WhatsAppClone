/*
 * Copyright (c) 2012, Codename One and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Codename One designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *  
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please contact Codename One through http://www.codenameone.com/ if you 
 * need additional information or have any questions.
 */

package com.codename1.whatsapp.components;

import com.codename1.ui.Component;
import com.codename1.ui.Display;
import com.codename1.ui.Graphics;
import com.codename1.ui.Image;
import com.codename1.ui.Stroke;
import com.codename1.ui.geom.GeneralPath;
import com.codename1.ui.geom.Rectangle;
import com.codename1.ui.plaf.Border;
import com.codename1.ui.plaf.Style;
import static com.codename1.ui.CN.*;

/**
 * Based on the code of RoundRectBorder
 */
public class ChatBubbleBorder extends Border {
    private static final String CACHE_KEY = "cn1$$-rrbcache";
        
    /**
     * The color of the edge of the border if applicable
     */
    private int strokeColor = 0;
    
    /**
     * The opacity of the edge of the border if applicable
     */
    private int strokeOpacity = 255;

    private Stroke stroke;

    
    /**
     * The thickness of the edge of the border if applicable, 0 
     * if no stroke is needed
     */
    private float strokeThickness;

    /**
     * True if the thickness of the stroke is in millimeters
     */
    private boolean strokeMM;

    /**
     * The spread of the shadow in millimeters
     */
    private float shadowSpread;

    /**
     * The opacity of the shadow between 0 and 255
     */
    private int shadowOpacity = 0;

    /**
     * X axis bias of the shadow between 0 and 1 where 0 is to the top 
     * and 1 is to the bottom, defaults to 0.5
     */
    private float shadowX = 0.5f;

    /**
     * Y axis bias of the shadow between 0 and 1 where 0 is to the left 
     * and 1 is to the right, defaults to 0.5
     */
    private float shadowY = 0.5f;

    /**
     * The Gaussian blur size
     */
    private float shadowBlur = 10;

    /**
     * The radius of the corners in millimeters
     */
    private float cornerRadius = 2;
    
    /**
     * True if the corners are bezier curves, otherwise the corners 
     * are drawn as a regular arc
     */
    private boolean bezierCorners;
        
    // these allow us to have more than one border per component in 
    // cache which is important for selected/unselected/pressed values
    private static int instanceCounter;
    private final int instanceVal;

    private boolean leftArrow;
    private boolean rightArrow;
    
    private ChatBubbleBorder() {
        shadowSpread = Display.getInstance().convertToPixels(0.2f);
        instanceCounter++;
        instanceVal = instanceCounter;
    }
    
    /**
     * Creates a flat border with styles derived from the component UIID
     * @return a border instance
     */
    public static ChatBubbleBorder create() {
        return new ChatBubbleBorder();
    }
    
    /**
     * Sets the opacity of the stroke line around the border
     * @param strokeOpacity the opacity from 0-255 where 255 is 
     *      completely opaque 
     * @return border instance so these calls can be chained
     */
    public ChatBubbleBorder strokeOpacity(int strokeOpacity) {
        this.strokeOpacity = strokeOpacity;
        return this;
    }
    
    /**
     * Sets the stroke color of the border
     * @param strokeColor the color 
     * @return border instance so these calls can be chained
     */
    public ChatBubbleBorder strokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
        return this;
    }

    /**
     * Sets the stroke of the border
     * @param stroke the stroke object
     * @return border instance so these calls can be chained
     */
    public ChatBubbleBorder stroke(Stroke stroke) {
    	if (stroke != null) {
    		strokeThickness = stroke.getLineWidth();
    		strokeMM = false;
    	}
        this.stroke = stroke;
        return this;
    }

    /**
     * Sets the stroke of the border
     * @param stroke the thickness of the stroke object
     * @param mm set to true to indicate the value is in millimeters, 
     *      false indicates pixels
     * @return border instance so these calls can be chained
     */
    public ChatBubbleBorder stroke(float stroke, boolean mm) {
        strokeThickness = stroke;
        if(strokeThickness == 0) {
            this.stroke = null;
            return this;
        }
        strokeMM = mm;
        if(mm) {
            stroke = Display.getInstance().convertToPixels(stroke);
        }
        return stroke(new Stroke(stroke, Stroke.CAP_SQUARE, 
                                Stroke.JOIN_MITER, 1));
    }
    
    /**
     * Sets the spread of the shadow in millimeters i.e how much bigger is 
     * it than the actual border
     * @param shadowSpread the amount in millimeters representing the size 
     *              of the shadow
     * @return border instance so these calls can be chained
     */
    public ChatBubbleBorder shadowSpread(float shadowSpread) {
        this.shadowSpread = shadowSpread;
        return this;
    }

    /**
     * Sets the spread in pixels of the shadow i.e how much bigger is it 
     * than the actual border
     * @param shadowSpread the amount in pixels representing the size of 
     *          the shadow
     * @return border instance so these calls can be chained
     */
    public ChatBubbleBorder shadowSpread(int shadowSpread) {
        this.shadowSpread = shadowSpread * 100f / 
            Display.getInstance().convertToPixels(100f);
        return this;
    }

    /**
     * Sets the opacity of the shadow from 0 - 255 where 0 means no 
     * shadow and 255 means opaque black shadow
     * @param shadowOpacity the opacity of the shadow
     * @return border instance so these calls can be chained
     */
    public ChatBubbleBorder shadowOpacity(int shadowOpacity) {
        this.shadowOpacity = shadowOpacity;
        return this;
    }

    /**
     * The position of the shadow on the X axis where 0.5f means the center 
     * and higher values draw it to the right side
     * @param shadowX the position of the shadow between 0 - 1 where 0 
     *          equals left and 1 equals right
     * @return border instance so these calls can be chained
     */
    public ChatBubbleBorder shadowX(float shadowX) {
        this.shadowX = shadowX;
        return this;
    }

    /**
     * The position of the shadow on the Y axis where 0.5f means the 
     * center and higher values draw it to the bottom
     * @param shadowY the position of the shadow between 0 - 1 where 0 
     *      equals top and 1 equals bottom
     * @return border instance so these calls can be chained
     */
    public ChatBubbleBorder shadowY(float shadowY) {
        this.shadowY = shadowY;
        return this;
    }

    /**
     * The blur on the shadow this is the standard Gaussian blur radius
     * @param shadowBlur The blur on the shadow this is the standard 
     *          Gaussian blur radius
     * @return border instance so these calls can be chained
     */
    public ChatBubbleBorder shadowBlur(float shadowBlur) {
        this.shadowBlur = shadowBlur;
        return this;
    }
        
    /**
     * The radius of the corners in millimeters
     * 
     * @param cornerRadius the radius value
     * @return border instance so these calls can be chained
     */
    public ChatBubbleBorder cornerRadius(float cornerRadius) {
        this.cornerRadius = cornerRadius;
        return this;
    }
    
    /**
     * True if the corners are Bezier curves, otherwise the corners are 
     * drawn as a regular arc
     * 
     * @param bezierCorners true if the corners use a bezier curve 
     *          for drawing
     * @return border instance so these calls can be chained
     */
    public ChatBubbleBorder bezierCorners(boolean bezierCorners) {
        this.bezierCorners = bezierCorners;
        return this;
    }
    

    public ChatBubbleBorder leftArrow(boolean leftArrow) {
        this.leftArrow = leftArrow;
        return this;
    }
    
    public ChatBubbleBorder rightArrow(boolean rightArrow) {
        this.rightArrow = rightArrow;
        return this;
    }

        
    private Image createTargetImage(Component c, int w, int h, 
                boolean fast) {
        Image target = Image.createImage(w, h, 0);
        
        int shapeX = 0;
        int shapeY = 0;
        int shapeW = w;
        int shapeH = h;
        
        Graphics tg = target.getGraphics();
        tg.setAntiAliased(true);
                
        int shadowSpreadL = Display.getInstance().
                convertToPixels(shadowSpread);
        
        if(shadowOpacity > 0) {
            shapeW -= shadowSpreadL;
            shapeH -= shadowSpreadL;
            shapeX += Math.round(((float)shadowSpreadL) * shadowX);
            shapeY += Math.round(((float)shadowSpreadL) * shadowY);
                        
            // draw a gradient of sort for the shadow
            for(int iter = shadowSpreadL - 1 ; iter >= 0 ; iter--) {            
                tg.translate(iter, iter);
                int iterOpacity = Math.max(0, 
                    Math.min(255, 
                        (int)(shadowOpacity * 
                            (shadowSpreadL - iter)/(float)shadowSpreadL)));
                drawShape(tg, 0, shadowOpacity-iterOpacity, w - (iter * 2), 
                    h - (iter * 2));
                tg.translate(-iter, -iter);
            }
            
            if(Display.getInstance().isGaussianBlurSupported() && !fast) {
                Image blured = Display.getInstance().
                    gaussianBlurImage(target, shadowBlur/2);
                target = Image.createImage(w, h, 0);
                tg = target.getGraphics();
                tg.drawImage(blured, 0, 0);
                tg.setAntiAliased(true);
            }
        }
        tg.translate(shapeX, shapeY);

        GeneralPath gp = createShape(shapeW, shapeH);
        Style s = c.getStyle();
        if(s.getBgImage() == null ) {
            byte type = s.getBackgroundType();
            if(type == Style.BACKGROUND_IMAGE_SCALED || 
                    type == Style.BACKGROUND_NONE) {
                byte bgt = c.getStyle().getBgTransparency();
                if(bgt != 0) {
                    tg.setAlpha(bgt &0xff);
                    tg.setColor(s.getBgColor());
                    tg.fillShape(gp);
                }
                if(this.stroke != null && strokeOpacity > 0 && 
                        strokeThickness > 0) {
                    tg.setAlpha(strokeOpacity);
                    tg.setColor(strokeColor);
                    tg.drawShape(gp, this.stroke);
                }            
                return target;
            }
        }
        
        c.getStyle().setBorder(Border.createEmpty());
        tg.setClip(gp);
        s.getBgPainter().paint(tg, new Rectangle(0, 0, w, h));
        if(this.stroke != null && strokeOpacity > 0 && 
                strokeThickness > 0) {
            tg.setClip(0, 0, w, h);
            tg.setAlpha(strokeOpacity);
            tg.setColor(strokeColor);
            tg.drawShape(gp, this.stroke);
        }            
        c.getStyle().setBorder(this);
        return target;
    }    
    
    @Override
    public void paintBorderBackground(Graphics g, final Component c) {
        final int w = c.getWidth();
        final int h = c.getHeight();
        int x = c.getX();
        int y = c.getY();
        boolean antiAliased = g.isAntiAliased();
        g.setAntiAliased(true);
        try {
            if(shadowOpacity == 0) {
                Style s = c.getStyle();
                if(s.getBgImage() == null ) {
                    byte type = s.getBackgroundType();
                    if(type == Style.BACKGROUND_IMAGE_SCALED || 
                            type == Style.BACKGROUND_NONE) {
                        GeneralPath gp = createShape(w, h);
                        byte bgt = c.getStyle().getBgTransparency();
                        if(bgt != 0) {
                            int a = g.getAlpha();
                            g.setAlpha(bgt &0xff);
                            g.setColor(s.getBgColor());
                            g.translate(x, y);
                            g.fillShape(gp);
                            if(this.stroke != null && 
                                    strokeOpacity > 0 && 
                                    strokeThickness > 0) {
                                g.setAlpha(strokeOpacity);
                                g.setColor(strokeColor);
                                g.drawShape(gp, this.stroke);
                            }            
                            g.translate(-x, -y);
                            g.setAlpha(a);
                        }
                        if(this.stroke != null && strokeOpacity > 0 && 
                                strokeThickness > 0) {
                            int a = g.getAlpha();
                            g.setAlpha(strokeOpacity);
                            g.setColor(strokeColor);
                            g.translate(x, y);
                            g.drawShape(gp, this.stroke);
                            g.translate(-x, -y);
                            g.setAlpha(a);
                        }      
                        return;
                    }
                }        
            }
            if(w > 0 && h > 0) {
                Image background = (Image)
                    c.getClientProperty(CACHE_KEY + instanceVal);
                if(background != null && background.getWidth() == w && 
                        background.getHeight() == h) {
                    g.drawImage(background, x, y);
                    return;
                }
            } else {
                return;
            }

            Image target = createTargetImage(c, w, h, true);
            g.drawImage(target, x, y);
            c.putClientProperty(CACHE_KEY + instanceVal, target);

            // update the cache with a more refined version and repaint
            Display.getInstance().callSeriallyOnIdle(new Runnable() {
                public void run() {
                    if(w == c.getWidth() && h == c.getHeight()) {
                        Image target = createTargetImage(c, w, h, false);
                        c.putClientProperty(CACHE_KEY + instanceVal, 
                            target);
                        c.repaint();
                    }
                }
            });
        } finally {
            g.setAntiAliased(antiAliased);
        }
    }
    
    private GeneralPath createShape(int shapeW, int shapeH) {
        GeneralPath gp = new GeneralPath();
        float radius = Display.getInstance().convertToPixels(cornerRadius);
        int arrowGap = convertToPixels(2);
        float x = arrowGap;
        float y = 0;
        float widthF = shapeW - arrowGap * 2;
        float heightF = shapeH;
        
        if(this.stroke != null && strokeOpacity > 0 && 
                strokeThickness > 0) {
            int strokePx = (int)strokeThickness;
            if(strokeMM) {
                strokePx = Display.getInstance().
                    convertToPixels(strokeThickness);
            }
            widthF -= strokePx;
            heightF -= strokePx;
            x += strokePx / 2;
            y += strokePx / 2;
            
            if(strokePx % 2 == 1) {
                x += 0.5f;
                y += 0.5f;
            }
        }            
                        
        if(leftArrow) {
            gp.moveTo(x, y + arrowGap);
            gp.lineTo(x - arrowGap, y);
        } else {
            gp.moveTo(x + radius, y);
        }
        if(rightArrow) {
            gp.lineTo(x + widthF + arrowGap, y);
            gp.lineTo(x + widthF, y + arrowGap);
        } else {
            gp.lineTo(x + widthF - radius, y);            
            gp.quadTo(x + widthF, y, x + widthF, y + radius);
        }
        gp.lineTo(x + widthF, y + heightF - radius);
        gp.quadTo(x + widthF, y + heightF, x + widthF - radius, y+heightF);
        gp.lineTo(x + radius, y + heightF);
        gp.quadTo(x, y + heightF, x, y + heightF - radius);
        gp.lineTo(x, y + radius);
        gp.quadTo(x, y, x + radius, y);
        
        gp.closePath();            
        return gp;
    }

    @Override
    public int getMinimumHeight() {
        return Display.getInstance().convertToPixels(shadowSpread) + 
            Display.getInstance().convertToPixels(cornerRadius) * 2;
    }

    @Override
    public int getMinimumWidth() {
        return Display.getInstance().convertToPixels(shadowSpread) + 
            Display.getInstance().convertToPixels(cornerRadius) * 2;
    }

    
    private Stroke stroke1;
    
    private void drawShape(Graphics g, int color, int opacity, 
            int width, int height) {
        g.setColor(color);
        g.setAlpha(opacity);
        GeneralPath gp = createShape(width, height);
        if (stroke1 == null) {
            stroke1 = new Stroke(1f, Stroke.CAP_ROUND, 
                Stroke.JOIN_MITER, 1f);
        }
        g.drawShape(gp, stroke1);
                   
    }
    
    @Override
    public boolean isBackgroundPainter() {
        return true;
    }

    /**
     * The color of the edge of the border if applicable
     * @return the strokeColor
     */
    public int getStrokeColor() {
        return strokeColor;
    }

    /**
     * The opacity of the edge of the border if applicable
     * @return the strokeOpacity
     */
    public int getStrokeOpacity() {
        return strokeOpacity;
    }

    /**
     * The thickness of the edge of the border if applicable, 0 if no 
     * stroke is needed
     * @return the strokeThickness
     */
    public float getStrokeThickness() {
        return strokeThickness;
    }

    /**
     * True if the thickness of the stroke is in millimeters
     * @return the strokeMM
     */
    public boolean isStrokeMM() {
        return strokeMM;
    }

    /**
     * True if the corners are bezier curves, otherwise the corners are 
     * drawn as a regular arc
     * 
     * @return true if the corners are a curve
     */
    public boolean isBezierCorners() {
        return bezierCorners;
    }
    
    /**
     * The spread of the shadow in pixels of millimeters
     * @return the shadowSpread
     */
    public float getShadowSpread() {
        return shadowSpread;
    }

    /**
     * The opacity of the shadow between 0 and 255
     * @return the shadowOpacity
     */
    public int getShadowOpacity() {
        return shadowOpacity;
    }

    /**
     * X axis bias of the shadow between 0 and 1 where 0 is to the top 
     * and 1 is to the bottom, defaults to 0.5
     * @return the shadowX
     */
    public float getShadowX() {
        return shadowX;
    }

    /**
     * Y axis bias of the shadow between 0 and 1 where 0 is to the left 
     * and 1 is to the right, defaults to 0.5
     * @return the shadowY
     */
    public float getShadowY() {
        return shadowY;
    }

    /**
     * The Gaussian blur size
     * @return the shadowBlur
     */
    public float getShadowBlur() {
        return shadowBlur;
    }

    
    /**
     * The radius of the corners in millimeters
     * 
     * @return the radius
     */
    public float getCornerRadius() {
        return cornerRadius;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + strokeColor;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ChatBubbleBorder other = (ChatBubbleBorder) obj;
        if (this.strokeColor != other.strokeColor) {
            return false;
        }
        if (this.strokeOpacity != other.strokeOpacity) {
            return false;
        }
        if (this.strokeThickness != other.strokeThickness) {
            return false;
        }
        if (this.strokeMM != other.strokeMM) {
            return false;
        }
        if (this.shadowSpread != other.shadowSpread) {
            return false;
        }
        if (this.shadowOpacity != other.shadowOpacity) {
            return false;
        }
        if (this.shadowX != other.shadowX) {
            return false;
        }
        if (this.shadowY != other.shadowY) {
            return false;
        }
        if (this.shadowBlur != other.shadowBlur) {
            return false;
        }
        if (this.bezierCorners != other.bezierCorners) {
            return false;
        }
        return true;
    }
}
