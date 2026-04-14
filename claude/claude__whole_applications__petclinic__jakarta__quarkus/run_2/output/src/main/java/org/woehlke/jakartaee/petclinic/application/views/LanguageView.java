package org.woehlke.jakartaee.petclinic.application.views;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Simplified for Quarkus migration - JSF removed
 */
public interface LanguageView extends Serializable {

    long serialVersionUID = 5845484688944674808L;

    Map<String, String> getCountries();

    void setCountries(List countries);

    Locale getLocale();

    void setLocale(Locale locale);

    String getLocaleSelected();

    void setLocaleSelected(String localeSelected);

    String changeLanguage();

    String getMsgCantDeleteSpecialty();
}
