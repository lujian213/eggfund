import { useRecoilState, useRecoilValue } from "recoil";
import { fundsQuery } from "../../../store/selector";
import { Chip, Stack, Tooltip, Typography } from "@mui/material";
import { selectedFundState } from "./store/atom";

export default function CodeList() {
  const funds = useRecoilValue(fundsQuery);
  const [selected, setSelected] = useRecoilState(selectedFundState);

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
