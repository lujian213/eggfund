import { styled, useTheme } from "@mui/material/styles";
import axios from "axios";
import { useEffect, useState } from "react";
import {
  formatNumber,
  formatNumberByPercent,
} from "../../../utils/process-number";
import { Divider, Drawer, IconButton } from "@mui/material";
import ChevronLeftIcon from "@mui/icons-material/ChevronLeft";
import ChevronRightIcon from "@mui/icons-material/ChevronRight";
import AggridWrapper from "../../../components/aggrid-wrapper";
import { useRecoilValue } from "recoil";
import { fundsQuery } from "../../../store/selector";

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

  useEffect(() => {
    const fetchDatasource = async () => {
      const response = await axios.post(`${BASE_URL}/summary/${investor}`);
      const result = response.data;
      const { investSummaryList, ...rest } = result;
      const items = investSummaryList.map((item) => {
        const fund = funds.find((f) => f.id === item.fundId);
        return {
          ...item,
          fundId: fund?.alias || fund?.name || item.fundId,
        };
      }
      );
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
  }, [investor, funds]);

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
      <AggridWrapper
        rowData={datasource}
        columnDefs={colDefs}
        getRowStyle={getRowStyle}
      />
    </Drawer>
  );
}
