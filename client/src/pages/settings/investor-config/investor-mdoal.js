import Button from "@mui/material/Button";
import Typography from "@mui/material/Typography";
import Modal from "@mui/material/Modal";
import { Stack } from "@mui/system";
import { useSetRecoilState } from "recoil";
import { refreshInvestorState } from "../../../store/atom";
import { useEffect, useState } from "react";
import axios from "axios";
import {
  TextField,
} from "@mui/material";
import AvatarSelect from './avatar-select';

const style = {
  position: "absolute",
  top: "50%",
  left: "50%",
  transform: "translate(-50%, -50%)",
  width: 800,
  boxshadow: 24,
  background: (theme) => theme.palette.background.sidebar,
  p: 4,
};
export default function InvestorModal(props) {
  const { open, handleSubmit, handleClose, data, mode } = props;
  const refetchInvestor = useSetRecoilState(refreshInvestorState);
  const [form, setForm] = useState({});

  useEffect(() => {
    setForm({ ...data });
  }, [data]);

  const confirm = async () => {
    let response;
    if (mode === "add") {
      response = await axios.put(`/investor`, null, {
        params: form,
      });
    } else {
      response = await axios.post(`/investor/${form.id}`, null, {
        params: {
          name: form.name,
          icon: form.icon,
        },
      });
    }
    response?.data && handleModalClose();
    handleSubmit && handleSubmit();
    refetchInvestor((pre) => pre + 1);
  };

  const handleChange = (event) => {
    const name = event.target.name;
    const value = event.target.value;
    setForm((pre) => ({ ...pre, [name]: value }));
  };

  const changeAvatar = (index) => {
    setForm((pre) => ({ ...pre, icon: index }));
  };

  const handleModalClose = () => {
    setForm({});
    handleClose();
  };

  return (
    <Modal
      open={open}
      onClose={handleModalClose}
      aria-labelledby="modal-modal-title"
      aria-describedby="modal-modal-description"
    >
      <Stack sx={style} spacing={2}>
        <Typography id="confirm-modal-title">
          {mode === "add" ? "Add" : "Edit"} Investor
        </Typography>
        <Stack spacing={2}>
          <TextField
            size="small"
            label="Id"
            variant="outlined"
            name="id"
            value={form?.id || ""}
            InputLabelProps={{ shrink: true }}
            disabled={mode === "edit"}
            onChange={handleChange}
          />
          <TextField
            size="small"
            label="Name"
            variant="outlined"
            name="name"
            value={form?.name || ""}
            InputLabelProps={{ shrink: true }}
            onChange={handleChange}
          />
          <AvatarSelect iconIndex={form.icon} changeAvatar={changeAvatar} />
        </Stack>
        <Stack direction={"row"} spacing={2} sx={{ alignSelf: "flex-end" }}>
          <Button variant="contained" onClick={confirm} autoFocus>
            Confirm
          </Button>
          <Button onClick={handleModalClose}>Cancel</Button>
        </Stack>
      </Stack>
    </Modal>
  );
}
