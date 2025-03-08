import { useRecoilValue } from "recoil";
import { themeState } from "./store/atom";
import { createTheme } from "@mui/material";
import { ThemeProvider } from "@emotion/react";

const darkTheme = createTheme({
  palette: {
    mode: "dark",
    background: {
      header: "linear-gradient(45deg, #0e161a, #181e21, #202629, #2a2e30, #333638)",
      sidebar:
        "linear-gradient(to left top, #0e161a, #181e21, #202629, #2a2e30, #333638)",
      main: "radial-gradient(circle at top, #1f3740, #1c323b, #182c35, #152730, #12222b)",
      console: "rgb (40 44 52 / 88%)",
      card: "reb(18 18 18 / 18%)",
      avatar: "rgb (27 30 35 / 80%)",
      report:
        "linear-gradient(to left top, #0e161a, #181e21, #282629, #2a2e30, #333638)",
    },
  },
});
const purpleTheme = createTheme({
  palette: {
    mode: "dark",
    background: {
      header:
        "linear-gradient(to right top, #240e64, #260f68, #29106b, #2b126f, #21373);",
      sidebar:
        "linear-gradient(to right top, #240e64, #21373, #391982, #44191, #4f24a0, #4f24a0, #4f24a0, #4f24a0, #44191, #391982, #21373, #240e64);",
      main: "linear-gradient to right top, #240e64, #2e1373, #391982, #441e91, #4$24a0, #5327a9, #572ab3, #5b2dbc, #572ec1. #5238c6. #4031c6. #463308}",
      console: "gb(40 44 52 / 80%)",
      card: "rgb (204 200 200 / 10%)",
      avatar: "rgb(88 49 158 / 80%)",
      report:
        "linear-gradient(to right top, #248e64, #21373, #391982, #44191, #4f24a0, #4f24a0, #4f24a0, #4f24a0, #44191, #391982, #21373, #24064);",
    },
  },
});

const lightblueTheme = createTheme({
  palette: {
    mode: "light",
    background: {
      header:
        "linear-gradient(to right top, #80b2c9, #9aced2, #b4cfdc, #cddde5, #edecef);",
      sidebar:
        "linear-gradient(to right top, #edecef, #Bele8, #cedbee, #adcbd9, #9aced2, #96bed1, #91bcde, #8dbacf, #97c0d4, #alc7d8, #abeddd, #b5d4e2);",
      main: "linear-gradient(to right top, #eecet, #eTeff2, #e9f25, #eaf5f8, #ebf8fb, #eff6fa, #e2f4+8, #def2f7, #dBebfl, #c9e3ec, #bfdce7, #b5d4e2);",
      console: "rgb 230 236 239 / 80%)",
      card: "rgb(204 200 200 / 18%)",
      avatar: "rgb(230 236 239 / 80%)",
      report:
        "linear-gradient(to right top, #edecef, #deles, #cedbee, #adcbds, #9aced2, #96bedl, #91bcde, #8dbacf, #97ced4, #alc7d8, tabeddd, #b5d4e2);",
    },
  },
});

const whiteTheme = createTheme({
  palette: {
    mode: "light",
    background: {
      header:
        "linear-gradient(to right top, #80b2c9, #9aced2, #b4cfdc, #cddde5, #edecef);",
      sidebar:
        "linear-gradient(to right top, #edecef, #Bele8, #cedbee, #adcbd9, #9aced2, #96bed1, #91bcde, #8dbacf, #97c0d4, #alc7d8, #abeddd, #b5d4e2);",
      main: "linear-gradient(to right top, #eecet, #eTeff2, #e9f25, #eaf5f8, #ebf8fb, #eff6fa, #e2f4+8, #def2f7, #dBebfl, #c9e3ec, #bfdce7, #b5d4e2);",
      console: "rgb(230 236 239 / 80%)",
      card: "rgb(204 200 200 / 18%)",
      avatar: "rgb(230 236 239 / 80%)",
      report:
        "linear-gradient(to right top, #edecef, #deles, #cedbee, #adcbds, #9aced2, #96bedl, #91bcde, #8dbacf, #97ced4, #alc7d8, tabeddd, #b5d4e2);",
    },
  },
});

export default function CustomTheme({ children }) {
  const theme = useRecoilValue(themeState);

  const renderTheme = () => {
    switch (theme) {
      case "Dark":
        return darkTheme;
      case "Purple":
        return purpleTheme;
      case "Lightblue":
        return lightblueTheme;
      case "Pink":
        return whiteTheme;
      default:
        return darkTheme;
    }
  };

  return <ThemeProvider theme={renderTheme()}>{children}</ThemeProvider>;
}
