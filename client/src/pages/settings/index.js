import {
  Box,
  Collapse,
  Divider,
  Stack,
  Typography,
  useMediaQuery,
  useTheme,
} from "@mui/material";
import { Suspense, useState } from "react";
import KeyboardArrowDownIcon from "@mui/icons-material/KeyboardArrowDown";
import KeyboardArrowUpIcon from "@mui/icons-material/KeyboardArrowUp";
import InvestorConfig from "./investor-config";
import FundConfig from "./fund-config";
import InvestsConfig from "./invests-config";

export default function Settings() {
  const theme = useTheme();
  const isLarge = useMediaQuery(theme.breakpoints.up("md"));

  const shouldOpen = isLarge ? true : false;

  return (
    <Stack spacing={1} style={{ height: "100%" }}>
      <SettingsSection title="Investor" shouldOpen={shouldOpen}>
        <InvestorConfig />
      </SettingsSection>
      <Divider />
      <SettingsSection title="Fund" shouldOpen={shouldOpen}>
        <FundConfig />
      </SettingsSection>
      <Divider />
      <Stack sx={{ flex: 1 }}>
        <SettingsSection title="Invests">
          <InvestsConfig />
        </SettingsSection>
      </Stack>
    </Stack>
  );
}

function SettingsSection({ title, children, shouldOpen = true }) {
  const [open, setOpen] = useState(shouldOpen);

  return (
    <Box>
      <Stack
        direction={"row"}
        alignItems={"center"}
        onClick={() => setOpen(!open)}
        sx={{ cursor: "pointer" }}
      >
        <Typography variant="h6">{title}</Typography>
        {open ? (
          <KeyboardArrowUpIcon sx={{ marginLeft: "auto" }} />
        ) : (
          <KeyboardArrowDownIcon sx={{ marginLeft: "auto" }} />
        )}
      </Stack>
      <Suspense fallback={<div>loading...</div>}>
        <Collapse in={open}>{children}</Collapse>
      </Suspense>
    </Box>
  );
}
