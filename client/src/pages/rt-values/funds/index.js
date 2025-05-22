import {
  Chip,
  Stack,
  Tooltip,
  Typography,
  useMediaQuery,
  useTheme,
} from "@mui/material";
import { useRecoilState, useRecoilValue, useSetRecoilState } from "recoil";
import { fundsQuery } from "../../../store/selector";
import { rtValuesState } from "../../../store/atom";
import { datasourceState, selectedFundsState } from "../store/atom";
import { useEffect } from "react";

export default function Funds() {
  const theme = useTheme();
  const isLarge = useMediaQuery(theme.breakpoints.up("md"));
  const funds = useRecoilValue(fundsQuery);
  const rtValues = useRecoilValue(rtValuesState);
  const [selectedFunds, setSelectedFunds] = useRecoilState(selectedFundsState);
  const setDatasource = useSetRecoilState(datasourceState);

  useEffect(() => {
    if (funds) {
      setSelectedFunds(funds.map((item) => item.id));
      setDatasource((pre) => {
        return funds.map((item) => {
          const isSelectedFund = pre?.find((data) => data.code === item.id);
          if (!isSelectedFund) {
            return {
              code: item.id,
              fundName: item.name,
              alias: item.alias,
              category: item.category,
              type: item.type,
              currency: item.currency,
              currencySign: item.currencySign,
            };
          }
          return {
            ...isSelectedFund,
            fundName: item.name,
            alias: item.alias,
            category: item.category,
            type: item.type,
            currency: item.currency,
            currencySign: item.currencySign,
          };
        });
      });
    }
  }, [funds, setDatasource, setSelectedFunds]);

  const handleClick = (id, name, alias) => {
    setSelectedFunds((prev) => {
      let newSelectedFunds = [];
      if (prev.includes(id)) {
        newSelectedFunds = prev.filter((item) => item !== id);
      } else {
        newSelectedFunds = [...prev, id];
      }
      return newSelectedFunds;
    });
    setDatasource((pre) => {
      const isSelectedFund = pre.find((item) => item.code === id);
      const findItem = rtValues.find((data) => data.code === id) || {};
      if (!isSelectedFund) {
        return [...pre, { code: id, fundName: name, alias, ...findItem }];
      }
      return pre.filter((item) => item.code !== id);
    });
  };

  if (!isLarge) {
    return null;
  }

  return (
    <Stack
      direction={"row"}
      sx={{
        flexWrap: "wrap",
        gap: "8px",
        paddingBlock: "8px",
      }}
    >
      {funds.map((item) => {
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
              variant={selectedFunds.includes(item.id) ? "filled" : "outlined"}
              clickable={true}
              onClick={() => handleClick(item.id, item.name, item.alias)}
            />
          </Tooltip>
        );
      })}
    </Stack>
  );
}
