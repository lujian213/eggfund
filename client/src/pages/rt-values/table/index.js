import { useRecoilState, useRecoilValue } from "recoil";
import { rtValuesState } from "../../../store/atom";
import { datasourceState } from "../store/atom";
import { useEffect, useMemo, useState } from "react";
import moment from "moment";
import InsightsIcon from "@mui/icons-material/Insights";
import QueryStatsIcon from "@mui/icons-material/QueryStats";
import {
  IconButton,
  Stack,
  Tooltip,
  useMediaQuery,
  useTheme,
} from "@mui/material";
import AggridWrapper from "../../../components/aggrid-wrapper";
import { formatNumberByPercent } from "../../../utils/process-number";
import TrendChartModal from "./trend-chart-modal";
import RealtimeTrendChartModal from "./realtime-trend-chart";

export default function RtValuesTable() {
  const theme = useTheme();
  const isLarge = useMediaQuery(theme.breakpoints.up("md"));
  const rtValues = useRecoilValue(rtValuesState);
  const [datasource, setDatasource] = useRecoilState(datasourceState);
  const [trendChartModal, setTrendChartModal] = useState({
    open: false,
    data: null,
  });
  const [realtimeTrendChartModal, setRealtimeTrendChartModal] = useState({
    open: false,
    data: null,
  });

  useEffect(() => {
    if (!rtValues || rtValues.length === 0) return;
    setTimeout(() => {
      setDatasource((pre) => {
        return pre
          .map((item) => {
            const findItem = rtValues.find((data) => data.code === item.code);
            if (!findItem) return item;
            return { ...item, ...findItem };
          })
          .toSorted((a, b) => {
            return a.increaseRate - b.increaseRate;
          });
      });
    }, 0);
  }, [setDatasource, rtValues]);

  const colDefs = useMemo(() => {
    if (isLarge) {
      return [
        {
          headerName: "",
          width: 120,
          cellRenderer: ActionRenderer,
          cellRendererParams: {
            setTrendChartModal,
            setRealtimeTrendChartModal,
          },
        },
        {
          field: "code",
          flex: 1,
        },
        {
          field: "fundName",
          flex: 1,
          valueGetter: (p) => (p.data.alias ? p.data.alias : p.data.fundName),
        },
        {
          field: "type",
          flex: 1,
        },
        {
          field: "day",
          flex: 1,
          valueGetter: (p) => (p.data.day ? p.data.day : "-"),
          cellStyle: (params) => {
            if (params.value !== moment().format("YYYY-MM-DD")) {
              return { backgroundColor: "darkgrey" };
            }
            return null;
          },
        },
        {
          field: "unitValue",
          flex: 1,
          valueGetter: (p) => (p.data.unitValue ? p.data.unitValue : "-"),
        },
        {
          field: "increaseRate",
          flex: 1,
          cellRenderer: RateRenderer,
          cellStyle: (params) => {
            const backgroundColor =
              Math.abs(params.value) > 0.01 ? "gold" : "transparent";
            if (params.value < 0) {
              return { color: "green", backgroundColor };
            } else if (params.value > 0) {
              return { color: "red", backgroundColor };
            }
            return null;
          },
        },
        {
          field: "time",
          flex: 1,
          valueGetter: (p) => (p.data.time ? p.data.time : "-"),
        },
      ];
    } else {
      return [
        {
          field: "fundName",
          flex: 1,
          valueGetter: (p) => (p.data.alias ? p.data.alias : p.data.fundName),
        },
        {
          field: "increaseRate",
          flex: 1,
          cellRenderer: RateRenderer,
          cellStyle: (params) => {
            const rateAbs = Math.abs(params.value);
            let backgroundColor = "transparent";
            if(rateAbs > 0.05) {
              backgroundColor = "gold";
            } else if(rateAbs > 0.03) {
              backgroundColor = "lightgoldenrodyellow";
            } else if (rateAbs > 0.01) {
              backgroundColor = "lightyellow";
            }
            if (params.value < 0) {
              return { color: "green", backgroundColor };
            } else if (params.value > 0) {
              return { color: "red", backgroundColor };
            }
            return null;
          },
        },
        {
          field: "time",
          flex: 1,
          valueGetter: (p) => (p.data.time ? p.data.time : "-"),
          cellStyle: (params) => {
            if (params.data.day !== moment().format("YYYY-MM-DD")) {
              return { backgroundColor: "darkgrey" };
            }
            return null;
          },
        },
      ];
    }
  }, [isLarge]);

  return (
    <Stack style={{ flex: 1 }}>
      <AggridWrapper rowData={datasource} columnDefs={colDefs} />
      <TrendChartModal
        open={trendChartModal.open}
        data={trendChartModal.data}
        handleClose={() => setTrendChartModal({ open: false, data: null })}
      />
      <RealtimeTrendChartModal
        open={realtimeTrendChartModal.open}
        data={realtimeTrendChartModal.data}
        handleClose={() =>
          setRealtimeTrendChartModal({ open: false, data: null })
        }
      />
    </Stack>
  );
}

function RateRenderer(params) {
  return params.value ? `${formatNumberByPercent(params.value)}` : "-";
}

function ActionRenderer(params) {
  const { data, setTrendChartModal, setRealtimeTrendChartModal } = params;
  const { code, fundName } = data || {};
  return (
    <Stack direction={"row"} spacing={1}>
      <Tooltip title="Unit Value Trending Over Time">
        <IconButton
          onClick={() =>
            setTrendChartModal({ open: true, data: { code, fundName } })
          }
        >
          <QueryStatsIcon />
        </IconButton>
      </Tooltip>
      <Tooltip title="Real-Time Unit Value Throughout the Day">
        <IconButton
          onClick={() =>
            setRealtimeTrendChartModal({ open: true, data: { code, fundName } })
          }
        >
          <InsightsIcon />
        </IconButton>
      </Tooltip>
    </Stack>
  );
}
