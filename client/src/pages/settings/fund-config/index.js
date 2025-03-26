import { useRecoilValue, useSetRecoilState } from "recoil";
import { fundsQuery } from "../../../store/selector";
import { refreshFundState } from "../../../store/atom";
import { useState } from "react";
import {
  Box,
  Chip,
  Divider,
  IconButton,
  Stack,
  Tooltip,
  Typography,
} from "@mui/material";
import axios from "axios";
import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";
import StoreIcon from "@mui/icons-material/Store";
import DeleteIcon from "@mui/icons-material/Delete";
import ConfirmModal from "../../../components/confirm-modal";
import FundModal from "./fund-modal";

const BASE_URL = process.env.REACT_APP_BASE_URL;

export default function FundConfig() {
  const funds = useRecoilValue(fundsQuery);
  const refetchFunds = useSetRecoilState(refreshFundState);
  const [fundModal, setFundModal] = useState({
    open: false,
    data: null,
    mode: "add",
  });
  const [confirmModal, setConfirmModal] = useState({
    open: false,
    fund: null,
  });

  const handleAdd = () => {
    setFundModal((pre) => ({
      ...pre,
      open: true,
      mode: "add",
      data: { etf: false },
    }));
  };

  const handleEdit = (fund) => {
    const { etf, id, priority, ...rest } = fund;
    setFundModal((pre) => ({
      ...pre,
      open: true,
      mode: "edit",
      data: { code: id, etf, priority, ...rest },
    }));
  };

  const handleDelete = async (fund) => {
    await axios.delete(`${BASE_URL}/fund/${fund.id}`);
    refetchFunds((pre) => pre + 1);
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
      {funds?.map((fund) => {
        return (
          <Tooltip
            title={
              <Stack>
                <Typography variant="subtitle1">
                  {fund.url ? (
                    <a
                      href={fund.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      style={{
                        color: "#fff",
                      }}
                    >
                      {fund.id}
                    </a>
                  ) : (
                    fund.id
                  )}
                </Typography>
              </Stack>
            }
          >
            <Chip
              icon={<StoreIcon />}
              label={
                <Stack direction={"row"} spacing={0.5} alignItems={"center"}>
                  <Box>{fund.alias || fund.name}</Box>
                  <Divider
                    sx={{ paddingLeft: "10px" }}
                    orientation="vertical"
                    flexItem
                  />
                  <IconButton size="small" onClick={() => handleEdit(fund)}>
                    <EditIcon color="primary" />
                  </IconButton>
                  <IconButton
                    size="small"
                    onClick={() => setConfirmModal({
                      open: true,
                      fund: fund,
                    })}
                  >
                    <DeleteIcon color="error" />
                  </IconButton>
                </Stack>
              }
              variant={"outlined"}
            />
          </Tooltip>
        );
      })}
      <Chip
        icon={<AddIcon />}
        label="Add New"
        variant="outlined"
        onClick={handleAdd}
      />
      <FundModal
        open={fundModal.open}
        data={fundModal.data}
        mode={fundModal.mode}
        handleClose={() => setFundModal((pre) => ({ ...pre, open: false }))}
        handleSubmit={() => console.log("submit")}
      />
      <ConfirmModal
        open={confirmModal.open}
        handleClose={() => setConfirmModal({
          open: false,
          fund: null,
        })}
        handleSubmit={() => handleDelete(confirmModal.fund)}
        message="Are you sure you want to delete this item?"
      />
    </Stack>
  );
}
