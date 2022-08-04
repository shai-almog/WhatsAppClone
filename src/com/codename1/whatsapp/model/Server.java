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

import com.codename1.contacts.Contact;
import com.codename1.io.JSONParser;
import com.codename1.io.Log;
import com.codename1.io.Preferences;
import com.codename1.io.Util;
import com.codename1.io.rest.RequestBuilder;
import com.codename1.io.rest.Response;
import com.codename1.io.rest.Rest;
import com.codename1.io.websocket.WebSocket;
import com.codename1.properties.PropertyIndex;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Display;
import com.codename1.ui.EncodedImage;
import com.codename1.util.EasyThread;
import com.codename1.util.OnComplete;
import com.codename1.util.regex.StringReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Server {
    private static final String SERVER_URL = "http://localhost:8080/";
    private static final String WEBSOCKER_URL = "ws://localhost:8080/socket";
    
    private static final String USER_FILE_NAME = "user.json";
    private static final String MESSAGE_QUEUE_FILE_NAME = "message_queue.json";

    private static ChatContact currentUser;
    
    private static WebSocket connection;
    private static boolean connected;
    private static List<ChatMessage> messageQueue;
    
    public static ChatContact user() {
        return currentUser;
    }

    public static void init() {
        if(existsInStorage(USER_FILE_NAME)) {
            currentUser = new ChatContact();
            currentUser.getPropertyIndex().loadJSON(USER_FILE_NAME);
            
            if(existsInStorage(MESSAGE_QUEUE_FILE_NAME)) {
                messageQueue = new ChatMessage().getPropertyIndex().
                    loadJSONList(MESSAGE_QUEUE_FILE_NAME);
            }
            if(existsInStorage("contacts.json")) {
                contactCache = new ChatContact().getPropertyIndex().
                    loadJSONList("contacts.json");
            } else {
                contactCache = new ArrayList<>();
            }
        } else {
            contactCache = new ArrayList<>();            
        } 
    }
    
    public static void flushMessageQueue() {
        if(connected && messageQueue != null && messageQueue.size() > 0) {
            for(ChatMessage m : messageQueue) {
                connection.send(m.getPropertyIndex().toJSON());
            }
            messageQueue.clear();
        }        
    }
    
    private static RequestBuilder post(String u) {
        RequestBuilder r = Rest.post(SERVER_URL + u).jsonContent();
        if(currentUser != null && currentUser.token.get() != null) {
            r.header("auth", currentUser.token.get());
        }
        return r;
    }

    private static RequestBuilder get(String u) {
        RequestBuilder r = Rest.get(SERVER_URL + u).jsonContent();
        if(currentUser != null && currentUser.token.get() != null) {
            r.header("auth", currentUser.token.get());
        }
        return r;
    }
    
    public static void login(OnComplete<ChatContact> c) {
        post("user/login").
            body(currentUser).fetchAsProperties(
                res -> {
                    currentUser = (ChatContact)res.getResponseData();
                    currentUser.
                        getPropertyIndex().
                        storeJSON(USER_FILE_NAME);
                    c.completed(currentUser);
                }, 
                ChatContact.class);
    }

    public static void signup(ChatContact user, 
                OnComplete<ChatContact> c) {
        post("user/signup").
            body(user).fetchAsProperties(
                res -> {
                    currentUser = (ChatContact)res.getResponseData();
                    currentUser.
                        getPropertyIndex().
                        storeJSON(USER_FILE_NAME);
                    c.completed(currentUser);
                }, 
                ChatContact.class);
    }

    public static void update(OnComplete<ChatContact> c) {
        post("user/update").
            body(currentUser).fetchAsProperties(
                res -> {
                    currentUser = (ChatContact)res.getResponseData();
                    currentUser.
                        getPropertyIndex().
                        storeJSON(USER_FILE_NAME);
                    c.completed(currentUser);
                }, 
                ChatContact.class);
    }

    public static boolean verify(String code) {
        Response<String> result = get("user/verify").
            queryParam("userId", currentUser.id.get()).
            queryParam("code", code).
            getAsString();
        return "OK".equals(result.getResponseData());
    }
    
    public static void sendMessage(ChatMessage m, ChatContact cont) {
        cont.lastActivityTime.set(new Date());
        cont.chats.add(m);
        saveContacts();
        if(connected) {
            post("user/sendMessage").
                body(m).
                fetchAsProperties(e -> {
                    cont.chats.remove(m);
                    cont.chats.add((ChatMessage)e.getResponseData());
                    saveContacts();
                }, ChatMessage.class);
            //connection.send(m.getPropertyIndex().toJSON());
        } else {
            if(messageQueue == null) {
                messageQueue = new ArrayList<>();
            }
            messageQueue.add(m);
            PropertyIndex.storeJSONList(MESSAGE_QUEUE_FILE_NAME, 
                    messageQueue);
        }
    }
    
    public static void bindMessageListener(final ServerMessages 
                callback) {
        connection = new WebSocket(WEBSOCKER_URL) {
            @Override
            protected void onOpen() {
                connected = true;
                long lastMessageTime = 
                    Preferences.get("LastReceivedMessage", (long)0);
                send("{\"t\":\"init\",\"tok\":\"" + 
                    currentUser.token.get() + 
                    "\",\"time\":" + 
                    lastMessageTime + "}");
                callSerially(() -> callback.connected());
                final WebSocket w = this;
                new Thread() {
                    public void run() {
                        Util.sleep(80000);
                        while(connection == w) {
                            // keep-alive message every 80 seconds to avoid 
                            // cloudflare killing of the connection
                            // https://community.cloudflare.com/t/cloudflare-websocket-timeout/5865/3
                            send("{\"t\":\"ping\"}");
                            Util.sleep(80000);
                        } 
                    }
                }.start();
            }
            
            @Override
            protected void onClose(int statusCode, String reason) {
                connected = false;
                callSerially(() -> callback.disconnected());
            }
            
            @Override
            protected void onMessage(String message) {
                try {
                    StringReader r = new StringReader(message);
                    JSONParser jp = new JSONParser();
                    JSONParser.setUseBoolean(true);
                    JSONParser.setUseLongs(true);
                    Map m  = jp.parseJSON(r);
                    ChatMessage c = new ChatMessage();
                    c.getPropertyIndex().
                        populateFromMap(m, ChatMessage.class);
                    callSerially(() -> {
                        if(c.typing.get() != null && 
                                c.typing.getBoolean()) {
                            callback.userTyping(c.authorId.get());
                            return;
                        }
                        if(c.viewedBy.size() > 0) {
                            callback.messageViewed(message, 
                                c.viewedBy.asList());
                            return;
                        }
                        Preferences.set("LastReceivedMessage", 
                            c.time.get().getTime());
                        updateMessage(c);
                        
                        ackMessage(c.id.get());
                        callback.messageReceived(c);
                    });
                } catch(IOException err) {
                    Log.e(err);
                    throw new RuntimeException(err.toString());
                }                
            }
            
            @Override
            protected void onMessage(byte[] message) {
            }
            
            @Override
            protected void onError(Exception ex) {
                Log.e(ex);
            }
        };
        connection.autoReconnect(5000);
        connection.connect();
    }
    
    private static void updateMessage(ChatMessage m) {
        for(ChatContact c : contactCache) {
            if(c.id.get() != null && 
                    c.id.get().equals(m.authorId.get())) {
                c.lastActivityTime.set(new Date());
                c.chats.add(m);
                saveContacts();        
                return;
            }
        }
        findRegisteredUserById(m.authorId.get(), cc -> {
            contactCache.add(cc);
            cc.chats.add(m);
            saveContacts();        
        });
    }
    
    public static void closeWebsocketConnection() {
        if(connection != null) {
            connection.close();
            connection = null;
        }
    }
    
    public static void saveContacts() {
        if(contactCache != null && contactsThread != null) {
            contactsThread.run(() -> {
                PropertyIndex.storeJSONList("contacts.json", 
                        contactCache);
            });
        }
    }
    
    private static List<ChatContact> contactCache;
    private static EasyThread contactsThread;
    public static void fetchContacts(
            OnComplete<List<ChatContact>> contactsCallback) {
        if(contactsThread == null) {
            contactsThread = EasyThread.start("Contacts Thread");
            contactsThread.run(() -> fetchContacts(contactsCallback));
            return;
        }
        if(!contactsThread.isThisIt()) {
            contactsThread.run(() -> fetchContacts(contactsCallback));
            return;
        }
        if(contactCache != null) {
            callSeriallyOnIdle(() -> 
                contactsCallback.completed(contactCache));
            return;
        }
        if(existsInStorage("contacts.json")) {
            contactCache = new ChatContact().
                getPropertyIndex().
                loadJSONList("contacts.json");
            
            callSerially(() -> contactsCallback.completed(contactCache));
            for(ChatContact c : contactCache) {
                if(existsInStorage(c.name.get() + ".jpg")) {
                    String f = c.name.get() + ".jpg";
                    try (InputStream is = createStorageInputStream(f)) {
                        c.photo.set(EncodedImage.create(is));
                    } catch(IOException err) {
                        Log.e(err);
                    }
                }
            }
            return;
        }
        
        Contact[] contacts = Display.getInstance().getAllContacts(true,
            true, false, true, false, false);
        ArrayList<ChatContact> l = new ArrayList<>();
        for(Contact c : contacts) {
            ChatContact cc = new ChatContact().
                phone.set(c.getPrimaryPhoneNumber()).
                name.set(c.getDisplayName());
            l.add(cc);
            callSeriallyOnIdle(() -> {
                cc.photo.set(c.getPhoto());
                if(cc.photo.get() != null) {
                    contactsThread.run(() -> {
                        String f = cc.name.get() + ".jpg";
                        try(OutputStream os = 
                                createStorageOutputStream(f)) {
                            EncodedImage img = EncodedImage.
                                createFromImage(cc.photo.get(), true);
                            os.write(img.getImageData());
                        } catch(IOException err) {
                            Log.e(err);
                        }
                    });
                }
            });
        }
        
        PropertyIndex.storeJSONList("contacts.json", l);
        callSerially(() -> contactsCallback.completed(l));
    }
    
    public static void findRegisteredUser(String phone, 
            OnComplete<ChatContact> resultCallback) {
        get("user/findRegisteredUser").
            queryParam("phone", phone).
            fetchAsPropertyList(res -> {
                List l = res.getResponseData();
                if(l.size() == 0) {
                    resultCallback.completed(null);
                    return;
                }
                resultCallback.completed((ChatContact)l.get(0));
            }, ChatContact.class);
    }
    
    public static void findRegisteredUserById(String id, 
            OnComplete<ChatContact> resultCallback) {
        get("user/findRegisteredUserById").
            queryParam("id", id).
            fetchAsPropertyList(res -> {
                List l = res.getResponseData();
                if(l.size() == 0) {
                    resultCallback.completed(null);
                    return;
                }
                resultCallback.completed((ChatContact)l.get(0));
            }, ChatContact.class);
    }

    public static void fetchChatList(
            OnComplete<List<ChatContact>> contactsCallback) {
        fetchContacts(cl -> {
            List<ChatContact> response = new ArrayList<>();
            for(ChatContact c : cl) {
                if(c.lastActivityTime.get() != null) {
                    response.add(c);
                }
            }
            Collections.sort(response, 
                (ChatContact o1, ChatContact o2) -> 
                    (int)(o1.lastActivityTime.get().getTime() - 
                        o2.lastActivityTime.get().getTime()));
            contactsCallback.completed(response);
        });
    }
    
    public static void ackMessage(String messageId) {
        post("user/ackMessage").
            body(messageId).fetchAsString(c -> {});
    }

    public static void updatePushKey(String key) {
        if(user() != null) {
            get("user/updatePushKey").
                queryParam("id", user().id.get()).
                queryParam("key", key).fetchAsString(c -> {});
        }
    }
}
