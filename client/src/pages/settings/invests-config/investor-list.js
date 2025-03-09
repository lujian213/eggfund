import { useRecoilState, useRecoilValue, useSetRecoilState } from "recoil";
import { investorsQuery } from "../../../store/selector";
import { useEffect, useState } from "react";
import moment from "moment";
import { Button, Chip, Stack, TextField } from "@mui/material";
import { LocalizationProvider, DatePicker } from "@mui/x-date-pickers";
import { AdapterMoment } from "@mui/x-date-pickers/AdapterMoment";
import CustomAvatar from "../../../utils/get-icons";
import { searchFormState, selectedInvestorState } from "./store/atom";

export default function InvestorList() {
  const investors = useRecoilValue(investorsQuery);
  const [selected, setSelected] = useRecoilState(selectedInvestorState);
  const [form, setForm] = useState({
    raiseRate: "",
    from: "",
    to: "",
    batch: "",
  });
  const setSearchForm = useSetRecoilState(searchFormState);

  useEffect(() => {
    setSelected(investors[0]?.id);
  }, [investors, setSelected]);

  const handleChange = (event) => {
    const value = event.target.value;
    const name = event.target.name;
    setForm((pre) => ({ ...pre, [name]: value }));
  };

  const handleSearch = () => {
    setSearchForm(form);
  };

  return (
    <Stack
      direction={"row"}
      spacing={2}
      sx={{ alignItems: "center", justifyContent: "space-between" }}
    >
      <Stack direction={"row"} spacing={1} sx={{ alignItems: "center" }}>
        {investors.map((item) => {
          return (
            <Chip
              key={item.id}
              icon={
                <CustomAvatar
                  id={item.icon || 0}
                  style={{
                    height: "24px",
                    width: "24px",
                  }}
                />
              }
              label={item.name}
              color="primary"
              variant={selected === item.id ? "filled" : "outlined"}
              clickable={true}
              onClick={() => setSelected(item.id)}
            />
          );
        })}
      </Stack>
      <Stack direction={"row"} spacing={1}>
        <LocalizationProvider dateAdapter={AdapterMoment}>
          <DatePicker
            value={form.from ? moment(form.from, "YYYY-MM-DD") : null}
            label="From"
            onChange={(newValue) =>
              setForm((pre) => ({
                ...pre,
                from: newValue?.format("YYYY-MM-DD"),
              }))
            }
            format="YYYY-MM-DD"
            slotProps={{
              textField: {
                variant: "standard",
                sx: { width: "160px" },
                InputLabelProps: { shrink: true },
                clearable: true,
              },
            }}
          />
        </LocalizationProvider>
        <LocalizationProvider dateAdapter={AdapterMoment}>
          <DatePicker
            value={form.to ? moment(form.to, "YYYY-MM-DD") : null}
            label="To"
            onChange={(newValue) =>
              setForm((pre) => ({
                ...pre,
                to: newValue?.format("YYYY-MM-DD"),
              }))
            }
            format="YYYY-MM-DD"
            slotProps={{
              textField: {
                variant: "standard",
                sx: { width: "160px" },
                InputLabelProps: { shrink: true },
                clearable: true,
              },
            }}
          />
        </LocalizationProvider>
        <TextField
          id="standard-batch"
          InputLabelProps={{ shrink: true }}
          sx={{
            width: "100px",
          }}
          label="Batch"
          type="number"
          variant="standard"
          name="batch"
          value={form.batch}
          onChange={handleChange}
        />
        <Button variant="contained" onClick={handleSearch}>
          Search
        </Button>
      </Stack>
    </Stack>
  );
}
