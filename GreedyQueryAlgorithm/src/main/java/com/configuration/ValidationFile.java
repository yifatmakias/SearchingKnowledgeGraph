package com.configuration;

/**
 * @Author: yhj
 * @Description:
 * @Date: Created in 2018/10/26.
 */
public enum ValidationFile {
    SEARCH_MAP("result\\ValidationFiles\\search.txt", 1);
    //SEARCH_MAP("result\\ValidationFiles\\germany.txt", 1);


    private String name;
    private int index;
    private ValidationFile(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public static String getName(int index) {
        for (ValidationFile c : ValidationFile.values()) {
            if (c.getIndex() == index) {
                return c.name;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
