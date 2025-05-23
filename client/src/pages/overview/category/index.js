import { useRecoilState, useRecoilValue, useSetRecoilState } from "recoil";
import { fundsQuery, investorsQuery } from "../../../store/selector";
import {
  fundsOfInvestorCategoryState,
  selectedCategoryState,
  selectedCategoryTypeState,
  selectedInvestorForCategoryState,
  userInfoState,
} from "../../../store/atom";
import { useCallback, useEffect, useMemo } from "react";
import { Stack } from "@mui/system";
import { Chip, Divider, IconButton, Tooltip } from "@mui/material";
import SummarizeIcon from "@mui/icons-material/Summarize";
import CustomAvatar from "../../../utils/get-icons";
import axios from "axios";
import { BASE_URL } from "../../../utils/get-baseurl";

export default function CategoryList({ setTotalSummaryDrawer }) {
  const userInfo = useRecoilValue(userInfoState);
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

  const handleInvestorChange = useCallback(async (id) => {
    setTotalSummaryDrawer((pre) => ({ ...pre, investor: id }));
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
  }, [setCategoryType, setFundsOfInvestorCategory, setSelected, setSelectedInvestor, setTotalSummaryDrawer]);

  useEffect(() => {
    if (!userInfo?.id) return;
    handleInvestorChange(userInfo.id);
  }, [userInfo, handleInvestorChange]);

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
          onClick={() => {
            setTotalSummaryDrawer({
              open: true,
              investor: selectedInvestor,
            });
          }}
        >
          <SummarizeIcon />
        </IconButton>
      )}
    </Stack>
  );
}
