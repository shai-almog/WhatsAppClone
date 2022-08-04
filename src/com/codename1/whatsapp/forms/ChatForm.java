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

package com.codename1.whatsapp.forms;

import com.codename1.capture.Capture;
import com.codename1.components.SpanLabel;
import com.codename1.ext.filechooser.FileChooser;
import com.codename1.l10n.SimpleDateFormat;
import com.codename1.ui.Button;
import com.codename1.ui.Container;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.TextArea;
import com.codename1.ui.TextField;
import com.codename1.ui.Toolbar;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.Border;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Component;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.util.DateUtil;
import com.codename1.whatsapp.components.ChatBubbleBorder;
import com.codename1.whatsapp.model.ChatContact;
import com.codename1.whatsapp.model.ChatMessage;
import com.codename1.whatsapp.model.Server;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

public class ChatForm extends Form {
    private ChatContact contact;
    private final int DAY = 24 * 60 * 60000;
    private final SimpleDateFormat DAY_FORMAT = 
        new SimpleDateFormat("MMMMMM dd, yyyy");
    private boolean todayAdded;
    
    public ChatContact getContact() {
        return contact;
    }
    
    public ChatForm(ChatContact contact, Form parent) {
        super(contact.name.get(), BoxLayout.yLast());
        setUIID("ChatForm");
        this.contact = contact;
        
        Toolbar tb = getToolbar();
        
        tb.setBackCommand("", Toolbar.BackCommandPolicy.AS_ARROW,
            e -> parent.showBack());
        
        tb.addCommandToLeftBar("", contact.getSmallIcon(), e -> {});

        tb.addMaterialCommandToRightBar("", FontImage.MATERIAL_VIDEOCAM, 
            e -> {});
        tb.addMaterialCommandToRightBar("", FontImage.MATERIAL_PHONE, 
            e -> {});
        
        tb.addCommandToOverflowMenu("View contact", null, e -> {});
        tb.addCommandToOverflowMenu("Media", null, e -> {});
        tb.addCommandToOverflowMenu("Search", null, e -> {});
        tb.addCommandToOverflowMenu("Mute notifications", null, e -> {});
        tb.addCommandToOverflowMenu("Wallpaper", null, e -> {});
        
        final Component cc = createInputContainer();
        add(cc);

        if(contact.chats.size() > 0) {
            Date time = contact.chats.get(0).time.get();
            long currentDay = time.getTime() / DAY;
            addDay(time, currentDay);
            for(ChatMessage m : contact.chats) {
                time = m.time.get();
                long c = time.getTime() / DAY;
                if(c != currentDay) {
                    currentDay = c;
                    addDay(time, currentDay);
                }
                addMessageToUINoAnimation(m);
            }
        }
        addShowListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                removeShowListener(this);
                getContentPane().scrollComponentToVisible(cc);
            }
        });
    }    
    
    
    private void addDay(Date d, long currentDay) {
        String text;
        long t = System.currentTimeMillis() / DAY;
        if(t == currentDay) {
            text = "Today";
            todayAdded = true;
        } else {
            if(t - 1 == currentDay) {
                text = "Yesterday";
            } else {
                text = DAY_FORMAT.format(d);
            }
        }
        Label day = new Label(text.toUpperCase(), "Day");
        addComponent(getContentPane().getComponentCount() - 1, 
            FlowLayout.encloseCenter(day));
    }
    
    private Container createInputContainer() {
        TextField input = new TextField("", "Type a message", 20, 
            TextArea.ANY);
        input.getAllStyles().setBorder(Border.createEmpty());
        input.setSingleLineTextArea(false);

        input.setDoneListener(e -> {
            addMessage(new ChatMessage().
                authorId.set(Server.user().id.get()).
                time.set(new Date()).
                sentTo.set(contact.id.get()).
                body.set(input.getText()));
            input.stopEditing();
            input.setText("");
        });
        
        Button emoji = new Button("", FontImage.MATERIAL_MOOD, 
            "TextFieldIcon");
        Button attach = new Button("", FontImage.MATERIAL_ATTACH_FILE, 
            "TextFieldIcon");
        Button camera = new Button("", FontImage.MATERIAL_CAMERA_ALT, 
            "TextFieldIcon");
        
        camera.addActionListener(e -> openCamera());
        attach.addActionListener(e -> openFile());
        
        Container inputContainer = BorderLayout.centerEastWest(input, 
            BoxLayout.encloseX(attach, camera), 
            emoji);
        inputContainer.setUIID("ChatTextField");
        Button microphone = new Button("", FontImage.MATERIAL_MIC, 
                "RecordButton") {
            @Override
            public void pointerPressed(int x, int y) {
                if(getMaterialIcon() == FontImage.MATERIAL_MIC) {
                    
                } else {
                    super.pointerPressed(x, y);
                }
            }
            

            @Override
            public void pointerReleased(int x, int y) {
                if(getMaterialIcon() == FontImage.MATERIAL_MIC) {
                    
                } else {
                    super.pointerReleased(x, y);
                }
            }
        };
        input.addDataChangedListener((i, ii) -> {
            if(input.getText().length() == 0) {
                microphone.setMaterialIcon(FontImage.MATERIAL_MIC);
            } else {
                microphone.setMaterialIcon(FontImage.MATERIAL_SEND);
            }
        });
        microphone.addActionListener(e -> {
            if(microphone.getMaterialIcon() == FontImage.MATERIAL_SEND) {
                addMessage(new ChatMessage().
                    authorId.set(Server.user().id.get()).
                    sentTo.set(contact.id.get()).
                    time.set(new Date()).
                    body.set(input.getText()));
                input.stopEditing();
                input.setText("");                
            }
        });
        return BorderLayout.
            centerCenterEastWest(inputContainer, microphone, null);
    }
    
    private String twoDigit(int i) {
        return i < 10 ? "0" + i : "" + i;
    }
    
    private String getTime(long t) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(t));
        int h = c.get(Calendar.HOUR_OF_DAY);
        int m = c.get(Calendar.MINUTE);
        return twoDigit(h) + ":" + twoDigit(m);
    }

    public void addMessageToUI(ChatMessage m) {
        if(!todayAdded) {
            addDay(new Date(), System.currentTimeMillis() / DAY);
        }
        addMessageToUINoAnimation(m);
        getContentPane().animateLayoutAndWait(100);
        getContentPane().scrollComponentToVisible(
            getContentPane().getComponentAt(
                getContentPane().getComponentCount() - 1));
    }
    
    private Component addMessageToUINoAnimation(ChatMessage m) {
        boolean left = m.authorId.get() == null || 
            !m.authorId.get().equals(Server.user().id.get());
        String uiid = left ? "ChatBubbleLeft" : "ChatBubbleRight";
        Component cmp;
        if(m.attachments.size() == 0) {
            cmp = createTextMessage(m.body.get(), m.time.get().getTime());
        } else {
            cmp = createMediaMessage(
                m.attachments.iterator().next().getValue(), 
                m.time.get().getTime());
        }
        ChatBubbleBorder cb = ChatBubbleBorder.create();
        if(getContentPane().getComponentCount() > 1) {
            Container cnt = (Container)getContentPane().
                getComponentAt(getContentPane().getComponentCount() - 2);
            if(cnt.getUIID().equals("Container")) {
                Component cc = cnt.getComponentAt(0);
                if(cc instanceof Container) {
                    cnt = (Container)cc;
                }
            }
            if(!cnt.getUIID().equals(uiid)) {
                cb.leftArrow(left).rightArrow(!left);
            } else {
                Style s = cnt.getAllStyles();
                s.setPaddingBottom(0);
                s.setMarginBottom(0);
            }
        } else {
            cb.leftArrow(left).rightArrow(!left);
        }
        Container cnt;
        if(left) {
            cnt = FlowLayout.encloseIn(cmp);
        } else {
            cnt = FlowLayout.encloseRight(cmp); 
        }
        cmp.setUIID(uiid);
        addComponent(getContentPane().getComponentCount() - 1, cnt);
        cnt.putClientProperty("message", m);
        cmp.getAllStyles().setBorder(cb);
        return cnt;
    }
    
    private void addMessage(ChatMessage m) {
        Server.sendMessage(m, contact);
        addMessageToUI(m);
    }
    
    private Component createTextMessage(String t, long mt) {
        TextArea bubbleText = new TextArea(t);
        bubbleText.setActAsLabel(true);
        bubbleText.setEditable(false);
        bubbleText.setFocusable(false);
        bubbleText.setUIID("ChatText");
        Label time = new Label(getTime(mt), "ChatTime");
        Container bubble;
        if(t.length() < 30) {
            bubble = BoxLayout.encloseX(bubbleText, time);
        } else {
            bubble = BoxLayout.encloseY(bubbleText, time);
        }
        return bubble;
    }
    
    public Component createMediaMessage(String media, long mt) {
        try(InputStream is = openFileInputStream(media)) {
            int s = Math.min(getDisplayWidth(), getDisplayHeight()) / 2;
            EncodedImage img = EncodedImage.create(is, 
                (int)getFileLength(media));
            Button thumb = new Button("", img.fill(s, s),"ChatText");
            Label time = new Label(getTime(mt), "ChatTime");
            return LayeredLayout.encloseIn(thumb, FlowLayout.encloseRightBottom(time));
        } catch(IOException ex) {
            log(ex);
            return new SpanLabel("Failed to load media");
        }
    }
    
    public void openCamera() {
        String pic = Capture.capturePhoto();
        if(pic != null) {
            addMessage(new ChatMessage().
                authorId.set(Server.user().id.get()).
                sentTo.set(contact.id.get()).
                time.set(new Date()).
                attachments.put("Pic", pic));
        }
    }
    
    public void openFile() {
        FileChooser.showOpenDialog("*/*", e -> {
            if(e != null) {
                String file = (String)e.getSource();
                if(file != null) {
                    String l = file.toLowerCase();
                    if(l.endsWith("jpg") || l.endsWith("jpeg") || 
                            l.endsWith("png")) {
                        addMessage(new ChatMessage().
                            authorId.set(Server.user().id.get()).
                            sentTo.set(contact.id.get()).
                            time.set(new Date()).
                            attachments.put("Pic", file));
                    } 
                }
            }
        });
    }
}
