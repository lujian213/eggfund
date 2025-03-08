import Button from "@mui/material/Button";
import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";
import Modal from "@mui/material/Modal";
import { Stack } from "@mui/system";

const style = {
  position: "absolute",
  top: "50%",
  left: "50%",
  transform: "translate (-50%, -50%)",
  width: 400,
  boxshadow: 24,
  background: (theme) => theme.palette.background.sidebar,
  p: 4,
};
export default function ConfirmModal(props) {
  const { open, handleSubmit, handleClose, message } = props;

  const confirm = async () => {
    await handleSubmit();
    handleClose();
  };

  return (
    <Modal
      open={open}
      onClose={handleClose}
      aria-labelledby="modal-modal-title"
      aria-describedby="modal-modal-description"
    >
      <Stack sx={style} spacing={2}>
        <Typography id="confirm-modal-title">{message}</Typography>
        <Box sx={{ alignSelf: "flex-end" }}>
          <Button variant="contained" onClick={confirm} autoFocus>
            Yes
          </Button>
          <Button onClick={handleClose}>No</Button>
        </Box>
      </Stack>
    </Modal>
  );
}
