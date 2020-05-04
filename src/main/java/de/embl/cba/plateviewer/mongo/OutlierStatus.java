package de.embl.cba.plateviewer.mongo;

public enum OutlierStatus {
    OUTLIER(1), VALID(0), UNKNOWN(-1);

    private final int status;

    private OutlierStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return this.status;
    }
}
