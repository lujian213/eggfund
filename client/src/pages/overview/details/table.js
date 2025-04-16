import { useEffect, useMemo, useRef, useState } from "react";
import { useRecoilValue } from "recoil";
import { summaryQuery } from "../../../store/selector";
import AggridWrapper from "../../../components/aggrid-wrapper";
import {
  Box,
  Button,
  ButtonGroup,
  Stack,
  Switch,
  useMediaQuery,
  useTheme,
} from "@mui/material";
import { selectedInvestorState, themeState } from "../../../store/atom";
import {
  formatNumber,
  formatNumberByPercent,
} from "../../../utils/process-number";
import axios from "axios";
import MonetizationOnIcon from "@mui/icons-material/MonetizationOn";
import ShoppingCartIcon from "@mui/icons-material/ShoppingCart";

const BASE_URL = process.env.REACT_APP_BASE_URL;

export default function DetailsTable({ handleModalOpen, type="items" }) {
  const muiTheme = useTheme();
  const isLarge = useMediaQuery(muiTheme.breakpoints.up("md"));

  const summary = useRecoilValue(summaryQuery);
  const { items, clearanceMap } = summary;
  const theme = useRecoilValue(themeState);
  const gridRef = useRef();
  const [datasource, setDatasource] = useState([]);
  const [selectedDataType, setSelectedDataType] = useState(type);

  useEffect(() => {
    if (items && clearanceMap) {
      let data = [];
      if (selectedDataType === "items" && items) {
        data = [...items];
      } else {
        data = clearanceMap[selectedDataType] || [];
      }
      const sortedItem = data.toSorted((a, b) => {
        if (a.day === b.day) {
          return a.batch - b.batch;
        }
        return a.day - b.day;
      });
      setDatasource(sortedItem);
    }
  }, [selectedDataType, items, clearanceMap]);

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

  let colDefs = [
    {
      field: "type",
      headerName: "",
      maxWidth: 60,
      cellRenderer: TypeRenderer,
      cellStyle: {
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
      },
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
      cellRenderer: PriceRenderer,
      cellRendererParams: { prices, themeType },
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
      field: "liquidatedQuota",
      flex: 1,
      valueGetter: (p) => formatNumber(p.data.liquidatedQuota),
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
      field: "tax",
      flex: 1,
      valueGetter: (p) => formatNumber(p.data.tax),
    },
    {
      field: "batch",
      flex: 1,
      valueGetter: (p) => (p.data.quota !== 0 ? p.data.batch : "-"),
    },
    {
      field: "comments",
      flex: 1,
    },
    {
      field: "enabled",
      flex: 1,
      cellRenderer: ActionsRenderer,
      cellRendererParams: { handleUpdateRowStyles, selectedDataType },
    },
  ];

  if(!isLarge) {
    colDefs.splice(0, 1);
  }

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
    <Stack sx={{ height: "100%" }}>
      <ButtonGroup aria-label="Basic button group">
        <Button
          key={"items"}
          variant={selectedDataType === "items" ? "contained" : "outlined"}
          onClick={() => setSelectedDataType("items")}
        >
          Default
        </Button>
        {Object.keys(clearanceMap || {}).map((key) => (
          <Button
            key={key}
            variant={selectedDataType === key ? "contained" : "outlined"}
            onClick={() => setSelectedDataType(key)}
          >
            {key}
          </Button>
        ))}
      </ButtonGroup>
      <Box sx={{ flexGrow: 1, height: "100%" }}>
        <AggridWrapper
          ref={gridRef}
          rowData={JSON.parse(JSON.stringify(datasource))}
          columnDefs={colDefs}
          defaultColDef={defaultColDef}
          getRowStyle={getRowStyle}
          onRowDoubleClicked={() => handleModalOpen(selectedDataType)}
        />
      </Box>
    </Stack>
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

function ActionsRenderer({
  value,
  data,
  handleUpdateRowStyles,
  selectedDataType,
}) {
  const { investId } = data;
  const selectedInvestor = useRecoilValue(selectedInvestorState);
  const [checked, setChecked] = useState(value);

  const handleChange = async (event) => {
    setChecked(event.target.checked);
    handleUpdateRowStyles(investId, event.target.checked);
    await axios.post(
      `${BASE_URL}/disableinvest/${selectedInvestor}/${investId}`,
      null,
      {
        params: {
          enabled: event.target.checked,
        },
      }
    );
  };

  if (!investId) return null;

  return (
    <Switch
      disabled={selectedDataType !== "items"}
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
