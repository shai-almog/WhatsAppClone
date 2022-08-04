package com.codename1.whatsapp.forms;

import com.codename1.components.MultiButton;
import com.codename1.components.ToastBar;
import com.codename1.ui.Form;
import static com.codename1.ui.CN.*;
import com.codename1.ui.FontImage;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.whatsapp.model.ChatContact;
import com.codename1.whatsapp.model.Server;

public class NewMessageForm extends Form {
    public NewMessageForm() {
        super("Select Contact", BoxLayout.y());
        Form current = getCurrentForm();
        getToolbar().setBackCommand("", e -> current.showBack());
        MultiButton newGroup = new MultiButton("New group");
        MultiButton newContact = new MultiButton("New contact");
        FontImage.setMaterialIcon(newGroup, 
            FontImage.MATERIAL_GROUP_ADD, 3.5f);
        FontImage.setMaterialIcon(newContact, 
            FontImage.MATERIAL_PERSON_ADD, 3.5f);
        
        add(newGroup);
        add(newContact);

        Server.fetchContacts(lst -> {
            for(ChatContact c : lst) {
                MultiButton mb = new MultiButton(c.name.get());
                mb.setTextLine2(c.tagline.get());
                mb.setIcon(c.getSmallIcon());
                c.photo.addChangeListener(p -> 
                    mb.setIcon(c.getSmallIcon()));
                mb.addActionListener(e -> {
                    if(c.id.get() == null) {
                        Server.findRegisteredUser(
                                c.phone.get(), contact -> {
                            if(contact == null) {
                                current.showBack();
                                callSerially(() -> 
                                    ToastBar.showErrorMessage(
                                        "Contact isn't registered"));
                                return;
                            }
                            c.id.set(contact.id.get());
                            Server.saveContacts();
                        });
                        return;
                    }
                    new ChatForm(c, current).show();
                });
                add(mb);
            }
            revalidate();
        });
    }
}
