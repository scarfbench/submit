package org.woehlke.jakartaee.petclinic.application.views;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.application.conf.PetclinicApplication;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Simplified for Quarkus migration - JSF removed
 */
@Log
@Getter
@Setter
@Named("languageView")
@ApplicationScoped
public class LanguageViewImpl implements LanguageView, Serializable {

    private static final long serialVersionUID = -5444922829398489233L;

    private final Locale DEFAULT = Locale.ENGLISH;
    private final Locale[] LOCALE_OPTIONS = {Locale.ENGLISH, Locale.GERMAN};

    @Inject
    private PetclinicApplication petclinicApplication;

    @Inject
    private FlashMessagesView flashMessagesView;

    private Locale locale;

    private String localeSelected;

    private Map<String, String> countries = new HashMap<>();

    public LanguageViewImpl() {
        this.locale = DEFAULT;
        this.localeSelected = DEFAULT.getLanguage();
    }

    public Map<String, String> getCountries() {
        this.countries.clear();
        for (Locale locale : LOCALE_OPTIONS) {
            this.countries.put(
                    locale.getDisplayLanguage(),
                    locale.getLanguage()
            );
        }
        return countries;
    }

    public void setCountries(List countries) {
        // Stub - no longer using SelectItem
    }

    public Locale getLocale() {
        if (this.locale == null) {
            locale = Locale.ENGLISH;
        }
        return locale;
    }

    @Deprecated
    public String getMsgCantDeleteSpecialty() {
        String msg = "";
        if (locale.equals(Locale.ENGLISH)) {
            msg = "cannot delete, Specialty still in use";
        } else if (locale.equals(Locale.GERMAN)) {
            msg = "löschen nicht möglich, Fachrichtung wird noch ausgeübt";
        }
        return msg;
    }

    public String changeLanguage() {
        Locale myLocale = new Locale(this.localeSelected);
        String msg = "cool: newLocale: " + this.locale + " -> " + myLocale;
        log.info("changed Language " + msg);
        this.flashMessagesView.addInfoMessage("changed Language", msg);
        this.setLocale(myLocale);
        return "#";
    }

    @PostConstruct
    public void postConstruct() {
        log.info("postConstruct: "+LanguageViewImpl.class.getSimpleName());
        countries = this.getCountries();
    }

    @PreDestroy
    public void preDestroy() {
        log.info("preDestroy");
    }

}
