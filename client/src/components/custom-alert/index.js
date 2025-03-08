import { Alert, Snackbar } from "@mui/material";
import { useRecoilState } from "recoil";
import { alertState } from "../../store/atom";

export default function CustomAlert() {
  const [alert, setAlert] = useRecoilState(alertState);

  const handleClose = (event, reason) => {
    if (reason === "clickaway") return;
    setAlert({ open: false, type: undefined, message: "" });
  };

  return (
    <Snackbar
      open={alert.open}
      onClose={handleClose}
      autoHideDuration={alert.type === "error" ? null : 3000}
    >
      <Alert onClose={handleClose} severity={alert.type} sx={{ width: "100%" }}>
        {alert.message}
      </Alert>
    </Snackbar>
  );
}
