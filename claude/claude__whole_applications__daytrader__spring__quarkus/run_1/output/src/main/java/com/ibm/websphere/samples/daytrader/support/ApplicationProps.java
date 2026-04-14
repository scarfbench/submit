package com.ibm.websphere.samples.daytrader.support;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ApplicationProps {
    @ConfigProperty(name = "daytrader.max-quotes", defaultValue = "1000")
    int maxQuotes;

    @ConfigProperty(name = "daytrader.max-users", defaultValue = "500")
    int maxUsers;

    public int getMaxQuotes() { return maxQuotes; }
    public void setMaxQuotes(int maxQuotes) { this.maxQuotes = maxQuotes; }
    public int getMaxUsers() { return maxUsers; }
    public void setMaxUsers(int maxUsers) { this.maxUsers = maxUsers; }
}