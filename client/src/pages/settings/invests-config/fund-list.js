import { useRecoilState, useRecoilValue } from "recoil";
import { fundsQuery } from "../../../store/selector";
import { Chip, Stack, Tooltip, Typography } from "@mui/material";
import { selectedFundState, selectedInvestorState } from "./store/atom";
import { userInfoState } from "../../../store/atom";

export default function CodeList() {
  const userInfo = useRecoilValue(userInfoState);
  const funds = useRecoilValue(fundsQuery);
  const [selected, setSelected] = useRecoilState(selectedFundState);
  const [, setSelectedInvestor] = useRecoilState(
    selectedInvestorState
  );

  const handleClick = (id) => {
    setSelected(id);
    if (!userInfo?.id) return;
    setSelectedInvestor(userInfo.id);
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
      {funds?.map((item) => {
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
