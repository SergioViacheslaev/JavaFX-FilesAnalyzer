package org.home.filesanalyzer.controllers.api;

public interface Observable {

    void addObserver(Observer observer);

    void notifyObservers(Object arg);

}
