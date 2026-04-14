package spring.tutorial.mood.web;

import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class MoodService {
    private String mood = "awake";

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }
}
