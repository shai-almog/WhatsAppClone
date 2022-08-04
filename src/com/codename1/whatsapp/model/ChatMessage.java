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

import com.codename1.properties.BooleanProperty;
import com.codename1.properties.MapProperty;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;
import com.codename1.properties.SetProperty;
import java.util.Date;

/**
 * Encapsulates the details of a specific message and server event
 */
public class ChatMessage implements PropertyBusinessObject {
    public final Property<String, ChatMessage> id = new Property<>("id");
    public final Property<String, ChatMessage> authorId = 
        new Property<>("authorId");
    public final Property<String, ChatMessage> authorPhone = 
        new Property<>("authorPhone");
    public final Property<String, ChatMessage> sentTo = 
        new Property<>("sentTo");
    public final Property<Date, ChatMessage> time = 
        new Property<>("time", Date.class);    
    public final Property<String, ChatMessage> body = 
        new Property<>("body");
    public final MapProperty<String, String, ChatMessage> attachments = 
        new MapProperty<>("media", String.class, String.class);
    public final SetProperty<String, ChatMessage> viewedBy = 
        new SetProperty<>("viewedBy", String.class);
    public final BooleanProperty<ChatMessage> typing = 
        new BooleanProperty<>("typing");
    
    private final PropertyIndex idx = new PropertyIndex(this, 
        "ChatMessage", id, authorId, authorPhone, sentTo, time, 
        body, attachments, viewedBy, typing);
    
    @Override
    public PropertyIndex getPropertyIndex() {
        return idx;
    }
}
