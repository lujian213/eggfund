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
import { Box, CssBaseline, useMediaQuery, useTheme } from "@mui/material";
import CustomTheme from "./custom-theme";
import Header from "./layout/header";
import Sidebar from "./layout/sidebar";
import Body from "./layout/body";
import CustomAlert from "./components/custom-alert";
import useViewportScale from "./hooks/useViewportScale";
import useAuth from "./hooks/useAuth";
import Login from "./pages/login";

function App() {
  useViewportScale();
  useInterceptor();
  const { isAuthenticated } = useAuth();
  const theme = useRecoilValue(themeState);
  const muiTheme = useTheme();
  const isLarge = useMediaQuery(muiTheme.breakpoints.up("md"));

  const themeType =
    theme === "Lightblue" || theme === "Pink" ? "light" : "dark";

  let gridAreas = `
    "header header"
    "sidebar body"
    `;

  if (!isLarge) {
    gridAreas = `
      "header header"
      "body body"
    `;
  }

  useEffect(() => {
    themeType === "dark" ? DarkUnica(Highcharts) : Light(Highcharts);
  }, [themeType]);

  return (
    <CustomTheme>
      <div className={`custom-theme-${themeType}`}>
        <CssBaseline />
        {
          !isAuthenticated ? (
            <Login />
          ) : (
            <Box
            sx={{
              height: "100vh",
              display: "grid",
              gridTemplateColumns: "64px 1fr",
              gridTemplateRows: "64px 1fr",
              gridTemplateAreas: gridAreas,
            }}
          >
            <Header />
            {isLarge && <Sidebar />}
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
          )
        }
        <CustomAlert />
      </div>
    </CustomTheme>
  );
}

export default App;
