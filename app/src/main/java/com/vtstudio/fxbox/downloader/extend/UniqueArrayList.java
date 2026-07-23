package com.vtstudio.fxbox.downloader.extend;

import java.util.ArrayList;

public class UniqueArrayList<T> extends ArrayList<T> {

    private boolean uniqueEnabled = true;
    @Override
    public boolean add(T t) {
        if(uniqueEnabled && contains(t)){
            removeAllOccurrences(t);
            super.add(0, t);
            return true;
        } else {
            return super.add(t);
        }
    }

    public void removeAllOccurrences(T element){
        int index = indexOf(element);
        while (index != -1){
            remove(index);
            index = indexOf(element);
        }
    }

    public boolean isUniqueEnabled() {
        return uniqueEnabled;
    }

    public void setUniqueEnabled(boolean uniqueEnabled) {
        this.uniqueEnabled = uniqueEnabled;
    }
}
