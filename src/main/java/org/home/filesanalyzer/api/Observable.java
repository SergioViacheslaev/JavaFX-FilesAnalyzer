package org.home.filesanalyzer.api;

public interface Observable {

    void addObserver(Observer observer);

    void notifyObservers(Object arg);

}
