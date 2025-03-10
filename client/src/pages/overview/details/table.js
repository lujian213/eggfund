import { useEffect, useMemo, useRef, useState } from "react";
import { useRecoilValue } from "recoil";
import { summaryQuery } from "../../../store/selector";
import AggridWrapper from "../../../components/aggrid-wrapper";
import { Box, Switch } from "@mui/material";
import { selectedInvestorState, themeState } from "../../../store/atom";
import {
  formatNumber,
  formatNumberByPercent,
} from "../../../utils/process-number";
import axios from "axios";
import MonetizationOnIcon from "@mui/icons-material/MonetizationOn";
import ShoppingCartIcon from "@mui/icons-material/ShoppingCart";

export default function DetailsTable({ handleModalOpen }) {
  const summary = useRecoilValue(summaryQuery);
  const theme = useRecoilValue(themeState);
  const gridRef = useRef();
  const [datasource, setDatasource] = useState([]);

  useEffect(() => {
    if (summary) {
      const items = summary.items || [];
      const sortedItem = items.toSorted((a, b) => {
        if (a.day === b.day) {
          return a.batch - b.batch;
        }
        return a.day - b.day;
      });
      setDatasource(sortedItem);
    }
  }, [summary]);

  const themeType =
    theme === "Lightblue" || theme === "Pink" ? "light" : "dark";
  const prices = datasource.map((item) => item.price).sort((a, b) => a - b);

  const defaultColDef = useMemo(() => {
    return {
      flex: 1,
      minWidth: 120,
      filter: "agTextColumnFilter",
    };
  }, []);

  const handleUpdateRowStyles = (investId, enabled) => {
    gridRef.current.api.forEachNode(function (rowNode) {
      if (rowNode.data.investId === investId) {
        rowNode.setDataValue("enabled", enabled);
      }
    });
  };

  const colDefs = [
    {
      field: "type",
      headerName: "",
      maxWidth: 60,
      cellenderer: TypeRenderer,
      cellStyle: {
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
      },
    },
    {
      field: "batch",
      flex: 1,
      valueGetter: (p) => (p.data.quota !== 0 ? p.data.batch : "-"),
    },
    {
      field: "day",
      flex: 1,
    },
    {
      field: "quota",
      flex: 1,
      valueGetter: (p) => formatNumber(p.data.quota),
    },
    {
      field: "price",
      flex: 1,
      cellenderer: PriceRenderer,
      cellrendererParams: { prices, themeType },
    },
    {
      field: "increaseRate",
      headerName: "COMP Rate",
      flex: 1,
      valueGetter: (p) => `${formatNumberByPercent(p.data.increaseRate)}`,
    },
    {
      field: "price minus2pct",
      headerName: "Price -2%",
      flex: 1,
      valueGetter: (p) => formatNumber(p.data.price_minus2pct, 4),
    },
    {
      field: "price_2pct",
      headerName: "Price +2%",
      flex: 1,
      valueGetter: (p) => formatNumber(p.data.price_2pct, 4),
    },
    {
      field: "fee",
      flex: 1,
      valueGetter: (p) => formatNumber(p.data.fee),
    },
    {
      field: "investAmt",
      flex: 1,
      valueGetter: (p) => formatNumber(p.data.investAmt),
    },
    {
      field: "var",
      flex: 1,
      valueGetter: (p) => `${formatNumberByPercent(p.data.var)}`,
      cellStyle: (params) => {
        if (params.value.includes("(")) {
          return { color: "green" };
        } else if (!params.value.includes("-")) {
          return { color: "red" };
        }
        return null;
      },
    },
    {
      field: "earning",
      flex: 1,
      valueGetter: (p) => formatNumber(p.data.earning),
    },
    {
      field: "comments",
      flex: 1,
    },
    {
      field: "enabled",
      flex: 1,
      cellenderer: ActionsRenderer,
      cellRendererParams: { handleUpdateRowStyles },
    },
  ];

  const getRowStyle = (params) => {
    if (!params.data.enabled) {
      return {
        backgroundColor: "rgb(128 128 128 / 50%) ",
        filter: "brightness(0.4)",
      };
    }
    return {};
  };

  return (
    <AggridWrapper
      ref={gridRef}
      rowData={JSON.parse(JSON.stringify(datasource))}
      columnDefs={colDefs}
      defaultColDef={defaultColDef}
      getRowStyle={getRowStyle}
      onRowDoubleClicked={handleModalOpen}
    />
  );
}

function TypeRenderer({ value }) {
  if (value === "dividend") {
    return (
      <Box
        sx={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          width: "100%",
        }}
      >
        <MonetizationOnIcon sx={{ color: "gold" }} />
      </Box>
    );
  }
  return (
    <Box
      sx={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        width: "100%",
      }}
    >
      <ShoppingCartIcon sx={{ color: "gold" }} />
    </Box>
  );
}

function ActionsRenderer({ value, data, handleUpdateRowStyles }) {
  const { investId } = data;
  const selectedInvestor = useRecoilValue(selectedInvestorState);
  const [checked, setChecked] = useState(value);

  const handleChange = async (event) => {
    setChecked(event.target.value);
    handleUpdateRowStyles(investId, event.target.checked);
    await axios.post(`/disableinvest/${selectedInvestor}/${investId}`, null, {
      params: {
        enabled: event.target.checked,
      },
    });
  };

  if (!investId) return null;

  return (
    <Switch
      checked={checked}
      onChange={handleChange}
      inputProps={{ "aria-label": "controlled" }}
    />
  );
}

function PriceRenderer({ value, prices, themeType }) {
  let startColor = { r: 0, g: 180, b: 6 }; // Dark Green
  let midColor = { r: 108, g: 100, b: 0 }; // Dark Yellow
  let endColor = { r: 100, g: 0, b: 0 }; // Dark Red
  if (themeType === "light") {
    startColor = { r: 0, g: 255, b: 0 }; // Light Green
    midColor = { r: 255, g: 255, b: 0 }; // Light Yellow
    endColor = { r: 255, g: 0, b: 0 }; // Light Red
  }
  function interpolateColor(start, end, factor) {
    const result = {
      r: Math.round(start.r + factor * (end.r - start.r)),
      g: Math.round(start.g + factor * (end.g - start.g)),
      b: Math.round(start.b + factor * (end.b - start.b)),
    };
    return `rgb(${result.r}, ${result.g}, ${result.b})`;
  }

  const colorMap = new Map();
  prices.forEach((price, index) => {
    const factor = index / (prices.length - 1); // Calculate interpolation factor
    let color;
    if (factor <= 0.5) {
      // Interpolate between green and yellow
      color = interpolateColor(startColor, midColor, factor * 2);
    } else {
      // Interpolate between yellow and red
      color = interpolateColor(midColor, endColor, (factor - 0.5) * 2);
    }
    colorMap.set(price, color);
  });
  const coloredPrices = prices.map((price) => ({
    price,
    color: colorMap.get(price),
  }));
  const color = coloredPrices.find((item) => item.price === value)?.color;
  return (
    <Box
      sx={{
        backgroundColor: color,
      }}
    >
      {formatNumber(value, 4)}
    </Box>
  );
}
