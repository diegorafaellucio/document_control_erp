package net.wasys.util.highcharts.piechart;

import java.util.List;

public class PieChartSeriesVO {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isColorByPoint() {
        return colorByPoint;
    }

    public void setColorByPoint(boolean colorByPoint) {
        this.colorByPoint = colorByPoint;
    }

    public List<PieChartDataVO> getData() {
        return data;
    }

    public void setData(List<PieChartDataVO> data) {
        this.data = data;
    }

    private boolean colorByPoint;

    private List<PieChartDataVO> data;



}
