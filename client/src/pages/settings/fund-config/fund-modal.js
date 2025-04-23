import Button from "@mui/material/Button";
import Typography from "@mui/material/Typography";
import Modal from "@mui/material/Modal";
import { Stack } from "@mui/system";
import { useSetRecoilState } from "recoil";
import { refreshFundState } from "../../../store/atom";
import { useEffect, useState } from "react";
import axios from "axios";
import {
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  TextField,
} from "@mui/material";
import { BASE_URL } from "../../../utils/get-baseurl";

const style = {
  position: "absolute",
  top: "50%",
  left: "50%",
  transform: "translate(-50%, -50%)",
  width: 'min(800px, 90%)',
  boxshadow: 24,
  background: (theme) => theme.palette.background.sidebar,
  p: 4,
};

export default function FundModal(props) {
  const { open, handleSubmit, handleClose, data, mode } = props;
  const refetchFunds = useSetRecoilState(refreshFundState);
  const [form, setForm] = useState({});

  useEffect(() => {
    setForm({ ...data });
  }, [data]);

  const confirm = async () => {
    let response;
    if (mode === "add") {
      response = await axios.put(`${BASE_URL}/fund/${form.code}`, form);
    } else {
      response = await axios.post(`${BASE_URL}/fund/${form.code}`, form);
    }
    response?.data && handleModalClose();
    handleSubmit && handleSubmit();
    refetchFunds((pre) => pre + 1);
  };

  const handleChange = (event) => {
    const name = event.target.name;
    const value = event.target.value;
    setForm((pre) => ({ ...pre, [name]: value }));
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
          {mode === "add" ? "Add" : "Edit"} Fund
        </Typography>
        <Stack spacing={2}>
          {mode === "edit" && (
            <TextField
              size="small"
              label="Name"
              variant="outlined"
              name="name"
              value={form?.name || ""}
              InputLabelProps={{ shrink: true }}
              disabled
            />
          )}
          <TextField
            size="small"
            label="Code"
            variant="outlined"
            name="code"
            value={form?.code || ""}
            InputLabelProps={{ shrink: true }}
            onChange={handleChange}
          />
          <TextField
            size="small"
            label="Alias"
            variant="outlined"
            name="alias"
            value={form?.alias || ""}
            InputLabelProps={{ shrink: true }}
            onChange={handleChange}
          />
          <FormControl fullWidth size="small">
            <InputLabel id={"eft-select-label"} shrink>
              Security
            </InputLabel>
            <Select
              notched
              labelId="eft-select-label"
              id="etf-select"
              value={String(form?.etf)}
              label="Security"
              name="etf"
              onChange={handleChange}
            >
              {["false", "true"].map((item) => (
                <MenuItem key={item} value={item}>
                  {item}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <TextField
            size="small"
            label="Priority"
            variant="outlined"
            name="priority"
            value={form?.priority || ""}
            InputLabelProps={{ shrink: true }}
            onChange={handleChange}
          />
          <TextField
            size="small"
            label="URL"
            variant="outlined"
            name="url"
            value={form?.url || ""}
            InputLabelProps={{ shrink: true }}
            onChange={handleChange}
          />
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
