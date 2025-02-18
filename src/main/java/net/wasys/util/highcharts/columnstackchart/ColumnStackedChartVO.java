package net.wasys.util.highcharts.columnstackchart;

import java.util.List;

public class ColumnStackedChartVO {

    private List<String> categories;
    private List<ColumnStackedSeriesVO> series;

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<ColumnStackedSeriesVO> getSeries() {
        return series;
    }

    public void setSeries(List<ColumnStackedSeriesVO> series) {
        this.series = series;
    }
}
