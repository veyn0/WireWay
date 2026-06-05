package xyz.wireway.frame.transmit;

import xyz.wireway.frame.datasource.DataSource;

public class DataSourceInfo {

    private final int id;
    private final DataSource dataSource;

    private boolean startedSending = false;

    public DataSourceInfo(int id, DataSource dataSource) {
        this.id = id;
        this.dataSource = dataSource;
    }

    public boolean isStartedSending() {
        return startedSending;
    }

    public void startedSending() {
        this.startedSending = true;
    }

    public int getId() {
        return id;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
