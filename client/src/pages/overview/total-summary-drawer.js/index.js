import { styled, useTheme } from "@mui/material/styles";
import axios from "axios";
import { useEffect, useState } from "react";
import {
  formatNumber,
  formatNumberByPercent,
} from "../../../utils/process-number";
import { Divider, Drawer, IconButton } from "@mui/material";
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft'
import ChevronRightIcon from '@mui/icons-material/ChevronRight'
import AggridWrapper from "../../../components/aggrid-wrapper";


const drawerWidth = 1200;

const DrawerHeader = styled("div")(({ theme }) => ({
  display: "flex",
  alignItems: "center",
  padding: theme.spacing(0, 1),
  ...theme.mixins.toolbar,
  justifyContent: "flex-start",
}));

export default function TotalSummaryDrawer(props) {
  const { open, investor, handleDrawerClose } = props;
  const theme = useTheme();
  const [datasource, setDatasource] = useState();

  useEffect(() => {
    const fetchDatasource = async () => {
      const response = await axios.post(`/summary/${investor}`);
      const result = response.data;
      const { investSummaryList, ...rest } = result;
      const data = [
        ...investSummaryList,
        {
          fundId: "Total",
          ...rest,
        },
      ];
      setDatasource(data);
    };
    investor && fetchDatasource();
  }, [investor]);

  const colDefs = [
    { field: "fundId", flex: 1 },
    {
      field: "totalLongAmt",
      flex: 1,
      valueGetter: (p) => formatNumber(p.data.totalLongAmt),
    },
    {
      field: "totalShortAmt",
      flex: 1,
      valueGetter: (p) => formatNumber(p.data.totalShortAmt),
    },
    {
      field: "totalFee",
      flex: 1,
      valueGetter: (p) => formatNumber(p.data.totalFee),
    },
    {
      field: "earning",
      flex: 1,
      valueGetter: (p) => formatNumber(p.data.earning),
    },
    {
      field: "grossEarning",
      flex: 1,
      valueGetter: (p) => formatNumber(p.data.grossEarning),
    },
    {
      field: "netAmt",
      flex: 1,
      valueGetter: (p) => formatNumber(p.data.netAmt),
    },
    {
      field: "predictedValue",
      flex: 1,
      valueGetter: (p) => formatNumber(p.data.predictedValue),
    },
    {
      field: "grossEarningRate",
      flex: 1,
      valueGetter: (p) => formatNumberByPercent(p.data.crossEarningRate),
    },
    {
      field: "earningRate",
      flex: 1,
      valueGetter: (p) => formatNumberByPercent(p.data.earningRate),
    },
  ];

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
      <AggridWrapper rowData={datasource} columnDefs={colDefs} />
    </Drawer>
  );
}
