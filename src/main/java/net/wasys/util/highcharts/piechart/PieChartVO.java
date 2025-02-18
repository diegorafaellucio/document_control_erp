package net.wasys.util.highcharts.piechart;

import java.util.List;

public class PieChartVO {

    private static final String type = "pie";

    private String title;
    private List<PieChartSeriesVO> series;

    public static String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<PieChartSeriesVO> getSeries() {
        return series;
    }

    public void setSeries(List<PieChartSeriesVO> series) {
        this.series = series;
    }
}
