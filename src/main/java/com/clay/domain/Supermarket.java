package com.clay.domain;

/**
 * @author clay
 */
public class Supermarket implements Store {

    @Override
    public void sell() {
        System.out.println("invoke supermarket sell method");
    }

}
