package com.creditmodule.ing.service;

import org.springframework.stereotype.Component;

@Component
public class MatchingSwitch {
    private final java.util.concurrent.atomic.AtomicBoolean running = new java.util.concurrent.atomic.AtomicBoolean(false);
    public boolean isRunning() { return running.get(); }
    public void turnOn() { running.set(true); }
    public void turnOff() { running.set(false); }
}