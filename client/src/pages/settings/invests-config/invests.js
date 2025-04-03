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
import { Button, Stack, useMediaQuery, useTheme } from "@mui/material";
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
  const theme = useTheme();
  const refetchInvests = useSetRecoilState(refreshInvestsState);
  const isLarge = useMediaQuery(theme.breakpoints.up("md"));
  const invests = useRecoilValue(investsQuery);
  const setAlert = useSetRecoilState(alertState);
  const selectedInvestor = useRecoilValue(selectedInvestorState);
  const selectedFund = useRecoilValue(selectedFundState);
  const searchForm = useRecoilValue(searchFormState);
  const [selectedRowIds, setSelectedRowIds] = useState([]);
  const [investModal, setInvestModal] = useState({
    open: false,
    data: {},
    mode: "add",
  });
  const [confirmModal, setConfirmModal] = useState(false);

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
      { field: "tax", flex: 1 },
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
        minWidth: 200,
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
    refetchInvests((pre) => pre + 1);
  };

  const handleDownload = () => {
    const params = Object.keys(searchForm || {}).reduce((acc, key) => {
      if (searchForm[key] !== "") {
        acc[key] = searchForm[key];
      }
      return acc;
    }, {});
    const query = new URLSearchParams(params).toString();
    const windowBaseURL = window.location.origin;
    window.open(
      `${windowBaseURL}/exportinvests/${selectedInvestor}/${selectedFund}?${query}`
    );
  };

  let containerStyle = {};

  if (!isLarge) {
    containerStyle = {
      height: "calc(100vh - 64px)",
      overflow: "auto",
      flexBasis: "calc(100vh - 64px)",
    };
  }

  const onSelectionChanged = (selectedRows) => {
    const selectedIds = selectedRows.api.getSelectedRows().map((row) => row.id);
    setSelectedRowIds(selectedIds);
  };

  const openDeletConfirmModal = async () => {
    setConfirmModal(true);
  };

  const handleDeleteRows = async () => {
    const investIds = selectedRowIds.join(",");
    await axios.delete(`${BASE_URL}/invest/${selectedInvestor}`, {
      params: {
        investIds,
      },
    });
    refetchInvests((pre) => pre + 1);
  };

  return (
    <Stack style={{ flex: 1, ...containerStyle }}>
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
        {selectedRowIds.length > 0 && (
          <Button
            variant="contained"
            color="error"
            onClick={openDeletConfirmModal}
          >
            Delete
          </Button>
        )}
        <ConfirmModal
          open={confirmModal}
          handleClose={() => setConfirmModal(false)}
          handleSubmit={handleDeleteRows}
          message="Are you sure you want to delete these items?"
        />
      </Stack>
      <AggridWrapper
        rowData={invests}
        columnDefs={colDefs}
        rowSelection={{ mode: "multiRow" }}
        onSelectionChanged={onSelectionChanged}
        getRowId={(params) => params.data.id}
      />
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
