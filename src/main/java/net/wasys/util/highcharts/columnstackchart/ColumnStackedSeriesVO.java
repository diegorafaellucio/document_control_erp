package net.wasys.util.highcharts.columnstackchart;

import java.util.List;

public class ColumnStackedSeriesVO {

    private String name;
    private List<Long> data;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Long> getData() {
        return data;
    }

    public void setData(List<Long> data) {
        this.data = data;
    }
}
