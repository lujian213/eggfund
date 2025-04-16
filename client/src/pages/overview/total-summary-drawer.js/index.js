import { styled, useTheme } from "@mui/material/styles";
import axios from "axios";
import { useEffect, useState } from "react";
import {
  formatNumber,
  formatNumberByPercent,
} from "../../../utils/process-number";
import { Divider, Drawer, IconButton, Stack } from "@mui/material";
import ChevronLeftIcon from "@mui/icons-material/ChevronLeft";
import ChevronRightIcon from "@mui/icons-material/ChevronRight";
import AggridWrapper from "../../../components/aggrid-wrapper";
import { useRecoilValue } from "recoil";
import { fundsQuery } from "../../../store/selector";
import { DatePicker, LocalizationProvider } from "@mui/x-date-pickers";
import { AdapterMoment } from "@mui/x-date-pickers/AdapterMoment";
import moment from "moment";

const drawerWidth = "min(800px, 90%)";

const BASE_URL = process.env.REACT_APP_BASE_URL;

const DrawerHeader = styled("div")(({ theme }) => ({
  display: "flex",
  alignItems: "center",
  padding: theme.spacing(0, 1),
  ...theme.mixins.toolbar,
  justifyContent: "flex-start",
}));

export default function TotalSummaryDrawer(props) {
  const { open, investor, handleDrawerClose } = props;
  const funds = useRecoilValue(fundsQuery);
  const theme = useTheme();
  const [datasource, setDatasource] = useState();
  const [form, setForm] = useState({
    from: moment(0).format("YYYY-MM-DD"),
    to: moment().format("YYYY-MM-DD"),
  });

  useEffect(() => {
    const fetchDatasource = async () => {
      const response = await axios.post(
        `${BASE_URL}/summary/${investor}`,
        null,
        {
          params: {
            from: form.from,
            to: form.to,
          },
        }
      );
      const result = response.data;
      const { investSummaryList, ...rest } = result;
      const items = investSummaryList.map((item) => {
        const fund = funds.find((f) => f.id === item.fundId);
        return {
          ...item,
          fundId: fund?.alias || fund?.name || item.fundId,
        };
      });
      const data = [
        ...items,
        {
          fundId: "Total",
          ...rest,
        },
      ];
      setDatasource(data);
    };
    investor && fetchDatasource();
  }, [investor, funds, form]);

  const colDefs = [
    { field: "fundId", flex: 1 },
    {
      field: "earning",
      flex: 1,
      valueGetter: (p) => formatNumber(p.data.earning),
    },
    {
      field: "netAmt",
      flex: 1,
      valueGetter: (p) => formatNumber(p.data.netAmt),
    },
    {
      field: "totalTax",
      flex: 1,
      valueGetter: (p) => formatNumber(p.data.totalTax),
    },
    {
      field: "earningRate",
      flex: 1,
      valueGetter: (p) => formatNumberByPercent(p.data.earningRate),
    },
  ];

  const getRowStyle = (params) => {
    if (params.data.fundId === "Total") {
      return { backgroundColor: "#534912" };
    }
    return {};
  };

  return (
    <Drawer
      sx={{
        width: drawerWidth,
        flexShrink: 0,
        "& .MuiDrawer-paper": {
          width: drawerWidth,
        },
      }}
      variant="persistent"
      anchor="right"
      open={open}
    >
      <DrawerHeader>
        <IconButton onClick={handleDrawerClose}>
          {theme.direction === "rtl" ? (
            <ChevronLeftIcon />
          ) : (
            <ChevronRightIcon />
          )}
        </IconButton>
      </DrawerHeader>
      <Divider />
      <Stack direction={"row"} spacing={1} sx={{ padding: 2 }}>
        <LocalizationProvider dateAdapter={AdapterMoment}>
          <DatePicker
            value={form.from ? moment(form.from, "YYYY-MM-DD") : null}
            label="From"
            onChange={(newValue) =>
              setForm((pre) => ({
                ...pre,
                from: newValue?.format("YYYY-MM-DD"),
              }))
            }
            format="YYYY-MM-DD"
            slotProps={{
              textField: {
                variant: "standard",
                sx: { width: "160px" },
                InputLabelProps: { shrink: true },
                clearable: true,
              },
            }}
          />
        </LocalizationProvider>
        <LocalizationProvider dateAdapter={AdapterMoment}>
          <DatePicker
            value={form.to ? moment(form.to, "YYYY-MM-DD") : null}
            label="To"
            onChange={(newValue) =>
              setForm((pre) => ({
                ...pre,
                to: newValue?.format("YYYY-MM-DD"),
              }))
            }
            format="YYYY-MM-DD"
            slotProps={{
              textField: {
                variant: "standard",
                sx: { width: "160px" },
                InputLabelProps: { shrink: true },
                clearable: true,
              },
            }}
          />
        </LocalizationProvider>
      </Stack>
      <AggridWrapper
        rowData={datasource}
        columnDefs={colDefs}
        getRowStyle={getRowStyle}
      />
    </Drawer>
  );
}
