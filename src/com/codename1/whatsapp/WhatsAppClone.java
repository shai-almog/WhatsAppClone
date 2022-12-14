package com.codename1.whatsapp;


import com.codename1.components.InfiniteProgress;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Form;
import com.codename1.ui.Dialog;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import com.codename1.io.Log;
import com.codename1.ui.Toolbar;
import com.codename1.io.Preferences;
import com.codename1.push.Push;
import com.codename1.push.PushCallback;
import com.codename1.sms.activation.ActivationForm;
import com.codename1.sms.activation.SMSVerification;
import com.codename1.util.SuccessCallback;
import com.codename1.whatsapp.forms.ChatForm;
import com.codename1.whatsapp.forms.MainForm;
import com.codename1.whatsapp.model.ChatContact;
import com.codename1.whatsapp.model.ChatMessage;
import com.codename1.whatsapp.model.Server;
import com.codename1.whatsapp.model.ServerMessages;
import java.util.List;

/**
 * This file was generated by <a href="https://www.codenameone.com/">Codename One</a> for the purpose 
 * of building native mobile applications using Java.
 */
public class WhatsAppClone implements PushCallback {

    private Form current;
    private Resources theme;

    public void init(Object context) {
        // use two network threads instead of one
        updateNetworkThreadCount(2);

        theme = UIManager.initFirstTheme("/theme");

        // Enable Toolbar on all Forms by default
        Toolbar.setGlobalToolbar(true);

        // Pro only feature
        Log.bindCrashProtection(true);

        Server.init();

        addNetworkErrorListener(err -> {
            // prevent the event from propagating
            err.consume();
            if(err.getError() != null) {
                Log.e(err.getError());
            }
            Log.sendLogAsync();
            Dialog.show("Connection Error", 
                "There was a networking error in the connection to " + 
                    err.getConnectionRequest().getUrl(), "OK", null);
        });        
    }

    @Override
    public void push(String value) {
    }

    @Override
    public void registeredForPush(String deviceId) {
        Server.updatePushKey(Push.getPushKey());
    }

    @Override
    public void pushRegistrationError(String error, int errorCode) {
    }
    
    static class SMSVerifyImpl extends SMSVerification {
        @Override
        public void sendSMSCode(String phone) {
            Dialog d = new InfiniteProgress().showInfiniteBlocking();
            Server.signup(new ChatContact().phone.set(phone), 
                c -> d.dispose());
        }

        @Override
        public void verifySmsCode(String code,
            SuccessCallback<Boolean> s) {
            Dialog d = new InfiniteProgress().showInfiniteBlocking();
            boolean result = Server.verify(code);
            d.dispose();
            s.onSucess(result);
        }
    }
    
    private void bindMessageListener() {
        Server.bindMessageListener(new ServerMessages() {
            @Override
            public void connected() {
            }

            @Override
            public void disconnected() {
            }

            @Override
            public void messageReceived(ChatMessage m) {
                Form f = getCurrentForm();
                if(f instanceof ChatForm) {
                    ChatForm cf = (ChatForm)f;
                    if(cf.getContact().id.equals(m.authorId.get())) {
                        cf.addMessageToUI(m);
                    }
                } 
                MainForm.getInstance().refreshChatsContainer();
            }

            @Override
            public void userTyping(String contactId) {
            }

            @Override
            public void messageViewed(String msgId, List<String> userIds) {
            }
        });
    }
    
    public void start() {
        if(current != null){
            current.show();
            bindMessageListener();
            return;
        }
        String phoneNumber = Preferences.get("PhoneNumber", null);
        if(phoneNumber == null) {
            ActivationForm.create("Signup").
                codeDigits(6).
                show(s -> {
                    Log.p("SMS Activation returned " + s);
                    Preferences.set("PhoneNumber", s);
                    new MainForm().show();
                }, new SMSVerifyImpl());                
        } else {
            new MainForm().show();
        }
        bindMessageListener();
        callSerially(() -> registerPush());
    }

    public void stop() {
        Server.closeWebsocketConnection();
        current = getCurrentForm();
        if(current instanceof Dialog) {
            ((Dialog)current).dispose();
            current = getCurrentForm();
        }
    }
    
    public void destroy() {
    }
}
