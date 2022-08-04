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

package com.codename1.whatsapp.model;

import com.codename1.properties.ListProperty;
import com.codename1.properties.LongProperty;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;
import com.codename1.properties.SetProperty;
import com.codename1.ui.Font;
import com.codename1.ui.FontImage;
import com.codename1.ui.Graphics;
import com.codename1.ui.Image;
import static com.codename1.ui.CN.*;
import com.codename1.ui.EncodedImage;
import java.util.Date;

/**
 * Encapsulates the details of a specific contact or group
 */
public class ChatContact implements PropertyBusinessObject {
    public final Property<String, ChatContact> id = new Property<>("id");
    public final Property<String, ChatContact> localId = 
            new Property<>("localId");
    public final Property<String, ChatContact> phone = 
            new Property<>("phone");
    public final Property<Image, ChatContact> photo = 
            new Property<>("photo", Image.class);
    public final Property<String, ChatContact> name = 
            new Property<>("name");
    public final Property<String, ChatContact> tagline = 
            new Property<>("tagline");
    public final Property<String, ChatContact> token = 
            new Property<>("token");
    public final SetProperty<String, ChatContact> members = 
            new SetProperty<>("members", String.class);
    public final SetProperty<String, ChatContact> admins = 
            new SetProperty<>("admins", String.class);
    public final LongProperty<ChatContact> muteUntil = 
            new LongProperty<>("muteUntil");
    public final Property<String, ChatContact> createdBy = 
            new Property<>("createdBy");
    public final Property<Date, ChatContact> creationDate = 
            new Property<>("creationDate", Date.class);
    public final Property<Date, ChatContact> lastActivityTime = 
            new Property<>("lastActivityTime", Date.class);
    public final ListProperty<ChatMessage, ChatContact> chats = 
            new ListProperty<>("chat", ChatMessage.class);
    
    private final PropertyIndex idx = new PropertyIndex(this, "ChatContact", 
        id, localId, phone, photo, name, tagline, token, members, admins, 
        muteUntil, createdBy, creationDate, lastActivityTime, chats);
    
    @Override
    public PropertyIndex getPropertyIndex() {
        return idx;
    }

    public ChatContact() {
        idx.setExcludeFromJSON(photo, true);
    }
    
    private static final int SMALL_IMAGE = 0;
    private static final int LARGE_IMAGE = 1;
    private static final float[] IMAGE_SIZES = {4f, 6.5f};
    private static final Image[] maskImage = new Image[2];
    private static final Object[] maskObject = new Object[2];
    private static final EncodedImage[] placeholder = new EncodedImage[2];
        
    private static Image createMaskImage(int size) {
        Image m = Image.createImage(size, size, 0);
        Graphics g = m.getGraphics();
        g.setAntiAliased(true);
        g.setColor(0xffffff);
        g.fillArc(0, 0, size - 1, size -1, 0, 360);
        return m;
    }
    
    private Image createPlaceholder(int size) {
        Image i = Image.createImage(size, size, 0xffe2e7ea);
        Graphics g = i.getGraphics();
        g.setAntiAliased(true);
        g.setAntiAliasedText(true);
        g.setColor(0xffffff);
        Font fnt = FontImage.getMaterialDesignFont().
            derive(size, Font.STYLE_PLAIN);
        g.setFont(fnt);
        String s = ""  + FontImage.MATERIAL_PERSON;
        g.drawString(s, size / 2 - fnt.stringWidth(s) / 2, 0);
        return i;
    } 
    
    private Image getImage(int offset) {
        if(maskImage[offset] == null) {
            maskImage[offset] = 
                createMaskImage(convertToPixels(IMAGE_SIZES[offset]));
            maskObject[offset] = maskImage[offset].createMask();
            Image i = createPlaceholder(
                    convertToPixels(IMAGE_SIZES[offset]));
            placeholder[offset] = EncodedImage.createFromImage(
                i.applyMask(maskObject[offset]), false);
        }
        
        if(photo.get() == null) {
            return placeholder[offset];
        }
        return photo.get().
            fill(maskImage[offset].getWidth(), 
                maskImage[offset].getHeight()).
            applyMask(maskObject[offset]);
    }
    
    
    private Image smallIcon;
    private Image largeIcon;

    public Image getSmallIcon() {
        if(smallIcon != null) {
            return smallIcon;
        }
        smallIcon = getImage(SMALL_IMAGE);
        return smallIcon;
    }
    
    public Image getLargeIcon() {
        if(largeIcon != null) {
            return largeIcon;
        }
        largeIcon = getImage(LARGE_IMAGE);
        return largeIcon;
    }
}
