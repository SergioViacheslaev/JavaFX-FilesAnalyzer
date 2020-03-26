package org.home.textfinder.api;

/**
 * @author Sergei Viacheslaev
 */
public interface Observable {
    void addObserver(Observer observer);

    void removeObserver(Observer observer);

    void notifyObservers(Object arg);

}
