import { useRecoilValue } from "recoil";
import HighchartsReact from "highcharts-react-official";
import Highcharts from 'highcharts';
import { summaryQuery } from "../../../store/selector";
import { roundDecimal } from "../../../utils/process-number";

export default function Charts() {
  const summary = useRecoilValue(summaryQuery);
  const items = summary?.items || [];

  const categories = items.map((item) => item.day);
  const quotaSeriesData = items.map((item) => item.quota);
  const priceSeriesData = items.map((item) => {
    return {
      y: parseFloat(item.price.toFixed(4)),
      marker: {
        enabled: item.quota !== 0,
        lineWidth: 2,
        lineColor: "#4840d6",
        fileColor: "#fff",
      },
    };
  });

  const totalInvestAmtSeriesData = items.reduce((acc, item) => {
    const previousValue = acc[acc.length - 1] || 0;
    const addedValue = roundDecimal(previousValue + item.investAmt);
    acc.push(Number(addedValue));
    return acc;
  }, []);

  const amtSeriesData = items.map((item) =>
    Number(roundDecimal(item.investAmt))
  );

  const options = {
    chart: {
      zooming: {
        type: "xy",
      },
      height: 400,
    },
    title: {
      text: "Trend",
      align: "left",
    },
    xAxis: [
      {
        categories: categories,
        crosshair: true,
      },
    ],
    yAxis: [
      {
        labels: {
          format: "{value}",
          style: {
            color: "rgb(43, 144, 143)",
          },
        },
        title: {
          text: "",
          style: {
            color: "rgb(43, 144, 143)",
          },
        },
        opposite: true,
      },
      {
        labels: {
          format: "{value}",
          style: {
            color: "rgb(144, 238, 126)",
          },
        },
        title: {
          text: "",
          style: {
            color: "rgb(144, 238, 126)",
          },
        },
        gridLineWidth: 0,
      },
    ],
    tooltip: {
      shared: true,
      formatter: function () {
        let tooltip = "<b>" + this.x + "</b>";
        let amtSeriesData;
        const amtSeriesIndex = this.points[0].series.chart.series.findIndex(
          (series) => series.name === "Amt Series"
        );
        if (amtSeriesIndex !== -1) {
          amtSeriesData =
            this.points[0].series.chart.series[amtSeriesIndex].data[
              this.points[0].point.index
            ]?.y;
        }
        this.points.forEach((point) => {
          if (point.series.name === "Quota" && amtSeriesData !== undefined) {
            tooltip +=
              "<br/><span style='color:" +
              point.color +
              "'>\u25CF</span> " +
              point.series.name +
              ": <b>" +
              point.y +
              "(" +
              amtSeriesData +
              ")</b>";
          } else {
            tooltip +=
              "<br/><span style='color:" +
              point.color +
              "'>\u25CF</span> " +
              point.series.name +
              ": <b>" +
              point.y +
              "</b>";
          }
        });
        return tooltip;
      },
    },
    series: [
      {
        name: "Quota",
        type: "column",
        threshold: 0,
        data: quotaSeriesData,
        color: "rgb(43, 144, 143)",
        tooltip: {
          valueSuffix: "",
        },
        dataLabels: {
          enabled: true,
          inside: false,
          style: {
            fontWeight: "bold",
            color: "black",
          },
          formatter: function () {
            return this.y !== 0 ? this.y : null;
          },
        },
      },
      {
        name: "Price",
        type: "line",
        yAxis: 1,
        data: priceSeriesData,
        color: "rgb(144, 238, 126)",
        tooltip: {
          valueSuffix: "",
        },
      },
      {
        name: "Total Invest Amt",
        type: "line",
        data: totalInvestAmtSeriesData,
        color: "rgb(244, 91, 91)",
        tooltip: {
          valueSuffix: "",
        },
      },
      {
        name: "Amt Series",
        type: "line",
        data: amtSeriesData,
        visible: false,
        showInLegend: false,
        tooltip: {
          valueSuffix: "",
        },
      },
    ],
    responsive: {
      rules: [
        {
          condition: {
            maxWidth: 500,
          },
          chartOptions: {
            legend: {
              floating: false,
              layout: "horizontal",
              align: "center",
              verticalAlign: "bottom",
              x: 0,
              y: 0,
            },
            yAxis: [
              {
                labels: {
                  align: "right",
                  x: 0,
                  y: -6,
                },
                showLastLabel: false,
              },
              {
                labels: {
                  align: "left",
                  x: 0,
                  y: -6,
                },
                showLastLabel: false,
              },
              {
                visible: false
              }
            ],
          },
        },
      ],
    },
    credit: {
        enabled: false
    }
  };

  return (
    <div>
        <HighchartsReact highcharts={Highcharts} options={options}/>
    </div>
  )
}
