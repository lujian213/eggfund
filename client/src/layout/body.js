import { Box } from "@mui/material";
import { Outlet } from "react-router-dom";
import useRTValueHook from "../hooks/useRTValueHook";

export default function Body() {
  useRTValueHook();

  return (
    <Box
      sx={{
        gridArea: "body",
        padding: 2,
        height: "100%",
        background: (theme) => theme.palette.background.main,
        overflow: "auto",
      }}
      color="text.secondary"
    >
      <Outlet />
    </Box>
  );
}
