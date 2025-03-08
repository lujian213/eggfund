import { useRecoilState, useRecoilValue } from "recoil";
import { investorsForFundQuery } from "../../../store/selector";
import {
  rtValuesState,
  searchFormState,
  selectedCategoryTypeState,
  selectedFundState,
  selectedInvestorForCategoryState,
  selectedInvestorState,
} from "../../../store/atom";
import { useEffect, useRef, useState } from "react";
import moment from "moment";
import { roundDecimal } from "../../../utils/process-number";
import axios from "axios";
import {
  Button,
  Chip,
  Divider,
  IconButton,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import styles from "./basic-info.module.css";
import { LocalizationProvider, DatePicker } from "@mui/x-date-pickers";
import { AdapterMoment } from "@mui/x-date-pickers/AdapterMoment";
import LoopIcon from "@mui/icons-material/Loop";
import CustomAvatar from "../../../utils/get-icons";

export default function BasicInfo() {
  const investors = useRecoilValue(investorsForFundQuery);
  const [selected, setSelected] = useRecoilState(selectedInvestorState);
  const selectedFund = useRecoilValue(selectedFundState);
  const [form, setForm] = useState({
    raiseRate: "",
    from: moment(0).format("YYYY-MM-DD"),
    to: moment().format("YYYY-MM-DD"),
    batch: "",
  });
  const [searchForm, setSearchForm] = useRecoilState(searchFormState);
  const rtValues = useRecoilValue(rtValuesState);
  const selectedInvestorForCategory = useRecoilValue(
    selectedInvestorForCategoryState
  );
  const categoryType = useRecoilValue(selectedCategoryTypeState);

  const findRtvalue = rtValues.find((item) => item.code === selectedFund);
  const increaseRateRef = useRef();
  const currentSelectedInvestorRef = useRef();

  useEffect(() => {
    if (findRtvalue && selected) {
      if (
        increaseRateRef.current !== findRtvalue.increaseRate ||
        currentSelectedInvestorRef.current !== selected
      ) {
        const batchMap = localStorage.getItem("batchMap")
          ? JSON.parse(localStorage.getItem("batchMap"))
          : [];
        const batch =
          batchMap.find(
            (item) => item[0] === selectedFund && item[1] === selected
          )?.[2] || "";
        setSearchForm({
          raiseRate:
            findRtvalue.day === moment().format("YYYY-MM-DD")
              ? findRtvalue.increaseRate
              : "0.00",
          from: moment(0).format("YYYY-MM-DD"),
          to: moment().format("YYYY-MM-DD"),
          batch: batch,
        });
        increaseRateRef.current = findRtvalue.increaseRate;
        currentSelectedInvestorRef.current = selected;
      }
    }
  }, [findRtvalue, setForm, selected, selectedFund, setSearchForm, rtValues]);

  useEffect(() => {
    if (categoryType === "investor") {
      setSelected(selectedInvestorForCategory);
    } else {
      setSelected(investors[0]?.id);
    }
  }, [investors, selectedInvestorForCategory, categoryType, setSelected]);

  useEffect(() => {
    searchForm &&
      setForm({
        ...searchForm,
        raiseRate: roundDecimal(searchForm.raiseRate * 100),
      });
  }, [searchForm]);

  const handleChange = (event) => {
    const value = event.target.value;
    const name = event.target.name;
    setForm((pre) => ({ ...pre, [name]: value }));
  };

  const hanldeBatchChange = (event) => {
    handleChange(event);
    const batchMap = localStorage.getItem("batchMap")
      ? JSON.parse(localStorage.getItem("batchMap"))
      : [];
    const batchValue = event.target.value;
    localStorage.setItem(
      "batchMap",
      JSON.stringify([
        ...batchMap.filter(
          (item) => item[0] !== selectedFund && item[1] !== selected
        ),
        [selectedFund, selected, batchValue],
      ])
    );
  };

  const handleSearch = () => {
    setSearchForm({ ...form, raiseRate: Number(form.raiseRate) / 100 });
  };

  const handleRefresh = async (event) => {
    event.stopPropagation();
    event.preventDefault();
    const response = await axios.get("/rtvalues", {
      params: {
        codes: selectedFund,
      },
    });
    const data = response.data;
    const rate = roundDecimal(data[selectedFund]?.increaseRate * 100);
    const value = rate === "NaN" || rate === "Infinity" ? "" : rate;
    setForm((pre) => ({ ...pre, raiseRate: value }));
  };

  const isRtvalueOutdated = findRtvalue?.day !== moment().format("YYYY-MM-DD");

  return (
    <Stack
      direction={"row"}
      spacing={2}
      sx={{ alignItems: "center", justifyContent: "space-between" }}
    >
      <Stack direction={"row"} spacing={1} sx={{ alignItems: "center" }}>
        <Stack sx={{ paddingRight: "1rem" }}>
          <Typography
            variant="h6"
            className={!isRtvalueOutdated && styles.blink}
            sx={{
              fontWeight: "bold",
              color: isRtvalueOutdated
                ? "lightgray"
                : findRtvalue?.increaseRate < 0
                ? "success.main"
                : "error.main",
            }}
          >
            {roundDecimal(findRtvalue?.unitValue, 4)}
            <Typography variant="caption" sx={{ marginLeft: "0.5rem" }}>
              ({roundDecimal(findRtvalue?.increaseRate * 100)}%)
            </Typography>
          </Typography>
          <Typography variant="caption">{findRtvalue?.time}</Typography>
        </Stack>
        <Divider
          orientation="vertical"
          flexItem
          sx={{
            marginRight: "1rem !important",
          }}
        />
        {categoryType !== "investor" &&
          investors.map((item) => {
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
        <TextField
          id="standard-raiseRate"
          InputLabelProps={{ shrink: true }}
          sx={{
            width: "100px",
          }}
          label={
            <Stack direction={"row"} alignItems={"center"}>
              <IconButton size="small" onClick={handleRefresh}>
                <LoopIcon fontSize="inherit" />
              </IconButton>
            </Stack>
          }
          variant="standard"
          name="raiseRate"
          value={form.raiseRate}
          onChange={handleChange}
        />
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
          onChange={hanldeBatchChange}
        />
        <Button variant="contained" onClick={handleSearch}>
          Search
        </Button>
      </Stack>
    </Stack>
  );
}
