import { useEffect } from "react";
import { useMediaQuery, useTheme } from "@mui/material";

export default function useViewportScale() {
  const theme = useTheme();
  const isLessThanMd = useMediaQuery(theme.breakpoints.down("md"));

  useEffect(() => {
    const viewportMetaTag = document.querySelector('meta[name="viewport"]');
    if (isLessThanMd) {
      viewportMetaTag.setAttribute(
        "content",
        "width=device-width, initial-scale=0.7"
      );
    } else {
      viewportMetaTag.setAttribute(
        "content",
        "width=device-width, initial-scale=0.1"
      );
    }
  }, [isLessThanMd]);
}
