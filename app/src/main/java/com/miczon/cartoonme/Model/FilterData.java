package com.miczon.cartoonme.Model;

import java.util.ArrayList;

public class FilterData {
    private static FilterData instance;
    private ArrayList<String> filterIds = new ArrayList<>();

    // Private constructor to prevent external instantiation
    private FilterData() {
    }

    // Singleton pattern to get the single instance of FilterData
    public static FilterData getInstance() {
        if (instance == null) {
            instance = new FilterData();
        }
        return instance;
    }

    // Method to set filter IDs
    public void setFilterIds(ArrayList<String> filterIds) {
        this.filterIds = filterIds;
    }

    // Method to get filter IDs
    public ArrayList<String> getFilterIds() {
        return filterIds;
    }
}