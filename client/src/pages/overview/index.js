import { useRecoilValue } from "recoil";
import { selectedFundState, selectedInvestorState } from "../../store/atom";
import { Suspense, useState } from "react";
import { Alert, Divider, Stack, useTheme } from "@mui/material";
import CategoryList from "./category";
import CodeList from "./code-list";
import BasicInfo from "./basic-info";
import Details from "./details";
import Summary from "./summary";
import Charts from "./charts";
import TotalSummaryDrawer from "./total-summary-drawer.js";

export default function Overview() {
  const theme = useTheme();
  const selectedFund = useRecoilValue(selectedFundState);
  const selectedInvestor = useRecoilValue(selectedInvestorState);
  const [totalSummaryDrawer, setTotalSummaryDrawer] = useState({
    open: false,
    investor: null,
  });

  const handleDrawerOpen = (investor) => {
    setTotalSummaryDrawer({
      open: true,
      investor,
    });
  };

  return (
    <Stack spacing={1} sx={{ height: "100%" }}>
      <Suspense fallback={<div>Loading...</div>}>
        <CategoryList handleDrawerOpen={handleDrawerOpen} />
      </Suspense>
      <Divider />
      <Suspense fallback={<div>Loading...</div>}>
        <CodeList />
      </Suspense>
      <Divider />
      {selectedFund ? (
        <Suspense fallback={<div>Loading...</div>}>
          <BasicInfo />
        </Suspense>
      ) : (
        <Alert severity="info">Please choose a fund first</Alert>
      )}
      <Divider />
      {selectedFund && selectedInvestor ? (
        <Suspense fallback={<div>Loading...</div>}>
          <Stack sx={{ flexGrow: 1, gap: '0.5rem', [theme.breakpoints.up('md')]: { flexDirection: "row" } }}>
            <Details />
            <Summary />
          </Stack>
          <Charts />
        </Suspense>
      ) : (
        <Alert severity="info">Please choose a Investor first</Alert>
      )}
      <TotalSummaryDrawer
        open={totalSummaryDrawer.open}
        investor={totalSummaryDrawer.investor}
        handleDrawerClose={() =>
          setTotalSummaryDrawer({
            open: false,
            investor: null,
          })
        }
      />
    </Stack>
  );
}
