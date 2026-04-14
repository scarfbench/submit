package org.quarkus.samples.petclinic.system;

import java.util.HashMap;
import java.util.Map;

/**
 * Application message constants.
 * Replaces Quarkus Qute @MessageBundle with a simple Map.
 */
public class AppMessages {

    private static final Map<String, String> MESSAGES = new HashMap<>();

    static {
        MESSAGES.put("find_owner", "Find Owner");
        MESSAGES.put("last_name", "Last Name");
        MESSAGES.put("edit_owner", "Edit Owner");
        MESSAGES.put("add_owner", "Add Owner");
        MESSAGES.put("find_owners", "Find Owners");
        MESSAGES.put("name", "Name");
        MESSAGES.put("address", "Address");
        MESSAGES.put("telephone", "Telephone");
        MESSAGES.put("city", "City");
        MESSAGES.put("pets", "Pets");
        MESSAGES.put("pet", "Pet");
        MESSAGES.put("error", "Error");
        MESSAGES.put("none", "None");
        MESSAGES.put("veterinarians", "Veterinarians");
        MESSAGES.put("specialties", "Specialties");
        MESSAGES.put("welcome", "Welcome");
        MESSAGES.put("birthdate", "Birthdate");
        MESSAGES.put("type", "Type");
        MESSAGES.put("owner", "Owner");
        MESSAGES.put("edit_pet", "Edit Pet");
        MESSAGES.put("add_pet", "Add Pet");
        MESSAGES.put("add_visit", "Add Visit");
        MESSAGES.put("visit_date", "Visit Date");
        MESSAGES.put("description", "Description");
        MESSAGES.put("new_", "New");
        MESSAGES.put("visit", "Visit");
        MESSAGES.put("date", "Date");
        MESSAGES.put("home", "Home");
        MESSAGES.put("pets_and_visits", "Pets and Visits");
        MESSAGES.put("previous_visits", "Previous Visits");
        MESSAGES.put("something_wrong", "Something happened...");
    }

    public static Map<String, String> getMessages() {
        return MESSAGES;
    }
}
