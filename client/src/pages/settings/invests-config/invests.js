import { useRecoilValue, useSetRecoilState } from "recoil";
import { investsQuery } from "./store/selector";
import { alertState } from "../../../store/atom";
import {
  refreshInvestsState,
  searchFormState,
  selectedFundState,
  selectedInvestorState,
} from "./store/atom";
import { useMemo, useState } from "react";
import { formatNumber } from "../../../utils/process-number";
import axios from "axios";
import { Button, Stack } from "@mui/material";
import AggridWrapper from "../../../components/aggrid-wrapper";
import InvestModal from "./invest-modal";
import ConfirmModal from "../../../components/confirm-modal";
import styled from "@emotion/styled";

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

export default function Invests() {
  const invests = useRecoilValue(investsQuery);
  const setAlert = useSetRecoilState(alertState);
  const selectedInvestor = useRecoilValue(selectedInvestorState);
  const selectedFund = useRecoilValue(selectedFundState);
  const searchForm = useRecoilValue(searchFormState);
  const [investModal, setInvestModal] = useState({
    open: false,
    data: {},
    mode: "add",
  });

  const maxBatch = invests.reduce((acc, item) => {
    if (item.batch > acc) {
      return item.batch;
    }
    return acc;
  }, 0);

  const colDefs = useMemo(
    () => [
      { field: "day", flex: 1 },
      { field: "batch", flex: 1 },
      { field: "share", flex: 1 },
      { field: "unitPrice", flex: 1 },
      { field: "type", flex: 1 },
      { field: "fee", flex: 1 },
      {
        field: "amount",
        headerName: "Effective Amt",
        flex: 1,
        valueGetter: (p) => formatNumber(p.data.amount),
      },
      { field: "comments", flex: 1 },
      {
        field: "actions",
        flex: 1,
        cellRenderer: ActionsRenderer,
        cellRendererParams: {
          openEditModal: (rowData) =>
            setInvestModal((pre) => ({
              ...pre,
              open: true,
              mode: "edit",
              data: rowData,
            })),
        },
      },
    ],
    []
  );

  const handleUpload = async (event) => {
    let file = event.target.files[0];
    const formData = new FormData();
    formData.append("file", file);
    await axios.post(
      `${BASE_URL}/uploadinvests/${selectedInvestor}/${selectedFund}`,
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      }
    );
    setAlert({
      open: true,
      message: "Invests Uploaded successfully",
      type: "success",
    });
  };

  const handleDownload = () => {
    const params = Object.keys(searchForm || {}).reduce((acc, key) => {
      if (searchForm[key] !== "") {
        acc[key] = searchForm[key];
      }
      return acc;
    }, {});
    const query = new URLSearchParams(params).toString();
    window.open(`/exportinvests/${selectedInvestor}/${selectedFund}?${query}`);
  };

  return (
    <Stack style={{ flex: 1, minHeight: 400 }}>
      <Stack sx={{ alignSelf: "flex-start" }} direction={"row"} spacing={1}>
        <Button
          variant="contained"
          onClick={() =>
            setInvestModal((pre) => ({
              ...pre,
              open: true,
              mode: "add",
              data: {},
              maxBatch: maxBatch,
            }))
          }
        >
          Add new
        </Button>
        <Button component="label" variant="contained">
          Upload
          <VisuallyHiddenInput onChange={handleUpload} type="file" />
        </Button>
        <Button variant="contained" onClick={handleDownload}>
          Download
        </Button>
      </Stack>
      <AggridWrapper rowData={invests} columnDefs={colDefs} />
      <InvestModal
        open={investModal.open}
        data={investModal.data}
        mode={investModal.mode}
        maxBatch={investModal.maxBatch || 0}
        handleClose={() => setInvestModal((pre) => ({ ...pre, open: false }))}
        handleSubmit={() => console.log("submit")}
      />
    </Stack>
  );
}

function ActionsRenderer(props) {
  const { openEditModal, data } = props;
  const refetchInvests = useSetRecoilState(refreshInvestsState);
  const selectedInvestor = useRecoilValue(selectedInvestorState);
  const [confirmModal, setConfirmModal] = useState(false);

  const handleEdit = () => {
    openEditModal(data);
  };

  const handleDelete = () => {
    setConfirmModal(true);
  };

  const deleteInvest = async () => {
    await axios.delete(`${BASE_URL}/invest/${selectedInvestor}/${data.id}`);
    refetchInvests((pre) => pre + 1);
  };

  return (
    <>
      <Button size="small" onClick={handleEdit}>
        edit
      </Button>
      <Button size="small" color="error" onClick={handleDelete}>
        delete
      </Button>
      <ConfirmModal
        open={confirmModal}
        handleClose={() => setConfirmModal(false)}
        handleSubmit={deleteInvest}
        message="Are you sure you want to delete this item?"
      />
    </>
  );
}
