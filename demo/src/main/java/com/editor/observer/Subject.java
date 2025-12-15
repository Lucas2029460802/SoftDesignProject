package com.editor.observer;

/**
 * 主题接口，用于观察者模式
 */
public interface Subject {
    void attach(Observer observer);
    void detach(Observer observer);
    void notifyObservers(Event event);
}







