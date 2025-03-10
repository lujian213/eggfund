import { Button, Modal, Stack, TextField, Typography } from "@mui/material";
import { useState } from "react";

const style = {
  position: "absolute",
  top: "50%",
  left: "50%",
  transform: "translate(-50%, -50%)",
  width: "500px",
  boxshadow: 24,
  background: (theme) => theme.palette.background.sidebar,
  p: 4,
};

export default function CalculateShareModal(props) {
  const { open, price, handleClose } = props;
  const [amount, setAmount] = useState(0);

  const calculatedShare = Math.floor(amount / price);

  const handleModalClose = () => {
    handleClose();
    setAmount(0);
  };

  const handleChange = (event) => {
    const value = event.target.value;
    setAmount(value);
  };

  return (
    <Modal
      open={open}
      onClose={handleClose}
      aria-labelledby="modal-modal-title"
      aria-describedby="modal-modal-description"
    >
      <Stack sx={style} spacing={2}>
        <Typography id="modal-modal-title">Calculate Share</Typography>
        <Stack spacing={2}>
          <TextField
            size="small"
            label="Amount"
            type="number"
            InputLabelProps={{ shrink: true }}
            value={amount}
            onChange={handleChange}
          />
          <TextField
            size="small"
            label="Share"
            type="number"
            InputLabelProps={{ shrink: true }}
            value={calculatedShare}
            disabled={true}
          />
        </Stack>
        <Stack direction={"row"} spacing={2} sx={{ alignSelf: "flex-end" }}>
          <Button onClick={handleModalClose}>Cancel</Button>
        </Stack>
      </Stack>
    </Modal>
  );
}
