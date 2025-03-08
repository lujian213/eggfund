import { ListItemIcon, MenuItem, MenuList, Stack } from "@mui/material";
import { Link, useLocation } from "react-router-dom";
import ShowChartIcon from '@mui/icons-material/ShowChart';
import SettingsIcon from '@mui/icons-material/Settings';
import MonitorHeartIcon from '@mui/icons-material/MonitorHeart';

export default function Sidebar() {
  const location = useLocation();

  const isSelected = (path) => location.pathname.includes(path);

  return (
    <Stack
      sx={{
        gridArea: "sidebar",
        height: "100%",
        background: (theme) => theme.palette.background.sidebar,
        color: "text.secondary",
      }}
    >
      <MenuList sx={{ paddingTop: 2 }}>
        <MenuItem
          component={Link}
          to={`/overview`}
          selected={isSelected("overview")}
        >
            <ListItemIcon>
                <ShowChartIcon />
            </ListItemIcon>
        </MenuItem>
        <MenuItem
          component={Link}
          to={`/rt-values`}
          selected={isSelected("rt-values")}
        >
            <ListItemIcon>
                <MonitorHeartIcon />
            </ListItemIcon>
        </MenuItem>
        <MenuItem
          component={Link}
          to={`/settings`}
          selected={isSelected("settings")}
        >
            <ListItemIcon>
                <SettingsIcon />
            </ListItemIcon>
        </MenuItem>
      </MenuList>
    </Stack>
  );
}
