import { useRecoilValue } from "recoil";
import Highcharts from "highcharts";
import DarkUnica from "highcharts/themes/dark-unica";
import Light from "highcharts/themes/high-contrast-light";
import "ag-grid-community/styles/ag-grid.css";
import "ag-grid-community/styles/ag-theme-quartz.css";
import "ag-grid-community/styles/ag-theme-alpine.css";
import "./App.css";
import useInterceptor from "./hooks/useInterceptor";
import { themeState } from "./store/atom";
import { Suspense, useEffect } from "react";
import { Box, CssBaseline } from "@mui/material";
import CustomTheme from "./custom-theme";
import Header from "./layout/header";
import Sidebar from "./layout/sidebar";
import Body from "./layout/body";
import CustomAlert from "./components/custom-alert";

function App() {
  useInterceptor();
  const theme = useRecoilValue(themeState);

  const themeType =
    theme === "Lightblue" || theme === "Pink" ? "light" : "dark";

  useEffect(() => {
    themeType === "dark" ? DarkUnica(Highcharts) : Light(Highcharts);
  }, [themeType]);

  return (
    <CustomTheme>
      <div className={`custom-theme-${themeType}`}>
        <CssBaseline />
        <Box
          sx={{
            height: "100vh",
            display: "grid",
            gridTemplateColumns: "64px 1fr",
            gridTemplateRows: "64px 1fr",
            gridTemplateAreas: `
            "header header"
            "sidebar body"
            `,
          }}
        >
          <Header />
          <Sidebar />
          <Suspense
            fallback={
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
                Loading...
              </Box>
            }
          >
            <Body />
          </Suspense>
        </Box>
        <CustomAlert />
      </div>
    </CustomTheme>
  );
}

export default App;
