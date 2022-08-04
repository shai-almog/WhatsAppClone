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

import com.codename1.camerakit.CameraKit;
import com.codename1.components.FloatingActionButton;
import com.codename1.components.MultiButton;
import com.codename1.ui.Button;
import com.codename1.ui.ButtonGroup;
import com.codename1.ui.Form;
import com.codename1.ui.Tabs;
import com.codename1.ui.layouts.BorderLayout;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.FontImage;
import com.codename1.ui.Label;
import com.codename1.ui.RadioButton;
import com.codename1.ui.Toolbar;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.table.TableLayout;
import com.codename1.whatsapp.model.ChatContact;
import com.codename1.whatsapp.model.Server;

public class MainForm extends Form {
    private Tabs tabs = new Tabs();
    private CameraKit ck;
    private Container chats;
    private Container status;
    private Container calls;
    private static MainForm instance;

    
    public MainForm() {
        super("WhatsApp Clone", new BorderLayout());
        instance = this;
        add(CENTER, tabs);
        tabs.hideTabs();
        ck = CameraKit.create();        

        tabs.addTab("", createCameraView());
        tabs.addTab("", createChatsContainer());
        tabs.addTab("", createStatusContainer());
        tabs.addTab("", createCallsContainer());
        tabs.setSelectedIndex(1);
        
        Toolbar tb = getToolbar();
        tb.setTitleComponent(createTitleComponent(chats, status, calls));
        setBackCommand("", null, e -> {
           if(tabs.getSelectedIndex() != 1) {
               tabs.setSelectedIndex(1);
           } else {
               minimizeApplication();
           }
        });
    }
    
    public static MainForm getInstance() {
        return instance;
    }
    
    private Container createCallsContainer() {
        Container cnt = new Container(BoxLayout.y());
        calls = cnt;
        cnt.setScrollableY(true);
        
        MultiButton chat = new MultiButton("Person");
        chat.setTextLine2("Date & time");
        cnt.add(chat);        
        
        FloatingActionButton fab = FloatingActionButton.
                createFAB(FontImage.MATERIAL_CALL);
        return fab.bindFabToContainer(cnt);
    }

    private Container createStatusContainer() {
        Container cnt = new Container(BoxLayout.y());
        status = cnt;
        cnt.setScrollableY(true);
        
        MultiButton chat = new MultiButton("My Status");
        chat.setTextLine2("Tap to add status update");
        cnt.add(chat);
        
        FloatingActionButton fab = FloatingActionButton.
            createFAB(FontImage.MATERIAL_CAMERA_ALT);
        return fab.bindFabToContainer(cnt);
    }
    
    
    public void refreshChatsContainer() {
        Server.fetchChatList(contacts -> {
            chats.removeAll();
            for(ChatContact c : contacts) {
                MultiButton chat = new MultiButton(c.name.get());
                chat.setTextLine2(c.tagline.get());
                if(chat.getTextLine2() == null || 
                        chat.getTextLine2().length() == 0) {
                    chat.setTextLine2("...");
                }
                chat.setIcon(c.getLargeIcon());
                chats.add(chat);
                chat.addActionListener(e -> new ChatForm(c, this).show());
            }

            chats.revalidate();
        });
        
        // Useful for testing the folding animation
        /*for(int iter = 0 ; iter < 100 ; iter++) {
            ChatContact c = new ChatContact().
                name.set("Contact " + iter).
                tagline.set("Tagline...");
            MultiButton chat = new MultiButton(c.name.get());
            chat.setTextLine2(c.tagline.get());
            if(chat.getTextLine2() == null || 
                    chat.getTextLine2().length() == 0) {
                chat.setTextLine2("...");
            }
            chat.setIcon(c.getLargeIcon());
            chats.add(chat);
        }
        chats.revalidate();*/
    }
    
    private Container createChatsContainer() {
        chats = new Container(BoxLayout.y());
        chats.setScrollableY(true);

        refreshChatsContainer();
        
        FloatingActionButton fab = FloatingActionButton.
                createFAB(FontImage.MATERIAL_CHAT);
        fab.addActionListener(e -> new NewMessageForm().show());
        return fab.bindFabToContainer(chats);
    }
    
    
    private Container createCameraView() {
        if(ck != null) {
            Container cameraCnt = new Container(new LayeredLayout());
            tabs.addSelectionListener((oldSelected, newSelected) -> {
                if(newSelected == 0) {
                    //ck.start();
                    //cameraCnt.add(ck.getView());
                    getToolbar().setHidden(true);
                } else {
                    if(oldSelected == 0) {
                        //cameraCnt.removeAll();
                        //ck.stop();
                        getToolbar().setHidden(false);
                    }
                }
            });
            return cameraCnt;
        } 
        return BorderLayout.center(new Label("Camera Unsupported"));
    }
    
    private void showOverflowMenu() {
        Button newGroup = new Button("New group", "Command");
        Button newBroadcast = new Button("New broadcast", "Command");
        Button whatsappWeb = new Button("WhatsApp Web", "Command");
        Button starred = new Button("Starred Messages", "Command");
        Button settings = new Button("Settings", "Command");
        Container cnt = BoxLayout.encloseY(newGroup, newBroadcast, 
                whatsappWeb, starred, settings);
        cnt.setUIID("CommandList");
        Dialog dlg = new Dialog(new BorderLayout());
        dlg.setDialogUIID("Container");
        dlg.add(CENTER, cnt);
        dlg.setDisposeWhenPointerOutOfBounds(true);
        dlg.setTransitionInAnimator(CommonTransitions.createEmpty());
        dlg.setTransitionOutAnimator(CommonTransitions.createEmpty());
        dlg.setBackCommand("", null, e -> dlg.dispose());
        int top = getUIManager().getComponentStyle("StatusBar").
                getVerticalPadding();
        setTintColor(0);
        int bottom = getHeight() - cnt.getPreferredH() - top - 
            cnt.getUnselectedStyle().getVerticalPadding() - 
            cnt.getUnselectedStyle().getVerticalMargins();
        int w = getWidth();
        int left = w - cnt.getPreferredW() - 
            cnt.getUnselectedStyle().getHorizontalPadding() - 
            cnt.getUnselectedStyle().getHorizontalMargins();
        dlg.show(top, bottom, left, 0);
    }
    
    private Container createTitleComponent(Container... scrollables) {
        Label title = new Label("WhatsApp", "Title");
        Container titleArea;
        if(title.getUnselectedStyle().getAlignment() == LEFT) {
            titleArea = BorderLayout.center(title);
        } else {
            // for iOS we want the title to center properly
            titleArea = BorderLayout.centerAbsolute(title);
        }
        
        Button search = new Button("", FontImage.MATERIAL_SEARCH, "Title");
        Button overflow = new Button("", FontImage.MATERIAL_MORE_VERT, 
            "Title");
        overflow.addActionListener(e -> showOverflowMenu());
        titleArea.add(EAST, GridLayout.encloseIn(2, search, overflow));
        
        ButtonGroup bg = new ButtonGroup();
        RadioButton camera = RadioButton.createToggle("", bg); 
        camera.setUIID("SubTitle");
        FontImage.setMaterialIcon(camera, FontImage.MATERIAL_CAMERA_ALT);
        RadioButton chats = RadioButton.createToggle("Chats", bg);
        RadioButton status = RadioButton.createToggle("Status", bg);
        RadioButton calls = RadioButton.createToggle("Calls", bg);
        chats.setUIID("SubTitle");
        status.setUIID("SubTitle");
        calls.setUIID("SubTitle");
        RadioButton[] buttons = new RadioButton[] {
            camera, chats, status, calls
        };
        
        TableLayout tb = new TableLayout(2, 4);
        Container toggles = new Container(tb);
        
        toggles.add(tb.createConstraint().widthPercentage(10), camera);
        toggles.add(tb.createConstraint().widthPercentage(30), chats);
        toggles.add(tb.createConstraint().widthPercentage(30), status);
        toggles.add(tb.createConstraint().widthPercentage(30), calls);
        Label whiteLine = new Label("", "SubTitleUnderline"); 
        whiteLine.setShowEvenIfBlank(true);
        
        toggles.add(tb.createConstraint(1, 1) ,whiteLine);
        
        final Container finalTitle = titleArea;
        for(int iter = 0  ; iter < buttons.length ; iter++) {
            final int current = iter;
            buttons[iter].addActionListener(e -> {
                tabs.setSelectedIndex(current);
                whiteLine.remove();
                toggles.add(tb.createConstraint(1, current) ,whiteLine);
                finalTitle.setPreferredSize(null);
                toggles.animateLayout(100);
            });
        }
        
        tabs.addSelectionListener((oldSelected, newSelected) -> {
            if(!buttons[newSelected].isSelected()) {
                finalTitle.setPreferredSize(null);
                buttons[newSelected].setSelected(true);
                whiteLine.remove();
                toggles.add(tb.createConstraint(1, newSelected) ,whiteLine);
                toggles.animateLayout(100);
            }
        });
        
        bindFolding(titleArea, titleArea.getPreferredH(), scrollables);
        
        return BoxLayout.encloseY(titleArea, toggles);
    }
    
    private void bindFolding(Container titleArea, int titleHeight, 
            Container... scrollables) {
        addPointerReleasedListener(e -> {
            if(titleArea.getHeight() != titleHeight && 
                        titleArea.getHeight() != 0) {
                if(titleHeight - titleArea.getHeight() > titleHeight / 2) {
                    titleArea.setPreferredSize(null);
                } else {
                    titleArea.setPreferredH(0);
                }
                titleArea.getParent().animateLayout(100);
            }
        });
        for(Container c : scrollables) {
            c.addScrollListener((scrollX, scrollY, oldscrollX,
                oldscrollY) -> {
                // special case for tensile drag
                if(scrollY <= 10) {
                    titleArea.setPreferredSize(null);
                    return;
                }
                int diff = oldscrollY - scrollY;
                if(diff > 0) {
                    if(titleArea.getHeight() < titleHeight) {
                        titleArea.setPreferredH(Math.min(titleHeight, 
                            titleArea.getPreferredH() + diff));
                        titleArea.setHeight(titleArea.getPreferredH());
                        titleArea.getParent().revalidate();
                    }
                } else {
                    if(diff < 0) {
                        if(titleArea.getHeight() > 0) {
                            titleArea.setPreferredH(Math.max(0, 
                                titleArea.getPreferredH() + diff));
                            titleArea.setHeight(titleArea.getPreferredH());
                            titleArea.getParent().revalidate();
                        }
                        
                    }
                }
            });
        }
    }

    @Override
    protected void initGlobalToolbar() {
        Toolbar tb = new Toolbar();
        tb.setTitleCentered(false);
        setToolbar(tb);
    }
}
