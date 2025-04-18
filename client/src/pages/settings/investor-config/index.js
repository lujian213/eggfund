import { useRecoilValue, useSetRecoilState } from "recoil";
import { investorsQuery } from "../../../store/selector";
import {
  alertState,
  refreshInvestorState,
  userInfoState,
} from "../../../store/atom";
import { useState } from "react";
import { Box, Chip, Divider, IconButton, Stack } from "@mui/material";
import axios from "axios";
import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";
import UploadFileIcon from "@mui/icons-material/UploadFile";
import DeleteIcon from "@mui/icons-material/Delete";
import ConfirmModal from "../../../components/confirm-modal";
import InvestorModal from "./investor-mdoal";
import CustomAvatar from "../../../utils/get-icons";
import styled from "@emotion/styled";
import { refreshInvestsState } from "../invests-config/store/atom";

const VisuallyHiddenInput = styled("input")({
  clip: "rect(0 0 0 0)",
  clipPath: "inset(50%)",
  height: 1,
  overflow: "hidden",
  position: "absolute",
  bottom: 0,
  left: 0,
  whiteSpace: "nowrap",
  width: 1,
});

const BASE_URL = process.env.REACT_APP_BASE_URL;

export default function InvestorConfig() {
  const userInfo = useRecoilValue(userInfoState);
  const investors = useRecoilValue(investorsQuery);
  const refetchInvestors = useSetRecoilState(refreshInvestorState);
  const setAlert = useSetRecoilState(alertState);
  const refetchInvests = useSetRecoilState(refreshInvestsState);
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

  const handleUpload = async (event, investor) => {
    let file = event.target.files[0];
    const formData = new FormData();
    formData.append("file", file);
    await axios.post(`${BASE_URL}/uploadinvests/${investor.id}`, formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
    setAlert({
      open: true,
      message: "Invests Uploaded successfully",
      type: "success",
    });
    refetchInvests((pre) => pre + 1);
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
                {userInfo?.id === investor.id && (
                  <>
                    <Divider
                      sx={{ paddingLeft: "10px" }}
                      orientation="vertical"
                      flexItem
                    />
                    <IconButton
                      size="small"
                      onClick={() => handleEdit(investor)}
                    >
                      <EditIcon color="primary" />
                    </IconButton>
                    <IconButton
                      size="small"
                      onClick={() =>
                        setConfirmModal({
                          open: true,
                          investor: investor,
                        })
                      }
                    >
                      <DeleteIcon color="error" />
                    </IconButton>
                    <IconButton size="small" component="label">
                      <UploadFileIcon color="primary" />
                      <VisuallyHiddenInput
                        onChange={(event) => handleUpload(event, investor)}
                        type="file"
                      />
                    </IconButton>
                  </>
                )}
              </Stack>
            }
            variant={"outlined"}
          />
        );
      })}
      {userInfo?.roles?.includes("admin") && (
        <Chip
          icon={<AddIcon />}
          label="Add New"
          variant="outlined"
          onClick={handleAdd}
        />
      )}
      <InvestorModal
        open={investorModal.open}
        data={investorModal.data}
        mode={investorModal.mode}
        handleClose={() => setInvestorModal((pre) => ({ ...pre, open: false }))}
        handleSubmit={() => console.log("submit")}
      />
      <ConfirmModal
        open={confirmModal.open}
        handleClose={() =>
          setConfirmModal({
            open: false,
            investor: null,
          })
        }
        handleSubmit={() => handleDelete(confirmModal.investor)}
        message="Are you sure you want to delete this item?"
      />
    </Stack>
  );
}
