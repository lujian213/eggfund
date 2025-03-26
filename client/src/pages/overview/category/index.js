import { useRecoilState, useRecoilValue, useSetRecoilState } from "recoil";
import { fundsQuery, investorsQuery } from "../../../store/selector";
import {
  fundsOfInvestorCategoryState,
  selectedCategoryState,
  selectedCategoryTypeState,
  selectedInvestorForCategoryState,
} from "../../../store/atom";
import { useEffect, useMemo } from "react";
import { Stack } from "@mui/system";
import { Chip, Divider, IconButton, Tooltip } from "@mui/material";
import SummarizeIcon from "@mui/icons-material/Summarize";
import CustomAvatar from "../../../utils/get-icons";
import axios from "axios";

const BASE_URL = process.env.REACT_APP_BASE_URL;

export default function CategoryList({ handleDrawerOpen }) {
  const funds = useRecoilValue(fundsQuery);
  const investors = useRecoilValue(investorsQuery);
  const [selected, setSelected] = useRecoilState(selectedCategoryState);
  const [selectedInvestor, setSelectedInvestor] = useRecoilState(
    selectedInvestorForCategoryState
  );
  const setFundsOfInvestorCategory = useSetRecoilState(
    fundsOfInvestorCategoryState
  );
  const setCategoryType = useSetRecoilState(selectedCategoryTypeState);

  const categories = useMemo(
    () =>
      funds.reduce((acc, item) => {
        if (!acc.includes(item.category) && item.category) {
          acc.push(item.category);
        }
        return acc;
      }, []),
    [funds]
  );

  useEffect(() => {
    setSelected(categories?.[0]);
  }, [categories, setSelected]);

  const handleClick = (id) => {
    setCategoryType("funds");
    setSelectedInvestor(null);
    setSelected(id);
  };

  const handleInvestorChange = async (id) => {
    setSelectedInvestor(id);
    setSelected(null);
    setCategoryType("investor");
    try {
      const response = await axios.get(`${BASE_URL}/funds/${id}`);
      const data = response.data || [];
      setFundsOfInvestorCategory(data);
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <Stack
      direction={"row"}
      sx={{
        flexWrap: "wrap",
        gap: "8px",
        paddingBlock: "8px",
      }}
    >
      {categories.map((item) => (
        <Tooltip title={item}>
          <Chip
            key={item}
            label={item}
            color="primary"
            variant={selected === item ? "filled" : "outlined"}
            clickable={true}
            onClick={() => handleClick(item)}
          />
        </Tooltip>
      ))}
      <Chip
        label={"Others"}
        color="primary"
        variant={selected === "__others__" ? "filled" : "outlined"}
        clickable={true}
        onClick={() => handleClick("__others__")}
      />
      <Divider orientation="vertical" flexItem />
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
                  fill: "#000",
                }}
              />
            }
            label={item.name}
            color="primary"
            variant={selectedInvestor === item.id ? "filled" : "outlined"}
            clickable={true}
            onClick={() => handleInvestorChange(item.id)}
          />
        );
      })}
      {selectedInvestor && (
        <IconButton
          sx={{ marginLeft: "auto" }}
          onClick={() => handleDrawerOpen(selectedInvestor)}
        >
          <SummarizeIcon />
        </IconButton>
      )}
    </Stack>
  );
}
