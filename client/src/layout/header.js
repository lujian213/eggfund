import { useRecoilState, useSetRecoilState } from "recoil";
import { themeState, userInfoState } from "../store/atom";
import { useState } from "react";
import {
  Box,
  IconButton,
  Menu,
  MenuItem,
  Stack,
  Tooltip,
  Typography,
  useMediaQuery,
  useTheme,
} from "@mui/material";
import PaletteIcon from "@mui/icons-material/Palette";
import { Link } from "react-router-dom";
import MenuIcon from "@mui/icons-material/Menu";
import axios from "axios";
import { BASE_URL } from "../utils/get-baseurl";
import CustomAvatar from "../utils/get-icons";

export default function Header() {
  const theme = useTheme();
  const isLarge = useMediaQuery(theme.breakpoints.up("md"));
  const setTheme = useSetRecoilState(themeState);
  const [userInfo, setUserInfo] = useRecoilState(userInfoState);
  const [navEl, setNavEl] = useState(null);
  const [anchorEl, setAnchorEl] = useState(null);
  const open = Boolean(anchorEl);
  const navOpen = Boolean(navEl);

  const handleNavClick = (event) => {
    setNavEl(event.currentTarget);
  };
  const handleNavClose = () => {
    setNavEl(null);
  };

  const handleClick = (event) => {
    setAnchorEl(event.currentTarget);
  };
  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleThemeChange = (theme) => {
    localStorage.setItem("custom-theme", theme);
    setTheme(theme);
    handleClose();
  };

  const handleLogout = async () => {
    await axios.post(`${BASE_URL}/logout`);
    // remove authHeader from localStorage
    localStorage.removeItem("EGG-Authorization");
    // remove user from recoil state
    setUserInfo({
      id: null,
      name: null,
      password: null,
      roles: [],
    });
  };

  return (
    <Box
      sx={{
        gridArea: "header",
        height: "100%",
        background: (theme) => theme.palette.background.header,
        color: "text.secondary",
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        paddingRight: "16px",
      }}
    >
      <Typography
        variant="h5"
        gutterBottom
        sx={{
          marginBottom: 0,
          textAlign: "center",
          display: "flex",
          alignItems: "center",
          gap: "1rem",
          padding: "1rem",
        }}
      >
        Egg Fund
      </Typography>
      <Stack
        direction="row"
        spacing={2}
        alignItems={"center"}
        sx={{ paddingRight: "1rem" }}
      >
        {userInfo?.name && (
          <>
            <CustomAvatar
              id={userInfo.icon || 0}
              style={{
                height: "24px",
                width: "24px",
              }}
            />
            <Typography
              variant="h6"
              gutterBottom
              onClick={handleLogout}
              sx={{
                marginBottom: 0,
                textAlign: "center",
                display: "flex",
                alignItems: "center",
                gap: "1rem",
                cursor: "pointer",
                color: "text.secondary",
                "&:hover": {
                  color: "text.primary",
                },
                textDecoration: "underline",
              }}
            >
              logout
            </Typography>
          </>
        )}
        {!isLarge && (
          <>
            <Box
              sx={{
                display: "flex",
                alignItems: "center",
                textAlign: "center",
              }}
            >
              <Tooltip title="Nav">
                <IconButton
                  onClick={handleNavClick}
                  size="small"
                  sx={{ ml: 2 }}
                  aria-controls={navOpen ? "nav-bar" : undefined}
                  aria-haspopup="true"
                  aria-expanded={navOpen ? "true" : undefined}
                >
                  <MenuIcon />
                </IconButton>
              </Tooltip>
            </Box>
            <Menu
              anchorEl={navEl}
              id="nav-bar"
              open={navOpen}
              onClose={handleNavClose}
              onClick={handleNavClose}
              PaperProps={{
                elevation: 0,
                sx: {
                  overflow: "visible",
                  filter: "drop-shadow(0px 2px 8px rgba(0,0,0,0.32))",
                  mt: 1.5,
                  "& .MuiAvatar-root": {
                    width: 32,
                    height: 32,
                    ml: -0.5,
                    mr: 1,
                  },
                  "&:before": {
                    content: '""',
                    display: "block",
                    position: "absolute",
                    top: 0,
                    right: 14,
                    width: 10,
                    height: 10,
                    bgcolor: "background.paper",
                    transform: "translate(-50%) rotate(45deg)",
                    zIndex: 0,
                  },
                },
              }}
              transformOrigin={{ horizontal: "right", vertical: "top" }}
              anchorOrigin={{ horizontal: "right", vertical: "bottom" }}
            >
              <MenuItem component={Link} to={`/overview`}>
                Overview
              </MenuItem>
              <MenuItem component={Link} to={`/rt-values`}>
                Real Time Values
              </MenuItem>
              <MenuItem component={Link} to={`/settings`}>
                Settings
              </MenuItem>
            </Menu>
          </>
        )}
        <Box
          sx={{
            display: "flex",
            alignItems: "center",
            textAlign: "center",
          }}
        >
          <Tooltip title="Theme">
            <IconButton
              onClick={handleClick}
              size="small"
              sx={{ ml: 2 }}
              aria-controls={open ? "account-menu" : undefined}
              aria-haspopup="true"
              aria-expanded={open ? "true" : undefined}
            >
              <PaletteIcon />
            </IconButton>
          </Tooltip>
        </Box>
        <Menu
          anchorEl={anchorEl}
          id="account-menu"
          open={open}
          onClose={handleClose}
          onClick={handleClose}
          PaperProps={{
            elevation: 0,
            sx: {
              overflow: "visible",
              filter: "drop-shadow(0px 2px 8px rgba(0,0,0,0.32))",
              mt: 1.5,
              "& .MuiAvatar-root": {
                width: 32,
                height: 32,
                ml: -0.5,
                mr: 1,
              },
              "&:before": {
                content: '""',
                display: "block",
                position: "absolute",
                top: 0,
                right: 14,
                width: 10,
                height: 10,
                bgcolor: "background.paper",
                transform: "translate(-50%) rotate(45deg)",
                zIndex: 0,
              },
            },
          }}
          transformOrigin={{ horizontal: "right", vertical: "top" }}
          anchorOrigin={{ horizontal: "right", vertical: "bottom" }}
        >
          {["Dark", "Purple", "Lightblue", "Pink"].map((theme) => {
            return (
              <MenuItem key="theme" onClick={() => handleThemeChange(theme)}>
                {theme}
              </MenuItem>
            );
          })}
        </Menu>
      </Stack>
    </Box>
  );
}
