import { Box, Divider, Stack, Typography } from "@mui/material";
import { Suspense } from "react";
import InvestorConfig from "./investor-config";
import FundConfig from "./fund-config";
import InvestsConfig from "./invests-config";

export default function Settings() {
  return (
    <Stack spacing={1} style={{ height: "100%" }}>
      <Box>
        <Typography variant="h6">Investor</Typography>
        <Suspense fallback={<div>loading...</div>}>
          <InvestorConfig />
        </Suspense>
      </Box>
      <Divider />
      <Box>
        <Typography variant="h6">Fund</Typography>
        <Suspense fallback={<div>loading...</div>}>
          <FundConfig />
        </Suspense>
      </Box>
      <Divider />
      <Stack sx={{flex: 1}}>
      <Typography variant="h6">Invests</Typography>
        <Suspense fallback={<div>loading...</div>}>
          <InvestsConfig />
        </Suspense>
      </Stack>
    </Stack>
  );
}
