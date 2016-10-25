package com.gmobi.poponews.model;

/**
 * User: vivian .
 * Date: 2016-07-15
 * Time: 09:15
 */
public class UninterestReason {
    String reason;
    boolean selected;

    public UninterestReason(String reason, boolean selected) {
        this.reason = reason;
        this.selected = selected;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
