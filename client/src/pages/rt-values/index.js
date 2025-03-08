import { Divider, Stack } from "@mui/material";
import { Suspense } from "react";
import Funds from "./funds";
import RtValuesTable from "./table";

export default function RtValues() {
  return (
    <Stack spacing={1} style={{ height: "100%" }}>
      <Suspense fallback={<div>loading...</div>}>
        <Funds />
      </Suspense>
      <Divider />
      <Suspense fallback={<div>loading...</div>}>
        <RtValuesTable />
      </Suspense>
    </Stack>
  );
}
