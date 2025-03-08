import { useRecoilState, useRecoilValue } from "recoil";
import { fundsQuery } from "../../../store/selector";
import {
  fundsOfInvestorCategoryState,
  selectedCategoryState,
  selectedCategoryTypeState,
  selectedFundState,
} from "../../../store/atom";
import { useEffect, useState } from "react";
import { Chip, Stack, Tooltip, Typography } from "@mui/material";

export default function CodeList() {
  const funds = useRecoilValue(fundsQuery);
  const [selected, setSelected] = useRecoilState(selectedFundState);
  const selectedCategory = useRecoilValue(selectedCategoryState);
  const fundsOfInvestorCategory = useRecoilValue(fundsOfInvestorCategoryState);
  const categoryType = useRecoilValue(selectedCategoryTypeState);

  const [filteredFunds, setFilteredFunds] = useState();

  useEffect(() => {
    if (categoryType === "investor") {
      setFilteredFunds(fundsOfInvestorCategory);
    } else {
      const newFunds = funds.filter((item) => {
        if (selectedCategory === "__others__") {
          return !item.category;
        }
        return item.category === selectedCategory;
      });
      setFilteredFunds(newFunds);
    }
  }, [categoryType, funds, fundsOfInvestorCategory, selectedCategory]);

  useEffect(() => {
    setSelected(filteredFunds?.[0]?.id);
  }, [filteredFunds, setSelected]);

  const handleClick = (id) => {
    setSelected(id);
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
      {filteredFunds?.map((item) => {
        return (
          <Tooltip
            title={
              <Stack>
                <Typography variant="subtitle1">
                  {item.url ? (
                    <a
                      href={item.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      style={{
                        color: "#fff",
                      }}
                    >
                      {item.id}
                    </a>
                  ) : (
                    item.id
                  )}
                </Typography>
              </Stack>
            }
          >
            <Chip
              key={item.id}
              label={item.alias || item.name}
              color="primary"
              variant={selected === item.id ? "filled" : "outlined"}
              clickable={true}
              onClick={() => handleClick(item.id)}
            />
          </Tooltip>
        );
      })}
    </Stack>
  );
}
