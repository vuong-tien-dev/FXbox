package com.vtstudio.fxbox.media.models;

import java.util.List;

public class TextSegment extends ApiModel {
    private List<String> segments;

    public List<String> getSegments() {
        return segments;
    }

    public void setSegments(List<String> segments) {
        this.segments = segments;
    }
}
