import { useRecoilValue, useSetRecoilState } from "recoil";
import { investorsQuery } from "../../../store/selector";
import { refreshInvestorState } from "../../../store/atom";
import { useState } from "react";
import { Box, Chip, Divider, IconButton, Stack } from "@mui/material";
import axios from "axios";
import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import ConfirmModal from "../../../components/confirm-modal";
import InvestorModal from "./investor-mdoal";
import CustomAvatar from "../../../utils/get-icons";

const BASE_URL = process.env.REACT_APP_BASE_URL;

export default function InvestorConfig() {
  const investors = useRecoilValue(investorsQuery);
  const refetchInvestors = useSetRecoilState(refreshInvestorState);
  const [investorModal, setInvestorModal] = useState({
    open: false,
    data: null,
    mode: "add",
  });
  const [confirmModal, setConfirmModal] = useState({
    open: false,
    investor: null,
  });

  const handleAdd = () => {
    setInvestorModal((pre) => ({
      ...pre,
      open: true,
      mode: "add",
    }));
  };

  const handleEdit = (investor) => {
    setInvestorModal((pre) => ({
      ...pre,
      open: true,
      mode: "edit",
      data: investor,
    }));
  };

  const handleDelete = async (investor) => {
    await axios.delete(`${BASE_URL}/investor/${investor.id}`);
    refetchInvestors((pre) => pre + 1);
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
      {investors?.map((investor) => {
        return (
          <Chip
            sx={{
              "& .MuiChip-deleteIcon": {
                color: (theme) => theme.palette.primary.main,
              },
            }}
            icon={
              <CustomAvatar
                id={investor.icon || 0}
                style={{
                  height: "24px",
                  width: "24px",
                }}
              />
            }
            label={
              <Stack direction={"row"} spacing={0.5} alignItems={"center"}>
                <Box>{investor.name}</Box>
                <Divider
                  sx={{ paddingLeft: "10px" }}
                  orientation="vertical"
                  flexItem
                />
                <IconButton size="small" onClick={() => handleEdit(investor)}>
                  <EditIcon color="primary" />
                </IconButton>
                <IconButton size="small" onClick={() => setConfirmModal({
                  open: true,
                  investor: investor,
                })}>
                  <DeleteIcon color="error" />
                </IconButton>
              </Stack>
            }
            variant={"outlined"}
          />
        );
      })}
      <Chip
        icon={<AddIcon />}
        label="Add New"
        variant="outlined"
        onClick={handleAdd}
      />
      <InvestorModal
        open={investorModal.open}
        data={investorModal.data}
        mode={investorModal.mode}
        handleClose={() => setInvestorModal((pre) => ({ ...pre, open: false }))}
        handleSubmit={() => console.log("submit")}
      />
      <ConfirmModal
        open={confirmModal.open}
        handleClose={() => setConfirmModal({
          open: false,
          investor: null,
        })}
        handleSubmit={() => handleDelete(confirmModal.investor)}
        message="Are you sure you want to delete this item?"
      />
    </Stack>
  );
}
