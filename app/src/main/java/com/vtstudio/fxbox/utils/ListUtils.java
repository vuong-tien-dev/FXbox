package com.vtstudio.fxbox.utils;

import java.util.List;

public class ListUtils {
    public static <T> void addUnique(List<T> list, T element) {
        if(list == null) return;

        removeAllOccurrences(list, element);
        list.add(0, element);

    }

    public static <T> void removeAllOccurrences(List<T> list, T element){
        if(list == null) return;

        int index = list.indexOf(element);
        while (index != -1){
            list.remove(index);
            index = list.indexOf(element);
        }
    }

    public static <T> boolean containsIn (List<T> list, T element) {
        for (int i = 0; i < list.size(); i++) {
            if(list.get(i).equals(element)) return true;
        }
        return false;
    }
}
