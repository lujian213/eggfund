import Button from "@mui/material/Button";
import Typography from "@mui/material/Typography";
import Modal from "@mui/material/Modal";
import { Stack } from "@mui/system";
import { useRecoilValue, useSetRecoilState } from "recoil";
import { useEffect, useState } from "react";
import axios from "axios";
import {
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  TextField,
} from "@mui/material";
import { fundsQuery } from "../../../store/selector";
import {
  refreshInvestsState,
  selectedFundState,
  selectedInvestorState,
} from "./store/atom";
import moment from "moment";
import { LocalizationProvider, DatePicker } from "@mui/x-date-pickers";
import { AdapterMoment } from "@mui/x-date-pickers/AdapterMoment";

const style = {
  position: "absolute",
  top: "50%",
  left: "50%",
  transform: "translate (-50%, -50%)",
  width: 800,
  boxshadow: 24,
  background: (theme) => theme.palette.background.sidebar,
  p: 4,
};
export default function InvestModal(props) {
  const { open, handleSubmit, handleClose, data, mode, maxBatch } = props;
  const funds = useRecoilValue(fundsQuery);
  const refetchInvests = useSetRecoilState(refreshInvestsState);
  const selectedFund = useRecoilValue(selectedFundState);
  const selectedInvestor = useRecoilValue(selectedInvestorState);
  const [form, setForm] = useState({
    code: selectedFund,
    day: moment().format("YYYY-MM-DD"),
    type: "trade",
    share: "",
    unitPrice: "",
    fee: 0,
    batch: maxBatch,
    comments: "",
  });

  useEffect(() => {
    const fundData = funds.find((item) => item.id === selectedFund);
    const etf = fundData?.etf;
    let batch = mode === "edit" ? data.batch : maxBatch;
    if (etf) {
      setForm((pre) => ({
        ...pre,
        ...data,
        code: selectedFund,
        etf: fundData?.etf,
        name: fundData?.name,
        batch,
      }));
    } else {
      setForm((pre) => ({
        ...pre,
        ...data,
        code: selectedFund,
        etf: fundData?.etf,
        name: fundData?.name,
        unitPrice: -1,
        batch,
      }));
    }
  }, [data, funds, maxBatch, mode, selectedFund]);

  const confirm = async () => {
    let response;
    if (mode === "add") {
      response = await axios.put(
        `/invest/${selectedInvestor}/${selectedFund}`,
        [
          {
            code: selectedFund,
            ...form,
          },
        ]
      );
    } else {
      response = await axios.post(`/invest/${selectedInvestor}`, {
        code: selectedFund,
        ...form,
      });
    }
    response?.data && handleModalClose();
    handleSubmit && handleSubmit();
    refetchInvests((pre) => pre + 1);
  };

  const handleChange = (event) => {
    const name = event.target.name;
    const value = event.target.value;
    setForm((pre) => ({ ...pre, [name]: value }));
  };

  const handleModalClose = () => {
    handleClose();
    setForm({
      code: selectedFund,
      day: moment().format("YYYY-MM-DD"),
      type: "trade",
      share: "",
      unitPrice: "",
      fee: 0,
      batch: maxBatch,
      comments: "",
    });
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
          {mode === "add" ? "Add" : "Edit"} Invest
        </Typography>
        <Stack spacing={2}>
          <TextField
            size="small"
            label="Investor"
            variant="outlined"
            name="id"
            value={selectedInvestor}
            InputLabelProps={{ shrink: true }}
            disabled={true}
          />
          <TextField
            size="small"
            label="Fund"
            variant="outlined"
            name="code"
            value={form?.code || ""}
            InputLabelProps={{ shrink: true }}
            disabled={true}
          />
          <TextField
            size="small"
            label="Name"
            variant="outlined"
            name="name"
            value={form?.name || ""}
            InputLabelProps={{ shrink: true }}
            disabled={true}
          />
          <LocalizationProvider dateAdapter={AdapterMoment}>
            <DatePicker
              value={form.day ? moment(form.day, "YYYY-MM-DD") : null}
              label="Day"
              onChange={(newValue) =>
                setForm((pre) => ({
                  ...pre,
                  day: newValue?.format("YYYY-MM-DD"),
                }))
              }
              format="YYYY-MM-DD"
              slotProps={{
                textField: {
                  size: "small",
                  clearable: true,
                },
              }}
            />
          </LocalizationProvider>
          <FormControl fullWidth size="small">
            <InputLabel id={"type-select-label"} shrink>
              Type
            </InputLabel>
            <Select
              notched
              labelId="type-select-label"
              id="type-select"
              value={String(form?.type)}
              label="type"
              name="type"
              onChange={handleChange}
            >
              {["trade", "dividend"].map((item) => (
                <MenuItem key={item} value={item}>
                  {item}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <TextField
            size="small"
            label="Share"
            variant="outlined"
            name="share"
            value={form?.share || ""}
            InputLabelProps={{ shrink: true }}
            onChange={handleChange}
          />
          {(form.type !== "trade" || form.etf) && (
            <TextField
              size="small"
              label="Unit Price"
              variant="outlined"
              name="unitPrice"
              value={form?.unitPrice || ""}
              InputLabelProps={{ shrink: true }}
              onChange={handleChange}
            />
          )}
          {form.type === "trade" && (
            <>
              <TextField
                size="small"
                label="Fee"
                variant="outlined"
                name="fee"
                value={form?.fee || ""}
                InputLabelProps={{ shrink: true }}
                onChange={handleChange}
              />
              <TextField
                size="small"
                label="Batch"
                variant="outlined"
                name="batch"
                type="number"
                value={form?.batch || ""}
                InputLabelProps={{ shrink: true }}
                onChange={handleChange}
              />
            </>
          )}
          <TextField
            size="small"
            label="Comments"
            variant="outlined"
            name="comments"
            value={form?.comments || ""}
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
