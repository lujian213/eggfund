import { useRecoilValue } from "recoil";
import { selectedFundState, selectedInvestorState } from "./store/atom";
import { Alert, Stack } from "@mui/material";
import { Suspense } from "react";
import CodeList from "./fund-list";
import InvestorList from "./investor-list";
import Invests from "./invests";

export default function InvestsConfig() {
  const selectedFund = useRecoilValue(selectedFundState);
  const selectedInvestor = useRecoilValue(selectedInvestorState);

  return (
    <Stack
      spacing={1}
      sx={{
        flex: 1,
        paddingBlock: "8px",
      }}
    >
      <Suspense fallback={<div>loading...</div>}>
        <CodeList />
      </Suspense>
      {selectedFund ? (
        <Suspense fallback={<div>loading...</div>}>
          <InvestorList />
        </Suspense>
      ) : (
        <Alert severity="info">Please choose a fund first</Alert>
      )}
      {selectedFund && selectedInvestor ? (
        <Suspense fallback={<div>loading...</div>}>
          <Invests />
        </Suspense>
      ) : (
        <Alert severity="info">Please choose a investor first</Alert>
      )}
    </Stack>
  );
}
