import { useSetRecoilState } from "recoil";
import { themeState } from "../store/atom";
import { useState } from "react";
import {
  Box,
  IconButton,
  Menu,
  MenuItem,
  Stack,
  Tooltip,
  Typography,
} from "@mui/material";
import PaletteIcon from '@mui/icons-material/Palette';

export default function Header() {
  const setTheme = useSetRecoilState(themeState);
  const [anchorEl, setAnchorEl] = useState(null);
  const open = Boolean(anchorEl);
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
              '&:before': {
                content: '"',
                display: 'block',
                position: 'absolute',
                top: 0,
                right: 14,
                width: 10,
                height: 10,
                bgcolor: 'background.paper',
                transform: 'translate(-50%) rotate(45deg)',
                zIndex: 0,
              }
            },
          }}
          transformOrigin={{horizontal: 'right', vertical: 'top'}}
          anchorOrigin={{horizontal: 'right', vertical: "bottom"}}
        >
            {
                ["Dark", "Purple", "Lightblue", "Pink"].map(theme => {
                    return <MenuItem key="theme" onClick={() => handleThemeChange(theme)}>
                        {theme}
                    </MenuItem>
                })
            }
        </Menu>
      </Stack>
    </Box>
  );
}
