package net.minebo.cobalt.service;

public abstract class CService {

    public abstract String getName();
    public abstract void onEnable();

    public void onDisable() {

    }

}
